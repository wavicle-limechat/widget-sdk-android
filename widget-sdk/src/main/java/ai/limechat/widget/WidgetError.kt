package ai.limechat.widget

/** Widget error class */
data class WidgetError(
        val code: ErrorCode,
        override val message: String,
        val originalError: Throwable? = null,
        val context: Map<String, Any>? = null
) : Exception(message, originalError) {

    enum class ErrorCode {
        @Deprecated("Unused. Will be removed in the next major release.")
        CONFIG_ERROR,
        WEBVIEW_ERROR,
        @Deprecated("Unused. Will be removed in the next major release.")
        NETWORK_ERROR,
        JAVASCRIPT_ERROR,
        @Deprecated("Unused. Will be removed in the next major release.")
        FILE_PICKER_ERROR,
        @Deprecated("Unused. Will be removed in the next major release.")
        INITIALIZATION_ERROR,
        @Deprecated("Unused. Will be removed in the next major release.")
        UNKNOWN_ERROR
    }
}
