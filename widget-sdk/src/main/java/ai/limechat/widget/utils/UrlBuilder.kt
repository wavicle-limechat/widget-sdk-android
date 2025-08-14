package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Utility class for building widget URLs
 */
object UrlBuilder {
    
    /**
     * Build the complete widget URL with query parameters
     */
    fun buildWidgetUrl(config: WidgetConfig): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        val params = mutableMapOf<String, String>()
        
        // Required parameters
        params["website_token"] = config.websiteToken
        params["locale"] = config.locale
        
        // Color scheme
        params["color_scheme"] = when (config.colorScheme) {
            WidgetConfig.ColorScheme.LIGHT -> "light"
            WidgetConfig.ColorScheme.DARK -> "dark"
            WidgetConfig.ColorScheme.AUTO -> "auto"
        }
        
        // Custom attributes
        config.customAttributes?.let { attrs ->
            if (attrs.isNotEmpty()) {
                params["custom_attributes"] = JSONObject(attrs).toString()
            }
        }
        
        return buildUrl(baseUrl, "widget", params)
    }
    
    private fun buildUrl(baseUrl: String, path: String, params: Map<String, String>): String {
        val url = StringBuilder("$baseUrl/$path")
        
        if (params.isNotEmpty()) {
            url.append("?")
            val queryParams = params.map { (key, value) ->
                "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
            }
            url.append(queryParams.joinToString("&"))
        }
        
        return url.toString()
    }
}