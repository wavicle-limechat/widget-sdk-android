package ai.limechat.widget

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebMessagePortCompat
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
    private var messagePort: WebMessagePortCompat? = null
    private val messageHandler = MessageHandler()

    init {
        webView = createHardenedWebView()
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * Initialize the widget with configuration and callback
     */
    fun init(config: WidgetConfig, callback: WidgetCallback? = null) {
        this.config = config
        this.callback = callback
        
        // Note: File picker should be attached separately using attachFilePicker()
        // to ensure it's created during onCreate() of the activity
        
        loadWidget()
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
                injectUserData()
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

    private fun loadWidget() {
        val config = this.config ?: return
        val url = UrlBuilder.buildWidgetUrl(config)
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
        // Try WebMessageListener first (preferred method)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            setupWebMessageListener()
        } else {
            // Fallback to JavascriptInterface
            setupJavascriptInterface()
        }
    }

    private fun setupWebMessageListener() {
        try {
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
            
            // Inject script to use WebMessageListener
            val script = """
                window.addEventListener('message', function(e) {
                    if (typeof e.data === 'string' && e.data.startsWith('limechat-widget:')) {
                        window.$MESSAGE_LISTENER_NAME.postMessage(e.data);
                    }
                });
            """.trimIndent()
            
            webView.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up WebMessageListener", e)
            setupJavascriptInterface()
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun setupJavascriptInterface() {
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun postMessage(message: String) {
                handleMessage(message)
            }
        }, JS_INTERFACE_NAME)
        
        // Inject script to use JavascriptInterface
        val script = """
            window.addEventListener('message', function(e) {
                if (typeof e.data === 'string' && e.data.startsWith('limechat-widget:')) {
                    window.$JS_INTERFACE_NAME.postMessage(e.data);
                }
            });
        """.trimIndent()
        
        webView.evaluateJavascript(script, null)
    }

    private fun handleMessage(data: String) {
        coroutineScope.launch {
            try {
                val processed = messageHandler.processMessage(data)
                processed?.let { message ->
                    val event = message["event"] as? String
                    
                    when (event) {
                        "loaded" -> callback?.onLoaded()
                        "close-widget" -> callback?.onClose()
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