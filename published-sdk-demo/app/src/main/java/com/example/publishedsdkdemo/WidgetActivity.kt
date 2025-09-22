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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "Opening widget with PUBLISHED SDK")
        
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        val websiteToken = intent.getStringExtra("website_token") ?: "MEFFACy4xaovJayhLjSt836h"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@published.com"
        val instanceId = intent.getStringExtra("instance_id") ?: "published-sdk-demo"
        
        val config = WidgetConfig.builder(websiteToken)
            .setLocale("en")
            .setColorScheme(WidgetConfig.ColorScheme.LIGHT)
            .setUser(WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ))
            .setInstanceId(instanceId)
            .setShowLegacyBackIcon(true)
            .build()
        
        // Initialize widget - SDK automatically handles close events when showLegacyBackIcon is enabled
        widgetView.init(config)
        
        Log.d(TAG, "Widget initialized with plug-and-play back button")
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
    }
}