package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import android.net.Uri
import org.json.JSONObject

/**
 * Utility class for building widget URLs
 */
object UrlBuilder {
    
    /**
     * Build the complete widget URL with query parameters
     */
    fun buildWidgetUrl(config: WidgetConfig): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        val builder = Uri.parse("$baseUrl/widget").buildUpon()

        builder.appendQueryParameter("website_token", config.websiteToken)
        builder.appendQueryParameter("locale", config.locale)

        val colorScheme = when (config.colorScheme) {
            WidgetConfig.ColorScheme.LIGHT -> "light"
            WidgetConfig.ColorScheme.DARK -> "dark"
            WidgetConfig.ColorScheme.AUTO -> "auto"
        }
        builder.appendQueryParameter("color_scheme", colorScheme)

        config.customAttributes
            ?.takeIf { it.isNotEmpty() }
            ?.let { attrs -> builder.appendQueryParameter("custom_attributes", JSONObject(attrs).toString()) }

        config.conversationToken
            ?.takeUnless { it.isBlank() }
            ?.let { token -> builder.appendQueryParameter("cw_conversation", token) }

        if (config.showBackButtonOnLegacyView) {
            builder.appendQueryParameter("show_legacy_back_icon", "true")
        }

        return builder.build().toString()
    }

    /**
     * Append encoded query parameters to an existing widget URL.
     */
    fun appendQueryParameters(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url

        val uri = Uri.parse(url)
        val builder = uri.buildUpon()

        params.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }

        return builder.build().toString()
    }
}
