package ai.limechat.widget

import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage
import ai.limechat.widget.models.WidgetState
import ai.limechat.widget.utils.ConversationTokenStore
import ai.limechat.widget.utils.WidgetUrlBuilder
import ai.limechat.widget.webview.WidgetWebViewManager
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import kotlinx.coroutines.*

/**
 * Refactored main widget view that renders the Limechat widget in a secure WebView
 *
 * Key improvements:
 * - Separation of concerns with dedicated managers
 * - Proper state management
 * - Enhanced error handling
 * - Memory leak prevention
 * - Better lifecycle management
 */
class LimechatWidgetViewRefactored
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LimechatWidgetView"
    }

    // Core components
    private var webView: WebView? = null
    private var webViewManager: WidgetWebViewManager? = null
    private var urlBuilder: WidgetUrlBuilder? = null
    private var filePicker: WidgetFilePicker? = null

    // Configuration and callbacks
    private var config: WidgetConfig? = null
    private var callback: WidgetCallback? = null

    // State management
    private var currentState = WidgetState()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var conversationToken: String? = null

    /** Initialize the widget with configuration and optional callback */
    fun initialize(
            config: WidgetConfig,
            callback: WidgetCallback? = null,
            initialMessage: WidgetMessage? = null
    ) {
        if (currentState.isInitialized) {
            Log.w(TAG, "Widget already initialized")
            return
        }

        try {
            val resolvedToken = resolveInitialConversationToken(config)
            val configWithToken = config.copy(conversationToken = resolvedToken)

            this.config = configWithToken
            this.callback = callback
            this.urlBuilder = WidgetUrlBuilder(configWithToken)

            conversationToken = resolvedToken
            resolvedToken?.let {
                ConversationTokenStore.save(context, configWithToken.websiteToken, configWithToken.baseUrl, it)
            }
            urlBuilder?.updateConversationToken(resolvedToken)

            updateState(currentState.copy(isInitialized = true).loading())
            setupWebView()
            loadWidget(initialMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize widget", e)
            handleError("Failed to initialize widget: ${e.message}", e)
        }
    }

    /** Open the widget with a custom message */
    fun openWithMessage(message: WidgetMessage) {
        if (!currentState.isReady) {
            Log.w(TAG, "Widget not ready, cannot open with message")
            return
        }

        try {
            val url = urlBuilder?.buildUrlWithMessage(message) ?: return
            Log.d(TAG, "Opening widget with message: ${message.getMessageContent()}")
            webView?.loadUrl(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open widget with message", e)
            handleError("Failed to open with message: ${e.message}", e)
        }
    }

    /** Send a custom message to the widget via JavaScript */
    fun sendMessage(event: String, data: Map<String, Any> = emptyMap()) {
        if (!currentState.isReady) {
            Log.w(TAG, "Widget not ready, cannot send message")
            return
        }

        try {
            val script = buildMessageScript(event, data)
            webView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
        }
    }

    /** Handle back press for WebView navigation */
    fun handleBackPress(): Boolean {
        return webView?.let { webView ->
            if (webView.canGoBack()) {
                webView.goBack()
                true
            } else {
                false
            }
        }
                ?: false
    }

    /** Attach a file picker for handling file uploads */
    fun attachFilePicker(filePicker: WidgetFilePicker) {
        this.filePicker = filePicker
    }

    /** Get current widget state */
    fun getState(): WidgetState = currentState

    /** Check if widget is ready for interactions */
    fun isReady(): Boolean = currentState.isReady

    /** Clean up resources */
    fun cleanup() {
        try {
            coroutineScope.cancel()
            webViewManager?.cleanup()
            webView?.destroy()

            // Clear references
            webView = null
            webViewManager = null
            urlBuilder = null
            config = null
            callback = null
            conversationToken = null

            Log.d(TAG, "Widget cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    // Private implementation methods

    private fun setupWebView() {
        val config = this.config ?: throw IllegalStateException("Config not set")

        webViewManager = WidgetWebViewManager(context, config, createInternalCallback())
        webView =
                webViewManager?.createHardenedWebView()?.also { webView ->
                    addView(
                            webView,
                            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    )
                }
    }

    private fun loadWidget(initialMessage: WidgetMessage?) {
        val urlBuilder = this.urlBuilder ?: return
        val webView = this.webView ?: return

        try {
            val url =
                    when (initialMessage) {
                        null -> urlBuilder.buildBaseUrl()
                        else -> urlBuilder.buildUrlWithMessage(initialMessage)
                    }

            Log.d(TAG, "Loading widget: $url")
            webView.loadUrl(url)
        } catch (e: Exception) {
            handleError("Failed to load widget: ${e.message}", e)
        }
    }

    private fun createInternalCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                updateState(currentState.loaded())
                callback?.onLoaded()
            }

            override fun onClose() {
                callback?.onClose()
            }

            override fun onError(error: WidgetError) {
                updateState(currentState.error(error.message))
                callback?.onError(error)
            }

            override fun onMessage(message: Map<String, Any?>) {
                callback?.onMessage(message)
            }

            override fun onConversationTokenChange(conversationToken: String?) {
                val (sanitizedToken, _) = applyConversationToken(conversationToken)
                callback?.onConversationTokenChange(sanitizedToken)
            }
        }
    }

    /**
     * Update the conversation token used for chat persistence.
     */
    fun updateConversationToken(conversationToken: String?, reloadWidget: Boolean = false) {
        val (sanitizedToken, changed) = applyConversationToken(conversationToken)
        if (reloadWidget && changed) {
            loadWidget(null)
        }
    }

    private fun buildMessageScript(event: String, data: Map<String, Any>): String {
        val dataJson =
                if (data.isNotEmpty()) {
                    org.json.JSONObject(data).toString()
                } else {
                    "{}"
                }

        return """
            (function() {
                try {
                    window.postMessage('limechat-widget:{"event":"$event","data":$dataJson}', '*');
                } catch(e) {
                    console.error('Failed to send message:', e);
                }
            })();
        """.trimIndent()
    }

    private fun updateState(newState: WidgetState) {
        currentState = newState
        Log.d(
                TAG,
                "State updated: isReady=${newState.isReady}, isLoaded=${newState.isLoaded}, hasError=${newState.hasError}"
        )
    }

    private fun handleError(message: String, cause: Throwable? = null) {
        updateState(currentState.error(message))
        val error = WidgetError(WidgetError.ErrorCode.INITIALIZATION_ERROR, message, cause)
        callback?.onError(error)
    }

    private fun applyConversationToken(token: String?): Pair<String?, Boolean> {
        val sanitizedToken = token?.takeUnless { it.isBlank() }
        val changed = sanitizedToken != conversationToken
        if (changed) {
            conversationToken = sanitizedToken
            urlBuilder?.updateConversationToken(sanitizedToken)
            config = config?.copy(conversationToken = sanitizedToken)
            config?.let {
                ConversationTokenStore.save(context, it.websiteToken, it.baseUrl, sanitizedToken)
            }
        }
        return sanitizedToken to changed
    }

    private fun resolveInitialConversationToken(config: WidgetConfig): String? {
        val providedToken = config.conversationToken?.takeUnless { it.isBlank() }
        if (providedToken != null) {
            return providedToken
        }
        return ConversationTokenStore.get(context, config.websiteToken, config.baseUrl)
    }
}
