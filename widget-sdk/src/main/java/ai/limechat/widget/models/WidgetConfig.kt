package ai.limechat.widget.models

import java.io.Serializable

/**
 * Configuration for the Limechat widget
 */
data class WidgetConfig(
    val websiteToken: String,
    val locale: String = "en",
    val colorScheme: ColorScheme = ColorScheme.LIGHT,
    val user: User? = null,
    val customAttributes: Map<String, Any>? = null,
    val baseUrl: String = "https://app.limechat.ai",
    val conversationToken: String? = null,
    val showBackButtonOnLegacyView: Boolean = false
) : Serializable {
    
    init {
        require(websiteToken.isNotBlank()) { "Website token cannot be blank" }
    }
    
    /**
     * User information to be passed to the widget
     */
    data class User(
        val name: String? = null,
        val email: String? = null,
        val phoneNumber: String? = null,
        val identifierHash: String? = null
    ) : Serializable
    
    /**
     * Color scheme options for the widget
     */
    enum class ColorScheme {
        LIGHT,
        DARK,
        AUTO
    }
}