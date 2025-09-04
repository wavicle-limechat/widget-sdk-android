package ai.limechat.widget.exceptions

/**
 * Base exception for all widget-related errors
 */
sealed class WidgetException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Thrown when widget initialization fails
     */
    class InitializationException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("Widget initialization failed: $message", cause)
    
    /**
     * Thrown when widget configuration is invalid
     */
    class ConfigurationException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("Invalid widget configuration: $message", cause)
    
    /**
     * Thrown when WebView operations fail
     */
    class WebViewException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("WebView error: $message", cause)
    
    /**
     * Thrown when network operations fail
     */
    class NetworkException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("Network error: $message", cause)
    
    /**
     * Thrown when JavaScript execution fails
     */
    class JavaScriptException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("JavaScript error: $message", cause)
    
    /**
     * Thrown when message processing fails
     */
    class MessageException(
        message: String,
        cause: Throwable? = null
    ) : WidgetException("Message processing error: $message", cause)
}

/**
 * Extension functions for better error handling
 */
fun <T> Result<T>.handleWidgetError(
    onFailure: (WidgetException) -> Unit = {}
): T? {
    return when {
        isSuccess -> getOrNull()
        else -> {
            val exception = exceptionOrNull()
            val widgetException = when (exception) {
                is WidgetException -> exception
                else -> WidgetException.InitializationException(
                    exception?.message ?: "Unknown error",
                    exception
                )
            }
            onFailure(widgetException)
            null
        }
    }
}