package ai.limechat.widget.models

import org.json.JSONObject

/**
 * Response from the widget_config API endpoint
 */
data class WidgetConfigResponse(
    val config: ConfigData?
) {
    data class ConfigData(
        val widgetIconMobile: String? = null,
        val widgetIconDesktop: String? = null,
        val widgetBubbleText: String? = null,
        val welcomeTitle: String? = null,
        val welcomeTagline: String? = null,
        val replyTime: String? = null,
        val preChatFormEnabled: Boolean = false,
        val preChatFormOptions: Map<String, Any>? = null,
        val brandName: String? = null,
        val brandUrl: String? = null,
        val hideMessageBubbleText: Boolean = false,
        val hideInputFieldText: Boolean = false,
        val showPoweredBy: Boolean = true
    ) {
        companion object {
            /**
             * Parse ConfigData from JSON
             */
            fun fromJson(json: JSONObject): ConfigData {
                return ConfigData(
                    widgetIconMobile = json.optString("widgetIconMobile").takeIf { it.isNotEmpty() },
                    widgetIconDesktop = json.optString("widgetIconDesktop").takeIf { it.isNotEmpty() },
                    widgetBubbleText = json.optString("widgetBubbleText").takeIf { it.isNotEmpty() },
                    welcomeTitle = json.optString("welcomeTitle").takeIf { it.isNotEmpty() },
                    welcomeTagline = json.optString("welcomeTagline").takeIf { it.isNotEmpty() },
                    replyTime = json.optString("replyTime").takeIf { it.isNotEmpty() },
                    preChatFormEnabled = json.optBoolean("preChatFormEnabled"),
                    brandName = json.optString("brandName").takeIf { it.isNotEmpty() },
                    brandUrl = json.optString("brandUrl").takeIf { it.isNotEmpty() },
                    hideMessageBubbleText = json.optBoolean("hideMessageBubbleText"),
                    hideInputFieldText = json.optBoolean("hideInputFieldText"),
                    showPoweredBy = json.optBoolean("showPoweredBy", true)
                )
            }
        }
    }

    companion object {
        /**
         * Parse WidgetConfigResponse from JSON
         */
        fun fromJson(json: JSONObject): WidgetConfigResponse {
            val configJson = json.optJSONObject("config") ?: json.optString("config").let { configStr ->
                if (configStr.isNotEmpty()) {
                    try {
                        JSONObject(configStr)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
            
            return WidgetConfigResponse(
                config = configJson?.let { ConfigData.fromJson(it) }
            )
        }
    }
}