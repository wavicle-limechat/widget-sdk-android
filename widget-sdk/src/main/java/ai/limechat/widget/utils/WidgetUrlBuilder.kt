package ai.limechat.widget.utils

import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage
import android.util.Log

/**
 * Responsible for building widget URLs with proper parameters
 */
class WidgetUrlBuilder(private val config: WidgetConfig) {
    
    companion object {
        private const val TAG = "WidgetUrlBuilder"
        private const val WIDGET_ENDPOINT = "/widget"
    }
    
    /**
     * Build the base widget URL
     */
    fun buildBaseUrl(): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        return "$baseUrl$WIDGET_ENDPOINT?website_token=${config.websiteToken}"
    }
    
    /**
     * Build URL with initial message
     */
    fun buildUrlWithMessage(message: WidgetMessage): String {
        return try {
            val baseUrl = buildBaseUrl()
            val messageParam = message.toUrlParameter()
            "$baseUrl&$messageParam"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build URL with message", e)
            buildBaseUrl()
        }
    }
    
    /**
     * Build URL with custom parameters
     */
    fun buildUrlWithParams(params: Map<String, String>): String {
        return try {
            val baseUrl = buildBaseUrl()
            val paramString = params.entries.joinToString("&") { (key, value) ->
                "$key=${java.net.URLEncoder.encode(value, "UTF-8")}"
            }
            if (paramString.isNotEmpty()) "$baseUrl&$paramString" else baseUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build URL with params", e)
            buildBaseUrl()
        }
    }
}