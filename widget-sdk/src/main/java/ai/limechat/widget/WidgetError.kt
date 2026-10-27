package ai.limechat.widget

/** Widget error class */
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
