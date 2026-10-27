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
        private const val PREFS_NAME = "limechat_widget_conversations"
        
        // In-memory conversation token cache for performance
        // Persists across widget instance recreation within same app session
        private val inMemoryTokenCache = mutableMapOf<String, String>()
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
    
    // Unique instance identifier (like React Native component instance)
    private var instanceId: String
    
    // Conversation token management (like React Native's component state)
    private var cwConversation: String? = null

    init {
        webView = createHardenedWebView()
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        
        // instanceId will be set during init() based on config
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
        
        // Set instance ID from config or generate a default one
        instanceId = if (!config.instanceId.isNullOrBlank()) {
            // App-provided instance ID - gives app full control over widget isolation
            config.instanceId
        } else {
            // Default: single instance per website token (like before the isolation fix)
            "default"
        }
        
        Log.d(TAG, "Widget instance ID: $instanceId")
        
        // Initialize conversation token (like React Native's cwConversation state)
        if (config.conversationToken != null) {
            // External token management - app provides token
            cwConversation = config.conversationToken
            Log.d(TAG, "Using external conversation token")
        } else {
            // Internal token management - SDK manages persistence using instanceId
            cwConversation = loadConversationToken(instanceId)
            Log.d(TAG, "Instance $instanceId - Loaded conversation token: ${if (cwConversation != null) "found" else "none"}")
        }
        
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
        // Use URL parameters approach with JSON payload
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
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                
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
                
                // Only allow loading the widget URL (same scheme + host + port as baseUrl)
                val baseUrl = config?.baseUrl ?: return false
                if (!isSameOrigin(request.url, baseUrl)) {
                    // User link taps open outside the widget, everything else stays blocked
                    if (request.isForMainFrame && request.hasGesture()) {
                        openExternally(url)
                    }
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
                val picker = filePicker
                if (picker == null) {
                    // No picker attached: cancel so the page's file input isn't stuck
                    filePathCallback.onReceiveValue(null)
                    return true
                }
                picker.showFileChooser(filePathCallback, fileChooserParams)
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(TAG, "JS Console: ${consoleMessage.message()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    /**
     * True if [uri] has the same scheme, host and port as [baseUrl]
     */
    private fun isSameOrigin(uri: Uri, baseUrl: String): Boolean {
        val base = Uri.parse(baseUrl.trimEnd('/'))
        return uri.scheme.equals(base.scheme, ignoreCase = true) &&
            uri.host != null &&
            uri.host.equals(base.host, ignoreCase = true) &&
            effectivePort(uri) == effectivePort(base)
    }

    private fun effectivePort(uri: Uri): Int {
        if (uri.port != -1) return uri.port
        return when (uri.scheme?.lowercase()) {
            "http" -> 80
            "https" -> 443
            else -> -1
        }
    }

    /**
     * Open a url outside the widget (system browser, mail, dialer)
     */
    private fun openExternally(url: String) {
        val uri = Uri.parse(url)
        val intent = when (uri.scheme?.lowercase()) {
            "http", "https", "mailto", "tel" -> Intent(Intent.ACTION_VIEW, uri)
            "intent" -> parseIntentUrl(url) ?: return
            else -> return
        }
        if (context !is android.app.Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallback = intent.getStringExtra("browser_fallback_url")
            val fallbackUri = fallback?.let { Uri.parse(it) }
            val fallbackScheme = fallbackUri?.scheme?.lowercase()
            if (uri.scheme.equals("intent", ignoreCase = true) && (fallbackScheme == "http" || fallbackScheme == "https")) {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                if (context !is android.app.Activity) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(fallbackIntent)
                    return
                } catch (fallbackError: ActivityNotFoundException) {
                    // reported below
                }
            }
            callback?.onError(
                WidgetError(
                    WidgetError.ErrorCode.WEBVIEW_ERROR,
                    "No app found to open external link",
                    e,
                    mapOf("scheme" to (uri.scheme ?: ""))
                )
            )
        } catch (e: SecurityException) {
            callback?.onError(
                WidgetError(
                    WidgetError.ErrorCode.WEBVIEW_ERROR,
                    "Not allowed to open external link",
                    e,
                    mapOf("scheme" to (uri.scheme ?: ""))
                )
            )
        }
    }

    /**
     * Parse an `intent:` url into a browsable intent, or null if it can't be parsed
     */
    private fun parseIntentUrl(url: String): Intent? {
        return try {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                component = null
                selector = null
            }
        } catch (e: Exception) {
            null
        }
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
                    Log.d(TAG, "JS->Android postMessage received")
                    this@LimechatWidgetView.handleMessage(message)
                }
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
                    val type = message["type"] as? String
                    val eventName = event ?: type ?: "unknown"
                    
                    Log.d(TAG, "Widget event: $eventName")
                    
                    when (eventName) {
                        "close-widget" -> {
                            Log.d(TAG, "Close widget event received")
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
        
        // If legacy back icon is enabled, automatically close the activity
        if (showLegacyBackIcon) {
            try {
                val activity = context as? android.app.Activity
                if (activity != null) {
                    activity.finish()
                } else {
                    // Fall back to callback if available
                    callback?.onClose()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to auto-close activity: ${e.message}")
                // Fall back to callback if available
                callback?.onClose()
            }
        } else {
            // Legacy back icon not enabled, use callback only
            callback?.onClose()
        }
    }
    
    /**
     * Load conversation token from persistent storage using instanceId
     * Provides cross-app restart persistence
     */
    private fun loadConversationToken(instanceId: String): String? {
        // First check in-memory cache for performance
        val memoryKey = instanceId
        inMemoryTokenCache[memoryKey]?.let { return it }
        
        // Load from SharedPreferences for cross-app persistence
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(instanceId, null)
        
        // Cache in memory for future access
        token?.let { inMemoryTokenCache[memoryKey] = it }
        
        return token
    }
    
    /**
     * Save conversation token to persistent storage using instanceId
     * Enables cross-app restart persistence
     */
    private fun saveConversationToken(instanceId: String, token: String) {
        // Save to in-memory cache for performance
        inMemoryTokenCache[instanceId] = token
        
        // Save to SharedPreferences for cross-app persistence
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(instanceId, token).apply()
    }
    
    private fun handleConversationTokenUpdate(token: String?) {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Received empty conversation token, ignoring")
            return
        }
        
        Log.d(TAG, "Conversation token updated")
        
        // Update conversation token (like React Native's handleCwConversationUpdate)
        val config = this.config
        if (config?.onConversationTokenChange != null) {
            // External token management - app handles persistence
            config.onConversationTokenChange.invoke(token)
            Log.d(TAG, "External token management - calling onConversationTokenChange")
        } else {
            // Internal token management - SDK handles persistence using instanceId
            cwConversation = token
            saveConversationToken(instanceId, token)
            Log.d(TAG, "Instance $instanceId - Saved conversation token")
        }
        
        // Also call the callback interface method
        callback?.onConversationTokenChange(token)
    }
}
