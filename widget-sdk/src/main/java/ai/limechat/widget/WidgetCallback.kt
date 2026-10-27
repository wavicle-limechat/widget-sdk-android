package ai.limechat.widget

/** Callback interface for widget events */
interface WidgetCallback {
    /** Called when the widget has finished loading */
    fun onLoaded()

    /** Called when the widget is closed */
    fun onClose()

    /**
     * Called when an error occurs
     * @param error The error that occurred
     */
    fun onError(error: WidgetError)

    /**
     * Called when a message is received from the widget
     * @param message The message data
     */
    fun onMessage(message: Map<String, Any?>)

    /**
     * Called when the conversation token changes
     * @param token The new conversation token
     */
    fun onConversationTokenChange(token: String) {}
}
