package ai.limechat.widget.internal

/** Events the widget page sends through the bridge, keyed by `event` (or `type`). */
internal object BridgeEvents {
    const val CLOSE_WIDGET = "close-widget"
    const val SET_CW_CONVERSATION = "set-cw-conversation"

    fun nameOf(message: Map<String, Any?>): String =
        message["event"] as? String ?: message["type"] as? String ?: "unknown"
}
