package ai.limechat.widget.core

import ai.limechat.widget.models.WidgetConfig
import android.util.Log

/**
 * Centralized logging utility for the widget SDK
 * Provides structured logging with consistent formatting and debug levels
 * 
 * Features:
 * - Structured log messages with context
 * - Performance timing utilities
 * - Debug mode support
 * - Consistent tag formatting
 */
object WidgetLogger {
    
    private const val BASE_TAG = "LimechatWidget"
    private var isDebugMode = false
    
    /**
     * Enable or disable debug mode
     * In debug mode, additional verbose logs are shown
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
        i("Logger", "Debug mode ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Log debug message (only shown in debug mode)
     */
    fun d(component: String, message: String, instanceId: String? = null) {
        if (isDebugMode) {
            Log.d(createTag(component), formatMessage(message, instanceId))
        }
    }
    
    /**
     * Log info message
     */
    fun i(component: String, message: String, instanceId: String? = null) {
        Log.i(createTag(component), formatMessage(message, instanceId))
    }
    
    /**
     * Log warning message
     */
    fun w(component: String, message: String, instanceId: String? = null, throwable: Throwable? = null) {
        Log.w(createTag(component), formatMessage(message, instanceId), throwable)
    }
    
    /**
     * Log error message
     */
    fun e(component: String, message: String, instanceId: String? = null, throwable: Throwable? = null) {
        Log.e(createTag(component), formatMessage(message, instanceId), throwable)
    }
    
    /**
     * Log verbose message (only shown in debug mode)
     */
    fun v(component: String, message: String, instanceId: String? = null) {
        if (isDebugMode) {
            Log.v(createTag(component), formatMessage(message, instanceId))
        }
    }
    
    /**
     * Log widget lifecycle event
     */
    fun lifecycle(component: String, event: String, instanceId: String? = null) {
        i(component, "🔄 $event", instanceId)
    }
    
    /**
     * Log widget initialization
     */
    fun init(component: String, message: String, instanceId: String? = null) {
        i(component, "🚀 $message", instanceId)
    }
    
    /**
     * Log widget success event
     */
    fun success(component: String, message: String, instanceId: String? = null) {
        i(component, "✅ $message", instanceId)
    }
    
    /**
     * Log widget error event
     */
    fun error(component: String, message: String, instanceId: String? = null, throwable: Throwable? = null) {
        e(component, "❌ $message", instanceId, throwable)
    }
    
    /**
     * Log widget warning event
     */
    fun warning(component: String, message: String, instanceId: String? = null) {
        w(component, "⚠️ $message", instanceId)
    }
    
    /**
     * Log network operation
     */
    fun network(component: String, message: String, instanceId: String? = null) {
        d(component, "🌐 $message", instanceId)
    }
    
    /**
     * Log persistence operation
     */
    fun persistence(component: String, message: String, instanceId: String? = null) {
        d(component, "💾 $message", instanceId)
    }
    
    /**
     * Log JavaScript operation
     */
    fun javascript(component: String, message: String, instanceId: String? = null) {
        d(component, "🔧 $message", instanceId)
    }
    
    /**
     * Performance timing utility
     */
    class Timer(private val component: String, private val operation: String, private val instanceId: String? = null) {
        private val startTime = System.currentTimeMillis()
        
        /**
         * Stop timer and log the duration
         */
        fun stop() {
            val duration = System.currentTimeMillis() - startTime
            d(component, "⏱️ $operation completed in ${duration}ms", instanceId)
        }
    }
    
    /**
     * Start performance timer
     */
    fun startTimer(component: String, operation: String, instanceId: String? = null): Timer {
        d(component, "⏱️ Starting $operation", instanceId)
        return Timer(component, operation, instanceId)
    }
    
    /**
     * Log widget configuration
     */
    fun logConfig(config: WidgetConfig, instanceId: String? = null) {
        val component = "Config"
        d(component, "Website token: ${config.websiteToken.take(8)}...", instanceId)
        d(component, "Base URL: ${config.baseUrl}", instanceId)
        d(component, "Locale: ${config.locale}", instanceId)
        d(component, "Color scheme: ${config.colorScheme}", instanceId)
        d(component, "Token management: ${config.tokenManagement}", instanceId)
        d(component, "Instance ID: ${config.instanceId ?: "auto-generated"}", instanceId)
        config.user?.let { user ->
            d(component, "User: ${user.getPrimaryIdentifier() ?: "anonymous"}", instanceId)
        }
    }
    
    private fun createTag(component: String): String {
        return "$BASE_TAG:$component"
    }
    
    private fun formatMessage(message: String, instanceId: String?): String {
        return if (instanceId != null) {
            "[$instanceId] $message"
        } else {
            message
        }
    }
}
