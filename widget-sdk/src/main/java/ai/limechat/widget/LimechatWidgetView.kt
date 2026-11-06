package ai.limechat.widget

import ai.limechat.widget.internal.BridgeEvents
import ai.limechat.widget.internal.CloseHandler
import ai.limechat.widget.internal.ConversationTokenStore
import ai.limechat.widget.internal.JsBridge
import ai.limechat.widget.internal.WebViewFactory
import ai.limechat.widget.internal.WidgetChromeClient
import ai.limechat.widget.internal.WidgetWebViewClient
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.utils.MessageHandler
import ai.limechat.widget.utils.UrlBuilder
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Main widget view that renders the Limechat widget in a hardened WebView
 */
class LimechatWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private companion object {
        const val TAG = "LimechatWidgetView"
    }

    private val webView: WebView
    private var config: WidgetConfig? = null
    private var callback: WidgetCallback? = null
    private var filePicker: WidgetFilePicker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val messageHandler = MessageHandler()
    private lateinit var bridge: JsBridge
    private var instanceId = ""
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
                    bridge.reattach()
                    callback?.onLoaded()
                }
            ),
            WidgetChromeClient { filePicker }
        )
        bridge = JsBridge(webView) { handleMessage(it) }
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
        callback: WidgetCallback?,
        initialMessage: String?,
        initialMessageData: Map<String, Any>?
    ) {
        this.config = config
        this.callback = callback

        instanceId = config.instanceId?.takeIf { it.isNotBlank() } ?: "default"
        cwConversation = config.conversationToken ?: ConversationTokenStore.load(context, instanceId)

        // the file picker is attached separately with attachFilePicker() so it can be created in the activity's onCreate()
        loadWidget(initialMessage, initialMessageData)
    }

    /**
     * Send a message to the widget
     */
    fun sendMessage(event: String, data: Map<String, Any> = emptyMap()) {
        val message = JSONObject(mapOf<String, Any>("event" to event) + data)
        webView.evaluateJavascript("window.postMessage('chatwoot-widget:' + JSON.stringify($message), '*');", null)
    }

    /**
     * Open the widget with an optional custom message
     * @param message Optional string message to display when opening the widget. Null does nothing.
     */
    fun open(message: String? = null) {
        val config = this.config ?: return
        if (message != null) {
            loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation, openMessage = message))
        }
    }

    /**
     * Open the widget with a custom message object
     * @param messageData Message object that can contain 'content' key or other properties
     */
    fun open(messageData: Map<String, Any>) {
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
        if (!webView.canGoBack()) return false
        webView.goBack()
        return true
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

    private fun loadWidget(initialMessage: String?, initialMessageData: Map<String, Any>?) {
        val config = this.config ?: return

        // Bridge must exist before the page loads so the page sees window.LimechatAndroid immediately
        bridge.setup()

        loadUrl(
            UrlBuilder.buildWidgetUrl(
                config,
                cwConversation,
                openMessage = initialMessage,
                openPayload = initialMessageData?.let { JSONObject(it).toString() }
            )
        )
    }

    private fun handleMessage(data: String) {
        coroutineScope.launch {
            try {
                val processed = messageHandler.processMessage(data)
                processed?.let { message ->
                    when (BridgeEvents.nameOf(message)) {
                        BridgeEvents.CLOSE_WIDGET ->
                            CloseHandler.handle(context, config?.showLegacyBackIcon ?: false, callback)
                        BridgeEvents.SET_CW_CONVERSATION ->
                            handleConversationTokenUpdate(message["cw_conversation"] as? String)
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
