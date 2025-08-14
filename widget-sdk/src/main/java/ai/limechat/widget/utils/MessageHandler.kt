package ai.limechat.widget.utils

import org.json.JSONException
import org.json.JSONObject

/**
 * Utility class for handling messages from the widget
 */
class MessageHandler {
    
    companion object {
        private const val MESSAGE_PREFIX = "limechat-widget:"
    }
    
    /**
     * Process a message from the WebView
     * @param data Raw message data
     * @return Processed message map or null if invalid
     */
    fun processMessage(data: String): Map<String, Any>? {
        return try {
            val message = extractMessage(data)
            if (isJsonString(message)) {
                parseJsonMessage(message)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extract message content by removing prefix
     */
    private fun extractMessage(data: String): String {
        return if (data.startsWith(MESSAGE_PREFIX)) {
            data.removePrefix(MESSAGE_PREFIX)
        } else {
            data
        }
    }
    
    /**
     * Check if string is valid JSON
     */
    private fun isJsonString(string: String): Boolean {
        return try {
            JSONObject(string)
            true
        } catch (e: JSONException) {
            false
        }
    }
    
    /**
     * Parse JSON message into a map
     */
    private fun parseJsonMessage(message: String): Map<String, Any>? {
        return try {
            val json = JSONObject(message)
            jsonToMap(json)
        } catch (e: JSONException) {
            null
        }
    }
    
    /**
     * Convert JSONObject to Map recursively
     */
    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            
            map[key] = when (value) {
                is JSONObject -> jsonToMap(value)
                JSONObject.NULL -> null
                else -> value
            } ?: continue
        }
        
        return map
    }
}