package ai.limechat.widget.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists conversation tokens per website/base URL so the SDK can restore chats automatically.
 */
internal object ConversationTokenStore {
    private const val PREF_NAME = "ai.limechat.widget.conversation_tokens"
    private const val KEY_PREFIX = "cw_conversation"

    private fun sharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun key(websiteToken: String, baseUrl: String): String {
        return "$KEY_PREFIX:${baseUrl.trimEnd('/')}:$websiteToken"
    }

    fun get(context: Context, websiteToken: String, baseUrl: String): String? {
        return sharedPreferences(context).getString(key(websiteToken, baseUrl), null)
    }

    fun save(context: Context, websiteToken: String, baseUrl: String, token: String?) {
        val prefs = sharedPreferences(context)
        val editor = prefs.edit()
        val storageKey = key(websiteToken, baseUrl)
        if (token.isNullOrBlank()) {
            editor.remove(storageKey)
        } else {
            editor.putString(storageKey, token)
        }
        editor.apply()
    }
}
