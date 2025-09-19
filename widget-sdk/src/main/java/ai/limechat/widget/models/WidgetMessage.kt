package ai.limechat.widget.models

import org.json.JSONObject
import java.net.URLEncoder

/**
 * Represents a message to be sent to the widget
 */
sealed class WidgetMessage {
    
    /**
     * Simple string message
     */
    data class Text(val content: String) : WidgetMessage()
    
    /**
     * Structured message with additional properties
     */
    data class Structured(
        val content: String,
        val properties: Map<String, Any> = emptyMap()
    ) : WidgetMessage()
    
    /**
     * Convert message to URL parameter format
     */
    fun toUrlParameter(): String {
        val (key, value) = toQueryParameters().entries.first()
        return "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
    }

    /**
     * Convert message to raw query parameters without encoding
     */
    fun toQueryParameters(): Map<String, String> {
        return when (this) {
            is Text -> mapOf("lc_open_message" to content)
            is Structured -> {
                val payload = mutableMapOf<String, Any>("content" to content)
                payload.putAll(properties)
                val json = JSONObject(payload as Map<String, Any>).toString()
                mapOf("lc_open_payload" to json)
            }
        }
    }
    
    /**
     * Get the display content of the message
     */
    fun getMessageContent(): String {
        return when (this) {
            is Text -> content
            is Structured -> content
        }
    }
}
