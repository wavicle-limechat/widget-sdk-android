package ai.limechat.widget.webview

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.*
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.utils.MessageHandler
import kotlinx.coroutines.*

/**
 * Manages WebView configuration, security, and messaging for the widget
 */
class WidgetWebViewManager(
    private val context: Context,
    private val config: WidgetConfig,
    private val callback: WidgetCallback?
) {
    companion object {
        private const val TAG = "WidgetWebViewManager"
        private const val MESSAGE_LISTENER_NAME = "LimechatNative"
        private const val JS_INTERFACE_NAME = "LimechatAndroid"
        private const val USER_AGENT_PREFIX = "LimechatWidget/Android"
        private const val WIDGET_Z_INDEX = 2147483647
    }

    private var isMessagingSetup = false
    private val messageHandler = MessageHandler()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Create and configure a hardened WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun createHardenedWebView(): WebView {
        return WebView(context).apply {
            configureSettings()
            webViewClient = createWebViewClient()
            webChromeClient = createWebChromeClient()
        }
    }

    private fun WebView.configureSettings() {
        settings.apply {
            // Enable JavaScript (required for widget)
            javaScriptEnabled = true
            
            // Enable DOM storage
            domStorageEnabled = true
            
            // Security hardening
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
            userAgentString = "$USER_AGENT_PREFIX $userAgentString"
            
            // Other optimizations
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                setupMessaging(view)
                callback?.onLoaded()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val widgetError = WidgetError(
                        WidgetError.ErrorCode.WEBVIEW_ERROR,
                        "WebView error: ${error.description}",
                        context = mapOf("errorCode" to error.errorCode)
                    )
                    callback?.onError(widgetError)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                
                // Handle special URLs
                if (url.startsWith("limechat://close")) {
                    callback?.onClose()
                    return true
                }
                
                // Only allow loading the widget URL
                if (!url.startsWith(config.baseUrl)) {
                    Log.w(TAG, "Blocked loading external URL: $url")
                    return true // Block loading
                }
                
                return false
            }
        }
    }

    private fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(TAG, "JS Console: ${consoleMessage.message()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun setupMessaging(webView: WebView) {
        if (isMessagingSetup) {
            Log.d(TAG, "Messaging already setup, skipping")
            return
        }
        
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                setupWebMessageListener(webView)
            } else {
                setupJavascriptInterface(webView)
            }
            isMessagingSetup = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup messaging", e)
            callback?.onError(
                WidgetError(
                    WidgetError.ErrorCode.JAVASCRIPT_ERROR,
                    "Failed to setup widget messaging: ${e.message}",
                    e
                )
            )
        }
    }

    private fun setupWebMessageListener(webView: WebView) {
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
            Log.d(TAG, "WebMessageListener setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up WebMessageListener, falling back to JavascriptInterface", e)
            setupJavascriptInterface(webView)
        }
    }

    @SuppressLint("JavascriptInterface")
    private fun setupJavascriptInterface(webView: WebView) {
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
        Log.d(TAG, "JavascriptInterface setup successful")
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
                        "widget-back" -> callback?.onClose() // Handle back button same as close
                        else -> callback?.onMessage(message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process message: $data", e)
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
     * Clean up resources
     */
    fun cleanup() {
        coroutineScope.cancel()
    }
}