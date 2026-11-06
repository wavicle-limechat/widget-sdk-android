package ai.limechat.widget.utils

import org.json.JSONObject

/** Utility class for handling messages from the widget */
class MessageHandler {

    /**
     * Process a message from the WebView
     * @param data Raw message data
     * @return Processed message map or null if invalid
     */
    fun processMessage(data: String): Map<String, Any?>? {
        return try {
            jsonToMap(JSONObject(data))
        } catch (e: Exception) {
            null
        }
    }

    /** Convert JSONObject to Map recursively */
    private fun jsonToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)

            map[key] =
                    when (value) {
                        is JSONObject -> jsonToMap(value)
                        JSONObject.NULL -> null
                        else -> value
                    }
        }

        return map
    }
}
