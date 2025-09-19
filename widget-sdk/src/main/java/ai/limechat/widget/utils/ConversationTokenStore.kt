package ai.limechat.widget.utils

import android.content.Context

/**
 * Internal storage for cw_conversation tokens when the host app does not manage them.
 */
internal object ConversationTokenStore {

    private const val PREFS_NAME = "ai.limechat.widget.conversation_store"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getToken(context: Context, key: String): String? {
        return prefs(context).getString(key, null)
    }

    fun setToken(context: Context, key: String, token: String) {
        if (token.isBlank()) return
        prefs(context).edit().putString(key, token).apply()
    }

    fun clearToken(context: Context, key: String) {
        prefs(context).edit().remove(key).apply()
    }
}
