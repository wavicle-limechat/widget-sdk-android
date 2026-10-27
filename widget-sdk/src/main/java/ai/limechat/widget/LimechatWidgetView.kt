package ai.limechat.widget

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import android.widget.FrameLayout
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import ai.limechat.widget.internal.ConversationTokenStore
import ai.limechat.widget.internal.WebViewFactory
import ai.limechat.widget.internal.WidgetChromeClient
import ai.limechat.widget.internal.WidgetWebViewClient
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.utils.MessageHandler
import ai.limechat.widget.utils.UrlBuilder
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * Main widget view that renders the Limechat widget in a hardened WebView
 */
class LimechatWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LimechatWidgetView"
        private const val MESSAGE_LISTENER_NAME = "LimechatNative"
        private const val JS_INTERFACE_NAME = "LimechatAndroid"
    }

    private val webView: WebView
    private var config: WidgetConfig? = null
    private var callback: WidgetCallback? = null
    private var filePicker: WidgetFilePicker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val messageHandler = MessageHandler()
    private var isMessagingSetup = false
    private var webMessageListenerAdded = false
    private var jsInterfaceAdded = false
    
    private var instanceId: String
    
    private var cwConversation: String? = null

    init {
        webView = WebViewFactory.create(
            context,
            WidgetWebViewClient(
                context,
                baseUrl = { config?.baseUrl },
                callback = { callback },
                onPageLoaded = {
                    // Ensure messaging bridge is re-established on every load/HMR
                    isMessagingSetup = false
                    setupMessaging()
                    callback?.onLoaded()
                }
            ),
            WidgetChromeClient { filePicker }
        )
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        
        instanceId = ""
    }

    /**
     * Initialize the widget with configuration and callback
     */
    fun init(config: WidgetConfig, callback: WidgetCallback? = null) {
        init(config, callback, null, null)
    }

    /**
     * Initialize the widget with configuration, callback and initial message
     */
    fun init(config: WidgetConfig, callback: WidgetCallback? = null, initialMessage: String? = null) {
        init(config, callback, initialMessage, null)
    }

    /**
     * Initialize the widget with configuration, callback and initial message data
     */
    fun init(config: WidgetConfig, callback: WidgetCallback? = null, initialMessageData: Map<String, Any>? = null) {
        init(config, callback, null, initialMessageData)
    }

    /**
     * Initialize the widget with configuration, callback, and optional initial message parameters
     * This is the main init method that all other overloads delegate to
     */
    private fun init(
        config: WidgetConfig, 
        callback: WidgetCallback? = null, 
        initialMessage: String? = null,
        initialMessageData: Map<String, Any>? = null
    ) {
        this.config = config
        this.callback = callback
        
        instanceId = if (!config.instanceId.isNullOrBlank()) {
            config.instanceId
        } else {
            "default"
        }
        
        if (config.conversationToken != null) {
            cwConversation = config.conversationToken
        } else {
            cwConversation = ConversationTokenStore.load(context, instanceId)
        }
        
        
        // the file picker is attached separately with attachFilePicker() so it can be created in the activity's onCreate()
        loadWidget(initialMessage, initialMessageData)
    }

    /**
     * Send a message to the widget
     */
    fun sendMessage(event: String, data: Map<String, Any> = emptyMap()) {
        val message = JSONObject().apply {
            put("event", event)
            data.forEach { (key, value) -> put(key, value) }
        }
        
        val script = """
            window.postMessage('limechat-widget:${message}', '*');
        """.trimIndent()
        
        webView.evaluateJavascript(script, null)
    }

    /**
     * Open the widget with an optional custom message
     * @param message Optional string message to display when opening the widget. Null does nothing.
     */
    fun open(message: String? = null) {
        if (message != null) {
            reloadWidgetWithMessage(message)
        }
    }

    /**
     * Open the widget with a custom message object
     * @param messageData Message object that can contain 'content' key or other properties
     */
    fun open(messageData: Map<String, Any>) {
        reloadWidgetWithMessageData(messageData)
    }

    private fun reloadWidgetWithMessage(message: String) {
        val config = this.config ?: return
        loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation, openMessage = message))
    }

    private fun reloadWidgetWithMessageData(messageData: Map<String, Any>) {
        val config = this.config ?: return
        loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation, openPayload = JSONObject(messageData).toString()))
    }

    private fun loadUrl(url: String) {
        Log.d(TAG, "Loading widget URL: ${redactUrl(url)}")
        webView.loadUrl(url)
    }

    /**
     * Handle back press for WebView navigation
     */
    fun onBackPressed(): Boolean {
        return if (webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    /**
     * Attach a file picker for handling file uploads
     */
    fun attachFilePicker(filePicker: WidgetFilePicker) {
        this.filePicker = filePicker
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        coroutineScope.cancel()
        webView.destroy()
    }

    /**
     * Hide the conversation token before a url is logged
     */
    private fun redactUrl(url: String): String {
        return url.replace(Regex("(cw_conversation=)[^&#]*"), "$1<redacted>")
    }

    private fun loadWidget(initialMessage: String? = null, initialMessageData: Map<String, Any>? = null) {
        val config = this.config ?: return

        // Bridge must exist before the page loads so the page sees window.LimechatAndroid immediately
        setupMessaging()

        loadUrl(
            UrlBuilder.buildWidgetUrl(
                config,
                cwConversation,
                openMessage = initialMessage,
                openPayload = initialMessageData?.let { JSONObject(it).toString() }
            )
        )
    }

    private fun setupMessaging() {
        if (isMessagingSetup) {
            return
        }
        
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            setupWebMessageListener()
            setupJavascriptInterface()
        } else {
            setupJavascriptInterface()
        }
        
        isMessagingSetup = true
    }

    private fun setupWebMessageListener() {
        try {
            if (!webMessageListenerAdded) {
                WebViewCompat.addWebMessageListener(
                    webView,
                    MESSAGE_LISTENER_NAME,
                    setOf("*"),
                    object : WebViewCompat.WebMessageListener {
                        override fun onPostMessage(
                            view: WebView,
                            message: WebMessageCompat,
                            sourceOrigin: Uri,
                            isMainFrame: Boolean,
                            replyProxy: JavaScriptReplyProxy
                        ) {
                            message.data?.let { data ->
                                handleMessage(data)
                            }
                        }
                    }
                )
                webMessageListenerAdded = true
            }
            // Inject (idempotent) script to forward window.postMessage to native bridge
            val script = """
                (function(){
                  if (!window.__lcAndroidBridgeWML) {
                    window.__lcAndroidBridgeWML = true;
                    // Wrap window.postMessage to also forward limechat-widget messages to native
                    try {
                      if (!window.__lcPostMessageWrapped) {
                        window.__lcPostMessageWrapped = true;
                        var __lcOrigPostMessage = window.postMessage.bind(window);
                        window.postMessage = function(data, targetOrigin, transfer) {
                          try {
                            if (typeof data === 'string' && data.indexOf('limechat-widget:') === 0) {
                              if (typeof window.$MESSAGE_LISTENER_NAME !== 'undefined' && typeof window.$MESSAGE_LISTENER_NAME.postMessage === 'function') {
                                window.$MESSAGE_LISTENER_NAME.postMessage(data);
                              } else if (typeof window.$JS_INTERFACE_NAME !== 'undefined' && typeof window.$JS_INTERFACE_NAME.postMessage === 'function') {
                                window.$JS_INTERFACE_NAME.postMessage(data);
                              }
                              try { console.log('[LimechatBridge] forwarded via wrapped postMessage'); } catch (e) {}
                            }
                          } catch (e) {}
                          return __lcOrigPostMessage(data, targetOrigin, transfer);
                        };
                      }
                    } catch (e) {}
                    window.addEventListener('message', function(e) {
                      if (typeof e.data === 'string' && e.data.startsWith('limechat-widget:')) {
                        try {
                          if (typeof window.$MESSAGE_LISTENER_NAME !== 'undefined' && typeof window.$MESSAGE_LISTENER_NAME.postMessage === 'function') {
                            window.$MESSAGE_LISTENER_NAME.postMessage(e.data);
                          } else if (typeof window.$JS_INTERFACE_NAME !== 'undefined' && typeof window.$JS_INTERFACE_NAME.postMessage === 'function') {
                            window.$JS_INTERFACE_NAME.postMessage(e.data);
                          }
                        } catch (err) {}
                      }
                    });
                  }
                })();
            """.trimIndent()
            webView.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up WebMessageListener", e)
            setupJavascriptInterface()
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun setupJavascriptInterface() {
        if (!jsInterfaceAdded) {
            webView.addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) {
                    this@LimechatWidgetView.handleMessage(message)
                }
            }, JS_INTERFACE_NAME)
            jsInterfaceAdded = true
        }
        
        // Inject (idempotent) script to use JavascriptInterface
        val script = """
            (function(){
              if (!window.__lcAndroidBridgeJSI) {
                window.__lcAndroidBridgeJSI = true;
                // Wrap window.postMessage to also forward limechat-widget messages to native
                try {
                  if (!window.__lcPostMessageWrapped) {
                    window.__lcPostMessageWrapped = true;
                    var __lcOrigPostMessage = window.postMessage.bind(window);
                    window.postMessage = function(data, targetOrigin, transfer) {
                      try {
                        if (typeof data === 'string' && data.indexOf('limechat-widget:') === 0) {
                          if (typeof window.$JS_INTERFACE_NAME !== 'undefined' && typeof window.$JS_INTERFACE_NAME.postMessage === 'function') {
                            window.$JS_INTERFACE_NAME.postMessage(data);
                          } else if (typeof window.$MESSAGE_LISTENER_NAME !== 'undefined' && typeof window.$MESSAGE_LISTENER_NAME.postMessage === 'function') {
                            window.$MESSAGE_LISTENER_NAME.postMessage(data);
                          }
                          try { console.log('[LimechatBridge] forwarded via wrapped postMessage'); } catch (e) {}
                        }
                      } catch (e) {}
                      return __lcOrigPostMessage(data, targetOrigin, transfer);
                    };
                  }
                } catch (e) {}
                window.addEventListener('message', function(e) {
                  if (typeof e.data === 'string' && e.data.startsWith('limechat-widget:')) {
                    try {
                      if (typeof window.$JS_INTERFACE_NAME !== 'undefined' && typeof window.$JS_INTERFACE_NAME.postMessage === 'function') {
                        window.$JS_INTERFACE_NAME.postMessage(e.data);
                      } else if (typeof window.$MESSAGE_LISTENER_NAME !== 'undefined' && typeof window.$MESSAGE_LISTENER_NAME.postMessage === 'function') {
                        window.$MESSAGE_LISTENER_NAME.postMessage(e.data);
                      }
                    } catch (err) {}
                  }
                });
              }
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }

    private fun handleMessage(data: String) {
        coroutineScope.launch {
            try {
                val processed = messageHandler.processMessage(data)
                processed?.let { message ->
                    val event = message["event"] as? String
                    val type = message["type"] as? String
                    val eventName = event ?: type ?: "unknown"
                    
                    when (eventName) {
                        "close-widget" -> {
                            handleCloseEvent()
                        }
                        "set-cw-conversation" -> {
                            val token = message["cw_conversation"] as? String
                            handleConversationTokenUpdate(token)
                        }
                        else -> callback?.onMessage(message)
                    }
                }
            } catch (e: Exception) {
                callback?.onError(
                    WidgetError(
                        WidgetError.ErrorCode.JAVASCRIPT_ERROR,
                        "Failed to process message: ${e.message}",
                        e
                    )
                )
            }
        }
    }
    
    /**
     * Handle close events with automatic activity close when legacy back icon is enabled
     */
    private fun handleCloseEvent() {
        val showLegacyBackIcon = config?.showLegacyBackIcon ?: false
        
        if (showLegacyBackIcon) {
            try {
                val activity = context as? android.app.Activity
                if (activity != null) {
                    activity.finish()
                } else {
                    callback?.onClose()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to auto-close activity: ${e.message}")
                callback?.onClose()
            }
        } else {
            callback?.onClose()
        }
    }
    
    private fun handleConversationTokenUpdate(token: String?) {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Received empty conversation token, ignoring")
            return
        }
        
        val config = this.config
        if (config?.onConversationTokenChange != null) {
            config.onConversationTokenChange.invoke(token)
        } else {
            cwConversation = token
            ConversationTokenStore.save(context, instanceId, token)
        }
        
        callback?.onConversationTokenChange(token)
    }
}
