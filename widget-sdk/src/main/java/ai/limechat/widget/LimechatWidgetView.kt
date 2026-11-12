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

    private lateinit var webView: WebView
    private var config: WidgetConfig? = null
    private var callback: WidgetCallback? = null
    private var filePicker: WidgetFilePicker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val messageHandler = MessageHandler()
    private lateinit var bridge: JsBridge
    private var instanceId = ""
    private var cwConversation: String? = null
    private var pendingOpen: Any? = null
    private var pageStarted = false
    private var widgetReady = false
    private var processRecoveryUsed = false
    private var pendingAttributes: Map<String, Any?> = emptyMap()

    init {
        createWebView()
    }

    private fun createWebView() {
        webView = WebViewFactory.create(
            context,
            WidgetWebViewClient(
                context,
                baseUrl = { config?.baseUrl },
                callback = { callback },
                onPageStarted = {
                    pageStarted = true
                    widgetReady = false
                },
                onPageLoaded = { injectUser() },
                onRenderGone = { handleRenderGone(it) }
            ),
            WidgetChromeClient { filePicker }
        )
        bridge = JsBridge(webView) { handleMessage(it) }
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * The render process died. The dead webview is replaced, never reused.
     * Reload once, then report until a page gets ready.
     */
    private fun handleRenderGone(dead: WebView) {
        if (dead !== webView) return
        removeView(dead)
        dead.destroy()
        pageStarted = false
        widgetReady = false
        createWebView()
        if (config != null) bridge.setup()
        if (processRecoveryUsed) {
            callback?.onError(WidgetError(WidgetError.ErrorCode.WEBVIEW_ERROR, "Widget web process died again before ready"))
            return
        }
        processRecoveryUsed = true
        reload()
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
        if (this.config != null) pendingAttributes = emptyMap()
        pendingOpen = null
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
     * @param message Optional string message to display when opening the widget. Null or empty does nothing.
     */
    fun open(message: String? = null) {
        if (!message.isNullOrEmpty()) deliverOpen(message)
    }

    /**
     * Open the widget with a custom message object
     * @param messageData Message object that can contain 'content' key or other properties. Empty does nothing.
     */
    fun open(messageData: Map<String, Any>) {
        if (messageData.isNotEmpty()) deliverOpen(messageData)
    }

    private fun deliverOpen(message: Any) {
        val config = this.config ?: return
        if (widgetReady) {
            sendMessage("open-with-message", mapOf("openMessage" to message))
            return
        }
        pendingOpen = message
        // a load is already running: keep it, the held message goes out at ready
        if (webView.progress !in 1..99) loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation))
    }

    /** Reload the widget. Does nothing before init. */
    fun reload() {
        val config = this.config ?: return
        loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation))
    }

    /**
     * Attach custom attributes to the customer's next message. Repeat calls merge, later keys win.
     * Held until the widget is ready, and sent before a held open message.
     * @return false for a null, empty or unserialisable value
     */
    fun setMessageAttributes(attributes: Map<String, Any?>?): Boolean {
        val valid = !attributes.isNullOrEmpty() && isJsonValue(attributes)
        if (!valid) return false
        pendingAttributes = pendingAttributes + attributes!!
        flushMessageAttributes()
        return true
    }

    private fun isJsonValue(value: Any?): Boolean = when (value) {
        null, is String, is Boolean -> true
        is Double -> value.isFinite()
        is Float -> value.isFinite()
        is Number -> true
        is Map<*, *> -> value.all { (k, v) -> k is String && isJsonValue(v) }
        is Collection<*> -> value.all(::isJsonValue)
        is Array<*> -> value.all(::isJsonValue)
        else -> false
    }

    private fun flushMessageAttributes() {
        if (!widgetReady || pendingAttributes.isEmpty()) return
        sendMessage("set-message-attributes", mapOf("attributes" to pendingAttributes))
        pendingAttributes = emptyMap()
    }

    private fun loadUrl(url: String) {
        Log.d(TAG, "Loading widget URL: ${url.substringBefore('?')}")
        pageStarted = false
        widgetReady = false
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

    private fun userFields(): Map<String, Any> {
        val user = config?.user ?: return emptyMap()
        return buildMap {
            user.name?.let { put("name", it) }
            user.email?.let { put("email", it) }
            user.phoneNumber?.let { put("phone_number", it) }
            user.customAttributes?.let { put("custom_attributes", it) }
        }
    }

    private fun injectUser() {
        val fields = userFields()
        if (fields.isEmpty()) return
        webView.evaluateJavascript(
            "if (window.chatwootWebChannel) { window.chatwootWebChannel.rnUser = ${JSONObject(fields)}; }",
            null
        )
    }

    private fun sendVerifiedUser() {
        val user = config?.user ?: return
        val identifier = user.identifier?.takeIf { it.isNotBlank() } ?: return
        val hash = user.identifierHash?.takeIf { it.isNotBlank() } ?: return
        val fields = buildMap<String, Any> {
            user.name?.let { put("name", it) }
            user.email?.let { put("email", it) }
            put("identifier_hash", hash)
        }
        sendMessage("set-user", mapOf("identifier" to identifier, "user" to fields))
    }

    private fun loadWidget(initialMessage: String?, initialMessageData: Map<String, Any>?) {
        val config = this.config ?: return

        // Bridge must exist before the page loads so the page sees window.LimechatAndroid immediately
        bridge.setup()

        pendingOpen = initialMessage?.takeIf { it.isNotEmpty() } ?: initialMessageData?.takeIf { it.isNotEmpty() }
        loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation))
    }

    private fun handleMessage(data: String) {
        coroutineScope.launch {
            try {
                val processed = messageHandler.processMessage(data)
                processed?.let { message ->
                    when (BridgeEvents.nameOf(message)) {
                        BridgeEvents.CLOSE_WIDGET, BridgeEvents.WIDGET_BACK ->
                            CloseHandler.handle(context, config?.showLegacyBackIcon ?: false, callback)
                        BridgeEvents.SET_CW_CONVERSATION -> {
                            val firstReady = markWidgetReady()
                            try {
                                handleConversationTokenUpdate(message["cw_conversation"] as? String)
                            } finally {
                                callback?.takeIf { firstReady }?.onLoaded()
                            }
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
     * The widget posts set-cw-conversation in the same task that registers its message listener,
     * so this is the first moment a held open message can be received. onPageFinished is too early:
     * hash routing finishes a fragment navigation before the widget mounts.
     * Returns true the first time the widget is ready on this page.
     */
    private fun markWidgetReady(): Boolean {
        if (!pageStarted || widgetReady) return false
        widgetReady = true
        processRecoveryUsed = false
        injectUser()
        sendVerifiedUser()
        flushMessageAttributes()
        pendingOpen?.let { sendMessage("open-with-message", mapOf("openMessage" to it)) }
        pendingOpen = null
        return true
    }

    private fun handleConversationTokenUpdate(token: String?) {
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Received empty conversation token, ignoring")
            return
        }
        
        val config = this.config
        cwConversation = token
        if (config?.onConversationTokenChange != null) {
            config.onConversationTokenChange.invoke(token)
        } else {
            ConversationTokenStore.save(context, instanceId, token)
        }
        
        callback?.onConversationTokenChange(token)
    }
}
