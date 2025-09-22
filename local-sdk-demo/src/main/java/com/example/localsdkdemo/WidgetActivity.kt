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
        val showLegacyBackIcon = intent.getBooleanExtra("show_legacy_back_icon", true) // Default to true for demo
        
        Log.d(TAG, "🔧 Widget configuration:")
        Log.d(TAG, "   - Instance ID: $instanceId")
        Log.d(TAG, "   - Show Legacy Back Icon: $showLegacyBackIcon")

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
            .setShowLegacyBackIcon(showLegacyBackIcon) // Use flag from intent
            .build()
        
        // Initialize widget based on legacy back icon setting
        if (showLegacyBackIcon) {
            // Plug-and-play mode - no callback needed
            Log.d(TAG, "🔌 PLUG-AND-PLAY MODE: Legacy back icon will auto-close activity!")
            widgetView.init(config)
        } else {
            // Traditional mode - callback required for minimize button
            Log.d(TAG, "🔧 TRADITIONAL MODE: Using WidgetCallback for close handling")
            widgetView.init(config, createWidgetCallback())
        }
        
        Log.d(TAG, "✅ Widget initialized with local SDK")
    }
    
    private fun createWidgetCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Local SDK widget loaded successfully")
            }
            
            override fun onClose() {
                Log.d(TAG, "🚪 Widget close requested via callback (minimize button clicked)")
                finish()
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "❌ Widget error: ${error.message}")
            }
            
            override fun onMessage(message: Map<String, Any?>) {
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
