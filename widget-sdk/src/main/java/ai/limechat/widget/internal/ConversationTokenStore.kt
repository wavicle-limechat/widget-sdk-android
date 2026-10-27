package ai.limechat.widget.internal

import android.content.Context

/**
 * Conversation token persistence for internal token management.
 * Prefs file and keys are part of the contract: changing them loses existing users' conversations.
 */
internal object ConversationTokenStore {
    private const val PREFS_NAME = "limechat_widget_conversations"

    // survives widget recreation within the same app process
    private val cache = mutableMapOf<String, String>()

    fun load(context: Context, instanceId: String): String? {
        cache[instanceId]?.let { return it }

        val token = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(instanceId, null)
        token?.let { cache[instanceId] = it }
        return token
    }

    fun save(context: Context, instanceId: String, token: String) {
        cache[instanceId] = token
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(instanceId, token).apply()
    }
}
