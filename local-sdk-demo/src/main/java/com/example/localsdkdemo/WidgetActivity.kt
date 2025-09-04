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
        val websiteToken = intent.getStringExtra("website_token") ?: "PN5LeU9Cyng1CRiCXTGNMm3x"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@local.com"
        
        // Create configuration
        val config = WidgetConfig(
            websiteToken = websiteToken,
            baseUrl = "https://cf2e01f8e319.ngrok-free.app",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "local_sdk_demo",
                "type" to "local_module_dependency"
            )
        )
        
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
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d(TAG, "📨 Widget message: $message")
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
