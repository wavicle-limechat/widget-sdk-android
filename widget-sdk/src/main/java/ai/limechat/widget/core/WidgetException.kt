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
}
