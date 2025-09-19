package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility class for building widget URLs
 */
object UrlBuilder {
    
    /**
     * Build the complete widget URL with query parameters
     */
    fun buildWidgetUrl(config: WidgetConfig): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        val urlBuilder = StringBuilder("$baseUrl/widget?")
        
        var firstParam = true
        
        fun appendParam(key: String, value: String) {
            if (!firstParam) urlBuilder.append("&")
            urlBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
            urlBuilder.append("=")
            urlBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
            firstParam = false
        }
        
        appendParam("website_token", config.websiteToken)
        appendParam("locale", config.locale)

        val colorScheme = when (config.colorScheme) {
            WidgetConfig.ColorScheme.LIGHT -> "light"
            WidgetConfig.ColorScheme.DARK -> "dark"
            WidgetConfig.ColorScheme.AUTO -> "auto"
        }
        appendParam("color_scheme", colorScheme)

        config.customAttributes
            ?.takeIf { it.isNotEmpty() }
            ?.let { attrs -> appendParam("custom_attributes", JSONObject(attrs).toString()) }

        config.conversationToken
            ?.takeUnless { it.isBlank() }
            ?.let { token -> appendParam("cw_conversation", token) }

        if (config.showBackButtonOnLegacyView) {
            appendParam("show_legacy_back_icon", "true")
        }

        return urlBuilder.toString()
    }

    /**
     * Append encoded query parameters to an existing widget URL.
     */
    fun appendQueryParameters(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url
        
        val separator = if (url.contains("?")) "&" else "?"
        val urlBuilder = StringBuilder(url).append(separator)
        
        var firstParam = true
        
        params.forEach { (key, value) ->
            if (!firstParam) urlBuilder.append("&")
            urlBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
            urlBuilder.append("=")
            urlBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
            firstParam = false
        }
        
        return urlBuilder.toString()
    }
}
