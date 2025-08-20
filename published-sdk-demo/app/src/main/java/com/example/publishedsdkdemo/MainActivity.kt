package com.example.publishedsdkdemo

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Import ONLY from the published SDK - no local references
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

/**
 * Demo app using ONLY the published JitPack SDK
 * 
 * Dependencies: com.github.wavicle-limechat:widget-sdk-android:0.0.1
 * 
 * This demonstrates that the SDK works completely independently
 * without any local project dependencies.
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "PublishedSDKDemo"
        private const val WEBSITE_TOKEN = "YOUR_WEBSITE_TOKEN"
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private var badgeCount = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 Starting Published SDK Demo")
        Log.d(TAG, "📦 Using: com.github.wavicle-limechat:widget-sdk-android:0.0.3")
        
        // Get actual SDK version to prove we're using published version
        val sdkVersion = LimechatWidgetButton.getSDKVersion()
        Log.d(TAG, "🔍 SDK Version from JitPack: $sdkVersion")
        
        // Display SDK info in UI - showing actual published version
        findViewById<TextView>(R.id.tvSDKVersion).text = "✅ PUBLISHED: $sdkVersion\n📦 From JitPack"
        Toast.makeText(this, "🚀 Using Published SDK: $sdkVersion", Toast.LENGTH_LONG).show()
        
        setupUI()
        setupPublishedSDK()
    }
    
    private fun setupUI() {
        findViewById<Button>(R.id.btnTestWidget).setOnClickListener {
            val version = LimechatWidgetButton.getSDKVersion()
            Toast.makeText(this, "✅ Published $version works! Check bottom-right corner", Toast.LENGTH_LONG).show()
            Log.d(TAG, "✅ Widget button test clicked - using PUBLISHED $version from JitPack")
        }
        
        findViewById<Button>(R.id.btnUpdateBadge).setOnClickListener {
            badgeCount = (1..9).random()
            widgetButton.updateUnreadCount(badgeCount)
            Toast.makeText(this, "Updated badge to $badgeCount", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🔢 Badge updated to: $badgeCount")
        }
    }
    
    private fun setupPublishedSDK() {
        Log.d(TAG, "🔧 Initializing published SDK...")
        
        // Create widget configuration using ONLY published SDK classes
        val config = WidgetConfig(
            websiteToken = WEBSITE_TOKEN,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = "Published SDK Demo User",
                email = "demo@published-sdk.com",
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "demo_type" to "published_sdk_only",
                "version" to "0.0.1",
                "source" to "jitpack"
            )
        )
        
        // Create widget button using published SDK
        widgetButton = LimechatWidgetButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(16), dpToPx(16))
            }
        }
        
        // Initialize with config - this will fetch default icon from API
        widgetButton.init(config)
        
        // Set click listener
        widgetButton.setOnClickListener {
            val version = LimechatWidgetButton.getSDKVersion()
            Toast.makeText(this, "🎯 Widget clicked! Published $version from JitPack!", Toast.LENGTH_LONG).show()
            Log.d(TAG, "🎯 Widget button clicked - Published $version working!")
        }
        
        // Add to layout
        val container = findViewById<FrameLayout>(R.id.mainContainer)
        container.addView(widgetButton)
        
        // Set initial badge
        widgetButton.updateUnreadCount(3)
        
        Log.d(TAG, "✅ Published SDK initialized successfully!")
        
        // Show token warning if needed
        if (WEBSITE_TOKEN == "YOUR_WEBSITE_TOKEN") {
            Toast.makeText(
                this, 
                "⚠️ Replace WEBSITE_TOKEN with your actual token", 
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetButton.isInitialized) {
            widgetButton.destroy()
        }
        Log.d(TAG, "🧹 Published SDK demo cleaned up")
    }
}