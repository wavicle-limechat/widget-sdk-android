package ai.limechat.widget

/** Widget error class */
data class WidgetError(
        val code: ErrorCode,
        override val message: String,
        val originalError: Throwable? = null,
        val context: Map<String, Any>? = null
) : Exception(message, originalError) {

    enum class ErrorCode {
        @Deprecated("Unused. Removed at the next major.")
        CONFIG_ERROR,
        WEBVIEW_ERROR,
        @Deprecated("Unused. Removed at the next major.")
        NETWORK_ERROR,
        JAVASCRIPT_ERROR,
        @Deprecated("Unused. Removed at the next major.")
        FILE_PICKER_ERROR,
        @Deprecated("Unused. Removed at the next major.")
        INITIALIZATION_ERROR,
        @Deprecated("Unused. Removed at the next major.")
        UNKNOWN_ERROR
    }
}
