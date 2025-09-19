package com.example.localsdkdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

// Import from LOCAL widget-sdk module
import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.WidgetCallback
import ai.limechat.widget.WidgetError
import ai.limechat.widget.WidgetFilePicker
import ai.limechat.widget.models.WidgetConfig

/**
 * Full-screen widget activity using LOCAL SDK
 */
class WidgetActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "LocalWidgetActivity"
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    private var hasHandledCustomMessage = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "🚀 Opening widget with LOCAL SDK")
        
        // Create widget view
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        // Set up file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get configuration from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "MEFFACy4xaovJayhLjSt836h"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@local.com"
        val instanceId = intent.getStringExtra("instance_id") // Get instance ID from intent

        // Create configuration using builder pattern (no token handling - SDK manages internally like React Native)
        val config = WidgetConfig.builder(websiteToken)
            .setBaseUrl("https://app.limechat.ai")
            .setLocale("en")
            .setColorScheme(WidgetConfig.ColorScheme.LIGHT)
            .setUser(WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ))
            .setCustomAttributes(mapOf(
                "source" to "local_sdk_demo",
                "type" to "local_module_dependency"
            ))
            .setInstanceId(instanceId) // Use app-provided instance ID
            .build()
        
        // Check for custom messages and initialize accordingly
        val customMessage = intent.getStringExtra("custom_message")
        @Suppress("UNCHECKED_CAST")
        val customMessageData = intent.getSerializableExtra("custom_message_data") as? Map<String, Any>
        
        // Initialize widget with callbacks and initial message if provided
        when {
            customMessage != null -> {
                Log.d(TAG, "🗨️ Initializing widget with custom string message: $customMessage")
                widgetView.init(config, createWidgetCallback(), customMessage)
                hasHandledCustomMessage = true
            }
            customMessageData != null -> {
                Log.d(TAG, "🗨️ Initializing widget with custom message data: $customMessageData")
                widgetView.init(config, createWidgetCallback(), customMessageData)
                hasHandledCustomMessage = true
            }
            else -> {
                Log.d(TAG, "Initializing widget without custom message")
                widgetView.init(config, createWidgetCallback())
                hasHandledCustomMessage = true
            }
        }
        
        Log.d(TAG, "✅ Widget initialized with local SDK")
    }
    
    private fun createWidgetCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Local SDK widget loaded successfully")
                // No need to handle custom messages here anymore - they're in the URL
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
            
            override fun onConversationTokenChange(token: String) {
                Log.d(TAG, "🔄 Conversation token changed (managed internally): ${token.take(8)}...")
            }
        }
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
        Log.d(TAG, "🧹 Local SDK widget cleaned up")
    }
}
