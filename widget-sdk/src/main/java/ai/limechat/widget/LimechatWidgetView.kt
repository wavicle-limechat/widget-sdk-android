package ai.limechat.widget

import android.annotation.SuppressLint
import android.content.Context
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
        private const val USER_AGENT_PREFIX = "LimechatWidget/Android"
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
    private var internalConversationToken: String? = null

    init {
        webView = createHardenedWebView()
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
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
        
        // Store initial conversation token for internal persistence
        this.internalConversationToken = config.conversationToken
        
        // Note: File picker should be attached separately using attachFilePicker()
        // to ensure it's created during onCreate() of the activity
        
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
     * @param message Optional string message to display when opening the widget
     */
    fun open(message: String? = null) {
        if (message != null) {
            // Use URL parameters approach since we're loading the widget directly
            reloadWidgetWithMessage(message)
        } else {
            // Regular widget opening via JavaScript event
            val script = """
                if (window.bus && window.bus.${'$'}emit) {
                    window.bus.${'$'}emit('widget:toggle');
                }
            """.trimIndent()
            webView.evaluateJavascript(script, null)
        }
    }

    /**
     * Open the widget with a custom message object
     * @param messageData Message object that can contain 'content' key or other properties
     */
    fun open(messageData: Map<String, Any>) {
        // Use URL parameters approach with JSON payload
        reloadWidgetWithMessageData(messageData)
    }

    /**
     * Update the conversation token used for chat persistence.
     * @param conversationToken Token provided by the host app; pass null or blank to clear persistence.
     * @param reloadWidget When true, reloads the widget so the new token is applied immediately.
     */
    fun updateConversationToken(conversationToken: String?, reloadWidget: Boolean = false) {
        val sanitizedToken = conversationToken?.takeUnless { it.isBlank() }
        val tokenChanged = sanitizedToken != internalConversationToken

        internalConversationToken = sanitizedToken
        config = config?.copy(conversationToken = sanitizedToken)

        if (reloadWidget && tokenChanged) {
            loadWidget()
        }
    }

    /**
     * Reload the widget with a custom message via URL parameters
     */
    private fun reloadWidgetWithMessage(message: String) {
        val config = this.config ?: return
        // Use internal conversation token for persistence
        val configWithToken = config.copy(conversationToken = internalConversationToken)
        val baseUrl = UrlBuilder.buildWidgetUrl(configWithToken)
        val params = mapOf("lc_open_message" to message)
        val urlWithMessage = UrlBuilder.appendQueryParameters(baseUrl, params)

        Log.d(TAG, "Reloading widget with message: $message")
        Log.d(TAG, "Widget URL: $urlWithMessage")
        webView.loadUrl(urlWithMessage)
    }

    /**
     * Reload the widget with a custom message object via URL parameters
     */
    private fun reloadWidgetWithMessageData(messageData: Map<String, Any>) {
        val config = this.config ?: return
        // Use internal conversation token for persistence
        val configWithToken = config.copy(conversationToken = internalConversationToken)
        val baseUrl = UrlBuilder.buildWidgetUrl(configWithToken)
        val messageJson = JSONObject(messageData).toString()
        val params = mapOf("lc_open_payload" to messageJson)
        val urlWithPayload = UrlBuilder.appendQueryParameters(baseUrl, params)

        Log.d(TAG, "Reloading widget with message data: $messageData")
        Log.d(TAG, "Widget URL: $urlWithPayload")
        webView.loadUrl(urlWithPayload)
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun createHardenedWebView(): WebView {
        return WebView(context).apply {
            settings.apply {
                // Enable JavaScript (required for widget)
                javaScriptEnabled = true
                
                // Enable DOM storage
                domStorageEnabled = true
                
                // Harden WebView
                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                
                // Block mixed content
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                
                // Enable safe browsing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = true
                }
                
                // Custom user agent
                userAgentString = "$USER_AGENT_PREFIX ${userAgentString}"
                
                // Other settings
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            
            // Set WebViewClient
            webViewClient = createWebViewClient()
            
            // Set WebChromeClient for file picker
            webChromeClient = createWebChromeClient()
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // Ensure messaging bridge is re-established on every load/HMR
                isMessagingSetup = false
                setupMessaging()
                injectUserData()
                callback?.onLoaded()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    callback?.onError(
                        WidgetError(
                            WidgetError.ErrorCode.WEBVIEW_ERROR,
                            "WebView error: ${error.description}",
                            context = mapOf("errorCode" to error.errorCode)
                        )
                    )
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                
                // Handle special URLs
                if (url.startsWith("limechat://close")) {
                    callback?.onClose()
                    return true
                }
                
                // Only allow loading the widget URL
                val baseUrl = config?.baseUrl ?: return false
                if (!url.startsWith(baseUrl)) {
                    return true // Block loading
                }
                
                return false
            }
        }
    }

    private fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                filePicker?.showFileChooser(filePathCallback, fileChooserParams)
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(TAG, "JS Console: ${consoleMessage.message()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun loadWidget(initialMessage: String? = null, initialMessageData: Map<String, Any>? = null) {
        val config = this.config ?: return
        
        // Create config with internal conversation token for persistence
        val configWithToken = config.copy(conversationToken = internalConversationToken)
        var url = UrlBuilder.buildWidgetUrl(configWithToken)

        // Ensure messaging bridges are available before the page loads
        // so JS can see window.LimechatAndroid / window.LimechatNative immediately
        setupMessaging()
        
        // Add initial message parameters if provided
        val params = mutableMapOf<String, String>()
        
        initialMessage?.let { message ->
            params["lc_open_message"] = message
            Log.d(TAG, "Loading widget with initial message: $message")
        }
        
        initialMessageData?.let { data ->
            val messageJson = JSONObject(data).toString()
            params["lc_open_payload"] = messageJson
            Log.d(TAG, "Loading widget with initial message data: $data")
        }
        
        if (params.isNotEmpty()) {
            url = UrlBuilder.appendQueryParameters(url, params)
        }
        
        Log.d(TAG, "Loading widget URL: $url")
        webView.loadUrl(url)
    }

    private fun injectUserData() {
        val config = this.config ?: return
        
        coroutineScope.launch {
            delay(500) // Small delay to ensure page is ready
            
            val scripts = buildList {
                // Set user data
                config.user?.let { user ->
                    add("""
                        window.LimechatWidget = window.LimechatWidget || {};
                        window.LimechatWidget.setUser = function(user) {
                            window.postMessage(JSON.stringify({
                                event: 'set-user',
                                user: user,
                                identifier: user.identifier_hash
                            }), '*');
                        };
                        window.LimechatWidget.setUser({
                            name: "${user.name ?: ""}",
                            email: "${user.email ?: ""}",
                            phone_number: "${user.phoneNumber ?: ""}",
                            identifier_hash: "${user.identifierHash ?: ""}"
                        });
                    """.trimIndent())
                }
                
                // Set locale
                add("""
                    window.postMessage(JSON.stringify({
                        event: 'set-locale',
                        locale: '${config.locale}'
                    }), '*');
                """.trimIndent())
                
                // Set color scheme
                add("""
                    window.postMessage(JSON.stringify({
                        event: 'set-color-scheme',
                        darkMode: ${config.colorScheme == WidgetConfig.ColorScheme.DARK}
                    }), '*');
                """.trimIndent())
                
                // Set custom attributes
                config.customAttributes?.let { attrs ->
                    val attrsJson = JSONObject(attrs).toString()
                    add("""
                        window.postMessage(JSON.stringify({
                            event: 'set-custom-attributes',
                            customAttributes: $attrsJson
                        }), '*');
                    """.trimIndent())
                }
            }
            
            scripts.forEach { script ->
                webView.evaluateJavascript(script, null)
            }
        }
    }

    private fun setupMessaging() {
        if (isMessagingSetup) {
            Log.d(TAG, "Messaging already setup, skipping")
            return
        }
        
        // Try WebMessageListener first (preferred method)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            setupWebMessageListener()
            // Also expose a JS interface so window.LimechatAndroid exists
            // This lets the JS bridge call into Android directly when it prefers interfaces
            setupJavascriptInterface()
        } else {
            // Fallback to JavascriptInterface
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
            } else {
                Log.d(TAG, "WebMessageListener already added, reusing")
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
                    Log.d(TAG, "JS->Android postMessage: $message")
                    this@LimechatWidgetView.handleMessage(message)
                }

                // Support alternate method names used by JS bridge - all delegate to postMessage
                @JavascriptInterface
                fun onMessage(message: String) = postMessage(message)

                @JavascriptInterface
                fun receiveMessage(message: String) = postMessage(message)

                @JavascriptInterface
                fun handleMessage(message: String) = postMessage(message)
            }, JS_INTERFACE_NAME)
            jsInterfaceAdded = true
        } else {
            Log.d(TAG, "JS interface already added, reusing")
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
                    Log.d(TAG, "Received widget event: ${event ?: "unknown"} | payload: $message")
                    
                    when (event) {
                        "loaded" -> callback?.onLoaded()
                        "close-widget" -> callback?.onClose()
                        "widget-back" -> {
                            // Handle back button the same as close widget
                            Log.d(TAG, "Widget back button pressed - treating as close")
                            callback?.onClose()
                        }
                        "set-cw-conversation" -> {
                            val conversationToken = (message["cw_conversation"] as? String)?.takeUnless { it.isBlank() }
                            // Store internally for persistence within this instance
                            internalConversationToken = conversationToken
                            config = config?.copy(conversationToken = conversationToken)
                            Log.d(TAG, "Internal conversation token updated: ${conversationToken ?: "cleared"}")
                            // Also notify external callback for cross-instance persistence
                            callback?.onConversationTokenChange(conversationToken)
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
}
