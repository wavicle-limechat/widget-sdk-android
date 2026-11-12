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
import android.os.SystemClock
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

    companion object {
        private const val TAG = "LimechatWidgetView"

        // a page that finished loading this long ago and is still not ready is treated as failed
        private const val READY_TIMEOUT_MS = 10_000L

        /**
         * Clears the stored conversation token for an instance (null or blank means "default").
         * A view that is showing is not reloaded. Call it on logout.
         */
        @JvmStatic
        @JvmOverloads
        fun reset(context: Context, instanceId: String? = null) {
            ConversationTokenStore.clear(context.applicationContext, instanceId?.takeIf { it.isNotBlank() } ?: "default")
        }
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
    private var loadPending = false
    private var pageFinishedAt: Long? = null

    // monotonic, injectable for tests
    internal var clock: () -> Long = { SystemClock.elapsedRealtime() }
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
                onPageLoaded = {
                    pageFinishedAt = clock()
                    injectUser()
                },
                onRenderGone = { handleRenderGone(it) },
                onMainFrameError = { loadPending = false }
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
        loadPending = false
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

        // a different user than the last one seen on this instance starts a new conversation. A token the host passed still wins.
        val identity = identityOf(config.user)
        val previous = ConversationTokenStore.identity(instanceId)
        if (previous != null && previous != identity) ConversationTokenStore.clear(context, instanceId)
        ConversationTokenStore.setIdentity(instanceId, identity)

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
     * A message object with no `content` and other keys, such as `submitted_values`, gets `content: "Hi"`, as in the web SDK.
     */
    fun open(messageData: Map<String, Any>) {
        if (messageData.isNotEmpty()) deliverOpen(withDefaultContent(messageData))
    }

    /** A message object with no content and other keys gets `content: "Hi"`, as in the web SDK. */
    private fun withDefaultContent(message: Map<String, Any>): Map<String, Any> {
        val content = message["content"]
        val missing = content == null || content === JSONObject.NULL || content == ""
        return if (missing && message.keys.any { it != "content" }) message + ("content" to "Hi") else message
    }

    private fun deliverOpen(message: Any) {
        val config = this.config ?: return
        if (widgetReady) {
            sendMessage("open-with-message", mapOf("openMessage" to message))
            return
        }
        pendingOpen = message
        // the page loaded but the widget never became ready (its js failed): treat the load as failed
        val finishedAt = pageFinishedAt
        if (loadPending && finishedAt != null && clock() - finishedAt >= READY_TIMEOUT_MS) loadPending = false
        // a load is already running: keep it, the held message goes out at ready
        if (!loadPending) loadUrl(UrlBuilder.buildWidgetUrl(config, cwConversation))
    }

    /**
     * `reset()` clears this widget's conversation, like the web SDK's `reset()`: it drops the stored or live conversation token and the user (and a token or user you passed in, until you pass new ones), drops held message attributes and a held open message, and reloads the page without a conversation if it is showing. It does not close the widget. Call it on logout.
     * Does nothing before init.
     */
    fun reset() {
        val config = this.config ?: return
        ConversationTokenStore.clear(context, instanceId)
        cwConversation = null
        this.config = config.copy(conversationToken = null, user = null)
        pendingAttributes = emptyMap()
        pendingOpen = null
        reload()
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
        loadPending = true
        pageFinishedAt = null
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

    private fun identityOf(user: WidgetConfig.User?): String? {
        val parts = listOf(user?.identifier, user?.email, user?.phoneNumber).map { it ?: "" }
        return if (parts.all { it.isEmpty() }) null else parts.joinToString("\n")
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

        pendingOpen = initialMessage?.takeIf { it.isNotEmpty() } ?: initialMessageData?.takeIf { it.isNotEmpty() }?.let { withDefaultContent(it) }
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
                        // the old page can still post after reset() or reload(): only the current page counts
                        BridgeEvents.SET_CW_CONVERSATION -> if (!pageStarted) Unit else {
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
        loadPending = false
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
