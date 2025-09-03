package ai.limechat.widget.models

import org.json.JSONObject

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
        return when (this) {
            is Text -> "lc_open_message=${java.net.URLEncoder.encode(content, "UTF-8")}"
            is Structured -> {
                val payload = mutableMapOf<String, Any>("content" to content)
                payload.putAll(properties)
                val json = JSONObject(payload as Map<String, Any>).toString()
                "lc_open_payload=${java.net.URLEncoder.encode(json, "UTF-8")}"
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