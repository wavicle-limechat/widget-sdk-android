package ai.limechat.widget

/**
 * Callback interface for widget events
 */
interface WidgetCallback {
    /**
     * Called when the widget has finished loading
     */
    fun onLoaded()
    
    /**
     * Called when the widget is closed
     */
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
    fun onMessage(message: Map<String, Any>)
}

/**
 * Widget error class
 */
data class WidgetError(
    val code: ErrorCode,
    override val message: String,
    val originalError: Throwable? = null,
    val context: Map<String, Any>? = null
) : Exception(message, originalError) {
    
    enum class ErrorCode {
        CONFIG_ERROR,
        WEBVIEW_ERROR,
        NETWORK_ERROR,
        JAVASCRIPT_ERROR,
        FILE_PICKER_ERROR,
        INITIALIZATION_ERROR,
        UNKNOWN_ERROR
    }
}