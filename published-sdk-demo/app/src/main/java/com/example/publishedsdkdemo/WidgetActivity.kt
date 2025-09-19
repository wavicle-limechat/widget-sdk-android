package com.example.publishedsdkdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

// Import from published SDK
import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import ai.limechat.widget.WidgetFilePicker
import ai.limechat.widget.models.WidgetConfig

/**
 * Full-screen widget activity using PUBLISHED SDK
 */
class WidgetActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "PublishedWidgetActivity"
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    private val prefs by lazy { getSharedPreferences("limechat_published_demo", MODE_PRIVATE) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "🚀 Opening widget with PUBLISHED SDK")
        
        // Create widget view
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        // Set up file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get configuration from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "MEFFACy4xaovJayhLjSt836h"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@published.com"
        
        // Restore the last persisted conversation token if available
        val cachedConversationToken = prefs.getString("cw_conversation", null)

        // Create configuration
        val config = WidgetConfig(
            websiteToken = websiteToken,
            baseUrl = "https://app.limechat.ai",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "published_sdk_demo",
                "type" to "jitpack_dependency"
            ),
            conversationToken = cachedConversationToken,
            showBackButtonOnLegacyView = true
        )
        
        // Initialize widget with callbacks
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Published SDK widget loaded successfully")
            }
            
            override fun onClose() {
                Log.d(TAG, "🚪 Widget close requested")
                finish()
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "❌ Widget error: ${error.message}")
            }
            
            override fun onMessage(message: Map<String, Any?>) {
                Log.d(TAG, "📨 Widget message: $message")
            }
            
            override fun onConversationTokenChange(conversationToken: String?) {
                Log.d(TAG, "🔄 Conversation token changed: $conversationToken")
                prefs.edit().putString("cw_conversation", conversationToken).apply()
            }
        })
        
        Log.d(TAG, "✅ Widget initialized with published SDK")
    }
    
    override fun onBackPressed() {
        // Let widget handle back first
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetView.isInitialized) {
            widgetView.destroy()
        }
        Log.d(TAG, "🧹 Published SDK widget cleaned up")
    }
}
