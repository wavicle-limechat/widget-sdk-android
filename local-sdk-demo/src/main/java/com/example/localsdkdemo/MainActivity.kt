package com.example.localsdkdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Import from LOCAL widget-sdk module - NOT published JitPack
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

/**
 * LOCAL SDK DEMO
 * 
 * This app uses ONLY the LOCAL widget-sdk module:
 * implementation(project(":widget-sdk"))
 * 
 * This proves the local SDK works for development and testing
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "LocalSDKDemo"
        private const val WEBSITE_TOKEN = "MEFFACy4xaovJayhLjSt836h"
        private const val BASE_URL = "https://app.limechat.ai"
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private var badgeCount = 3
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 LOCAL SDK DEMO - Using local widget-sdk module")
        
        setupUI()
        setupWidgetWithLocalSDK()
    }
    
    private fun setupUI() {
        // Update status text
        findViewById<TextView>(R.id.tvStatus).text = "✅ Using LOCAL SDK from widget-sdk module"
        
        // Test button - opens widget directly
        findViewById<Button>(R.id.btnTestWidget).setOnClickListener {
            Toast.makeText(this, "Opening local SDK widget...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🚀 Test button clicked - opening local SDK widget")
            openWidget()
        }
        
        // Badge test button  
        findViewById<Button>(R.id.btnUpdateBadge).setOnClickListener {
            badgeCount = (1..9).random()
            widgetButton.updateUnreadCount(badgeCount)
            Toast.makeText(this, "Badge updated to $badgeCount", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "📱 Badge count updated: $badgeCount")
        }
        
        // Custom message button
        findViewById<Button>(R.id.btnOpenWithCustomMessage).setOnClickListener {
            val customMessage = "Hi, I wanted to ask something!"
            Log.d(TAG, "🗨️ Opening widget with custom message: $customMessage")
            Toast.makeText(this, "Opening with message: $customMessage", Toast.LENGTH_SHORT).show()
            openWidgetWithMessage(customMessage)
        }
        
        // Without legacy back icon button
        findViewById<Button>(R.id.btnOpenWithoutLegacyIcon).setOnClickListener {
            Log.d(TAG, "🚫 Opening widget WITHOUT legacy back icon")
            Toast.makeText(this, "Opening widget without legacy back icon", Toast.LENGTH_SHORT).show()
            openWidgetWithoutLegacyIcon()
        }
    }
    
    private fun setupWidgetWithLocalSDK() {
        Log.d(TAG, "🔧 Initializing widget with LOCAL SDK...")
        
        // Create widget configuration for floating button using builder pattern
        val config = WidgetConfig.builder(WEBSITE_TOKEN)
            .setLocale("en")
            .setColorScheme(WidgetConfig.ColorScheme.LIGHT)
            .setUser(WidgetConfig.User(
                name = "Local SDK Demo User",
                email = "demo@local.com"
            ))
            .setCustomAttributes(mapOf(
                "demo_type" to "local_widget_sdk",
                "source" to "local_module_dependency"
            ))
            .setInstanceId("floating-button") // Unique ID for floating button
            .setShowLegacyBackIcon(true) // Enable legacy back icon for demo
            .build()
        
        // Create widget button using local SDK
        widgetButton = LimechatWidgetButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(16), dpToPx(16))
            }
        }
        
        // Initialize widget
        widgetButton.init(config)
        
        // Set click listener for consistent behavior (same as "Test Widget" button)
        widgetButton.setOnClickListener {
            Log.d(TAG, "🎯 Floating widget button clicked - opening widget")
            Toast.makeText(this, "Opening floating widget...", Toast.LENGTH_SHORT).show()
            
            // Open widget consistently (same behavior as Test Widget button)
            val intent = Intent(this, WidgetActivity::class.java).apply {
                putExtra("website_token", WEBSITE_TOKEN)
                putExtra("user_name", "Local SDK Demo User")
                putExtra("user_email", "demo@local.com")
                putExtra("instance_id", "floating-button") // Unique ID for floating button (matches config)
            }
            startActivity(intent)
        }
        
        // Add to layout
        findViewById<FrameLayout>(R.id.mainContainer).addView(widgetButton)
        
        // Set initial badge
        widgetButton.updateUnreadCount(badgeCount)
        
        Log.d(TAG, "✅ Local SDK widget initialized successfully")
        Log.d(TAG, "🌐 Widget will load: ${config.baseUrl}/widget?website_token=${config.websiteToken}")
        
        Toast.makeText(this, "✅ Local SDK ready with token: ${WEBSITE_TOKEN}", Toast.LENGTH_LONG).show()
    }
    
    private fun openWidget() {
        Log.d(TAG, "🚀 Opening full-screen widget with local SDK")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("instance_id", "main-widget-buttons") // Shared ID for synced buttons
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with custom string message
     */
    private fun openWidgetWithMessage(message: String) {
        Log.d(TAG, "🚀 Opening full-screen widget with custom message: $message")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message", message)
            putExtra("instance_id", "main-widget-buttons") // Shared ID for synced buttons
        }
        startActivity(intent)
    }
    
    /**
     * Open widget WITHOUT legacy back icon (showLegacyBackIcon = false)
     */
    private fun openWidgetWithoutLegacyIcon() {
        Log.d(TAG, "🚫 Opening full-screen widget WITHOUT legacy back icon")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("instance_id", "no-legacy-icon") // Unique ID for this demo
            putExtra("show_legacy_back_icon", false) // Pass flag to disable legacy icon
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with custom message object
     */
    private fun openWidgetWithMessage(messageData: Map<String, Any>) {
        Log.d(TAG, "🚀 Opening full-screen widget with custom message data: $messageData")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message_data", HashMap(messageData))
            putExtra("instance_id", "main-widget-buttons") // Shared ID for synced buttons
        }
        startActivity(intent)
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetButton.isInitialized) {
            widgetButton.destroy()
        }
        Log.d(TAG, "🧹 Local SDK demo cleaned up")
    }
}
