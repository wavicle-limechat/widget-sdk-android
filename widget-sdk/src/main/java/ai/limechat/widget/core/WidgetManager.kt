package ai.limechat.widget.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ai.limechat.widget.models.WidgetConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized manager for widget conversation persistence and lifecycle
 * 
 * Responsibilities:
 * - Manage conversation tokens per widget instance
 * - Handle cross-app persistence using SharedPreferences
 * - Provide thread-safe operations
 * - Manage widget instance lifecycle
 * 
 * Architecture:
 * - Two-tier caching: In-memory (performance) + Persistent (durability)
 * - Instance-based isolation using instanceId
 * - Thread-safe operations for concurrent access
 */
class WidgetManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "WidgetManager"
        private const val PREFS_NAME = "limechat_widget_conversations"
        private const val PREFS_KEY_PREFIX = "conversation_"
        
        @Volatile
        private var INSTANCE: WidgetManager? = null
        
        /**
         * Get singleton instance of WidgetManager
         * Thread-safe initialization
         */
        fun getInstance(context: Context): WidgetManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WidgetManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Thread-safe in-memory cache for performance
    private val memoryCache = ConcurrentHashMap<String, String>()
    
    // SharedPreferences for persistent storage
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Mutex for thread-safe persistence operations
    private val persistenceMutex = Mutex()
    
    /**
     * Load conversation token for a widget instance
     * 
     * @param instanceId Unique identifier for widget instance
     * @return Conversation token if exists, null otherwise
     */
    suspend fun loadConversationToken(instanceId: String): String? {
        // First check memory cache for performance
        memoryCache[instanceId]?.let { token ->
            Log.d(TAG, "Token loaded from memory cache for instance: $instanceId")
            return token
        }
        
        // Load from persistent storage
        return persistenceMutex.withLock {
            val key = PREFS_KEY_PREFIX + instanceId
            val token = prefs.getString(key, null)
            
            // Cache in memory for future access
            token?.let { memoryCache[instanceId] = it }
            
            Log.d(TAG, "Token loaded from persistent storage for instance: $instanceId - ${token?.take(8) ?: "none"}")
            token
        }
    }
    
    /**
     * Save conversation token for a widget instance
     * 
     * @param instanceId Unique identifier for widget instance
     * @param token Conversation token to save
     */
    suspend fun saveConversationToken(instanceId: String, token: String) {
        // Update memory cache immediately
        memoryCache[instanceId] = token
        
        // Persist to storage asynchronously
        persistenceMutex.withLock {
            val key = PREFS_KEY_PREFIX + instanceId
            prefs.edit().putString(key, token).apply()
            
            Log.d(TAG, "Token saved for instance: $instanceId - ${token.take(8)}...")
        }
    }
    
    /**
     * Clear conversation for a widget instance
     * 
     * @param instanceId Unique identifier for widget instance
     */
    suspend fun clearConversation(instanceId: String) {
        memoryCache.remove(instanceId)
        
        persistenceMutex.withLock {
            val key = PREFS_KEY_PREFIX + instanceId
            prefs.edit().remove(key).apply()
            
            Log.d(TAG, "Conversation cleared for instance: $instanceId")
        }
    }
    
    /**
     * Get all stored conversation instances
     * Useful for debugging and analytics
     */
    fun getAllConversationInstances(): Set<String> {
        return prefs.all.keys
            .filter { it.startsWith(PREFS_KEY_PREFIX) }
            .map { it.removePrefix(PREFS_KEY_PREFIX) }
            .toSet()
    }
    
    /**
     * Clear all conversations
     * Useful for logout or reset functionality
     */
    suspend fun clearAllConversations() {
        memoryCache.clear()
        
        persistenceMutex.withLock {
            val editor = prefs.edit()
            prefs.all.keys
                .filter { it.startsWith(PREFS_KEY_PREFIX) }
                .forEach { editor.remove(it) }
            editor.apply()
            
            Log.d(TAG, "All conversations cleared")
        }
    }
    
    /**
     * Generate a unique instance ID if not provided by the app
     * 
     * @param config Widget configuration
     * @return Unique instance ID
     */
    fun generateInstanceId(config: WidgetConfig): String {
        return config.instanceId ?: "default_${config.websiteToken.take(8)}"
    }
    
    /**
     * Validate widget configuration
     * 
     * @param config Widget configuration to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    fun validateConfig(config: WidgetConfig) {
        require(config.websiteToken.isNotBlank()) { "Website token cannot be blank" }
        require(config.baseUrl.isNotBlank()) { "Base URL cannot be blank" }
        
        // Validate URL format
        try {
            java.net.URL(config.baseUrl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid base URL: ${config.baseUrl}", e)
        }
    }
}
