package ai.limechat.widget.models

import ai.limechat.widget.core.WidgetException
import java.io.Serializable
import java.net.URL

/**
 * Immutable configuration for the Limechat widget
 * 
 * Use WidgetConfig.Builder to create instances with validation
 * 
 * Example:
 * ```
 * val config = WidgetConfig.Builder("your-website-token")
 *     .setInstanceId("my-widget-1")
 *     .setUser(user)
 *     .setColorScheme(ColorScheme.DARK)
 *     .build()
 * ```
 */
data class WidgetConfig internal constructor(
    val websiteToken: String,
    val locale: String,
    val colorScheme: ColorScheme,
    val user: User?,
    val customAttributes: Map<String, Any>?,
    val baseUrl: String,
    val conversationToken: String?,
    val onConversationTokenChange: ((String) -> Unit)?,
    val instanceId: String?
) : Serializable {
    
    /**
     * User information to be passed to the widget
     */
    data class User(
        val name: String? = null,
        val email: String? = null,
        val phoneNumber: String? = null,
        val identifierHash: String? = null,
        val customAttributes: Map<String, Any>? = null
    ) : Serializable {
        
        /**
         * Check if user has any identifying information
         */
        fun hasIdentifyingInfo(): Boolean {
            return !name.isNullOrBlank() || 
                   !email.isNullOrBlank() || 
                   !phoneNumber.isNullOrBlank() || 
                   !identifierHash.isNullOrBlank()
        }
        
        /**
         * Get primary identifier for the user
         */
        fun getPrimaryIdentifier(): String? {
            return identifierHash ?: email ?: phoneNumber ?: name
        }
    }
    
    /**
     * Color scheme options for the widget
     */
    enum class ColorScheme {
        LIGHT,
        DARK,
        AUTO
    }
    
    /**
     * Token management mode
     */
    enum class TokenManagement {
        /** SDK manages tokens internally using instanceId */
        INTERNAL,
        /** App manages tokens externally via callbacks */
        EXTERNAL
    }
    
    /**
     * Get the token management mode based on configuration
     */
    val tokenManagement: TokenManagement
        get() = if (conversationToken != null || onConversationTokenChange != null) {
            TokenManagement.EXTERNAL
        } else {
            TokenManagement.INTERNAL
        }
    
    /**
     * Check if configuration is valid for external token management
     */
    val isExternalTokenManagementValid: Boolean
        get() = tokenManagement == TokenManagement.INTERNAL || 
                (conversationToken != null && onConversationTokenChange != null)
    
    /**
     * Builder for WidgetConfig with validation
     */
    class Builder(private val websiteToken: String) {
        private var locale: String = "en"
        private var colorScheme: ColorScheme = ColorScheme.LIGHT
        private var user: User? = null
        private var customAttributes: Map<String, Any>? = null
        private var baseUrl: String = "https://app.limechat.ai"
        private var conversationToken: String? = null
        private var onConversationTokenChange: ((String) -> Unit)? = null
        private var instanceId: String? = null
        
        /**
         * Set the locale for the widget
         */
        fun setLocale(locale: String): Builder {
            this.locale = locale
            return this
        }
        
        /**
         * Set the color scheme for the widget
         */
        fun setColorScheme(colorScheme: ColorScheme): Builder {
            this.colorScheme = colorScheme
            return this
        }
        
        /**
         * Set user information
         */
        fun setUser(user: User?): Builder {
            this.user = user
            return this
        }
        
        /**
         * Set custom attributes
         */
        fun setCustomAttributes(attributes: Map<String, Any>?): Builder {
            this.customAttributes = attributes
            return this
        }
        
        /**
         * Set base URL for the widget
         */
        fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }
        
        /**
         * Set external conversation token management
         * Both token and callback must be provided for external management
         */
        fun setExternalTokenManagement(
            token: String?,
            onTokenChange: ((String) -> Unit)?
        ): Builder {
            this.conversationToken = token
            this.onConversationTokenChange = onTokenChange
            return this
        }
        
        /**
         * Set instance ID for widget isolation
         * If not set, SDK will use default behavior
         */
        fun setInstanceId(instanceId: String?): Builder {
            this.instanceId = instanceId
            return this
        }
        
        /**
         * Build and validate the configuration
         * 
         * @throws WidgetException.ConfigurationError if configuration is invalid
         */
        fun build(): WidgetConfig {
            validateConfiguration()
            
            return WidgetConfig(
                websiteToken = websiteToken,
                locale = locale,
                colorScheme = colorScheme,
                user = user,
                customAttributes = customAttributes?.toMap(), // Defensive copy
                baseUrl = baseUrl,
                conversationToken = conversationToken,
                onConversationTokenChange = onConversationTokenChange,
                instanceId = instanceId
            )
        }
        
        private fun validateConfiguration() {
            // Validate website token
            if (websiteToken.isBlank()) {
                throw WidgetException.ConfigurationError("Website token cannot be blank")
            }
            
            // Validate base URL
            try {
                URL(baseUrl)
            } catch (e: Exception) {
                throw WidgetException.ConfigurationError("Invalid base URL: $baseUrl", e)
            }
            
            // Validate locale
            if (locale.isBlank()) {
                throw WidgetException.ConfigurationError("Locale cannot be blank")
            }
            
            // Validate external token management
            val hasToken = conversationToken != null
            val hasCallback = onConversationTokenChange != null
            
            if (hasToken && !hasCallback) {
                throw WidgetException.ConfigurationError(
                    "External token management requires both token and callback"
                )
            }
            
            if (!hasToken && hasCallback) {
                throw WidgetException.ConfigurationError(
                    "External token management requires both token and callback"
                )
            }
            
            // Validate instance ID format if provided
            instanceId?.let { id ->
                if (id.isBlank()) {
                    throw WidgetException.ConfigurationError("Instance ID cannot be blank")
                }
                if (id.contains(":") || id.contains("/")) {
                    throw WidgetException.ConfigurationError(
                        "Instance ID cannot contain ':' or '/' characters"
                    )
                }
            }
        }
    }
    
    companion object {
        /**
         * Create a builder for WidgetConfig
         */
        fun builder(websiteToken: String): Builder = Builder(websiteToken)
        
        /**
         * Create a simple configuration with just website token
         * Uses all default values
         */
        fun simple(websiteToken: String): WidgetConfig = builder(websiteToken).build()
        
        /**
         * Create configuration with instance ID for conversation isolation
         */
        fun withInstanceId(websiteToken: String, instanceId: String): WidgetConfig =
            builder(websiteToken).setInstanceId(instanceId).build()
    }
}