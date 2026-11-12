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
     * @param openMessage Unused by the SDK. Will be removed in the next major release.
     * @param openPayload Unused by the SDK. Will be removed in the next major release.
     */
    @Suppress("DEPRECATION")
    @JvmOverloads
    fun buildWidgetUrl(
        config: WidgetConfig,
        cwConversation: String? = null,
        openMessage: String? = null,
        openPayload: String? = null
    ): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        val params = mutableMapOf<String, String>()
        
        params["website_token"] = config.websiteToken
        params["platform"] = "android"
        params["locale"] = config.locale
        
        params["color_scheme"] = when (config.colorScheme) {
            WidgetConfig.ColorScheme.LIGHT -> "light"
            WidgetConfig.ColorScheme.DARK -> "dark"
            WidgetConfig.ColorScheme.AUTO -> "auto"
        }
        
        config.customAttributes?.let { attrs ->
            if (attrs.isNotEmpty()) {
                params["custom_attributes"] = JSONObject(attrs).toString()
            }
        }
        
        val conversationToken = cwConversation ?: config.conversationToken
        conversationToken?.let { token ->
            if (token.isNotBlank()) {
                params["cw_conversation"] = token
            }
        }
        
        if (config.hideFilePicker) params["hide_file_picker"] = "true"

        openMessage?.let { params["lc_open_message"] = it.replace("%", "%25") }
        openPayload?.let { params["lc_open_payload"] = it.replace("%", "\\u0025") }
        
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