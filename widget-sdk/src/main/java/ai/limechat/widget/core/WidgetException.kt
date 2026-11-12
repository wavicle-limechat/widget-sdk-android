package ai.limechat.widget.core

/**
 * Widget-specific exceptions with structured error information
 */
sealed class WidgetException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Configuration validation error
     */
    class ConfigurationError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * Network-related error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class NetworkError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * JavaScript execution error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class JavaScriptError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * WebView initialization error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class WebViewError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * File picker operation error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class FilePickerError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * Conversation persistence error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class PersistenceError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * Unknown or unexpected error
     */
    @Deprecated("Unused by the SDK. Will be removed in the next major version.")
    class UnknownError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
}
