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
    class NetworkError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * JavaScript execution error
     */
    class JavaScriptError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * WebView initialization error
     */
    class WebViewError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * File picker operation error
     */
    class FilePickerError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * Conversation persistence error
     */
    class PersistenceError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
    
    /**
     * Unknown or unexpected error
     */
    class UnknownError(message: String, cause: Throwable? = null) : WidgetException(message, cause)
}
