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
    }
    
    private fun setupWidgetWithLocalSDK() {
        Log.d(TAG, "🔧 Initializing widget with LOCAL SDK...")
        
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = WEBSITE_TOKEN,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = "Local SDK Demo User",
                email = "demo@local.com"
            ),
            customAttributes = mapOf(
                "demo_type" to "local_widget_sdk",
                "source" to "local_module_dependency"
            )
        )
        
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
        
        // Set click listener to demonstrate custom message functionality
        widgetButton.setOnClickListener {
            Log.d(TAG, "🎯 Local SDK widget clicked - demonstrating custom message")
            
            // Alternate between different message types for demo
            val randomChoice = (1..3).random()
            when (randomChoice) {
                1 -> {
                    val message = "Hello! I need help with my order #${(1000..9999).random()}"
                    Log.d(TAG, "🗨️ Opening widget with string message: $message")
                    openWidgetWithMessage(
                        message = message,
                        instanceId = WidgetActivity.INSTANCE_ID_FLOATING,
                        manageToken = false
                    )
                }
                2 -> {
                    val messageData = mapOf(
                        "content" to "I have a question about your pricing plans",
                        "type" to "inquiry",
                        "priority" to "medium"
                    )
                    Log.d(TAG, "🗨️ Opening widget with object message: $messageData")
                    openWidgetWithMessage(
                        messageData = messageData,
                        instanceId = WidgetActivity.INSTANCE_ID_FLOATING,
                        manageToken = false
                    )
                }
                else -> {
                    Log.d(TAG, "🎯 Opening widget without custom message")
                    openWidget(
                        instanceId = WidgetActivity.INSTANCE_ID_FLOATING,
                        manageToken = false
                    )
                }
            }
        }
        
        // Add to layout
        findViewById<FrameLayout>(R.id.mainContainer).addView(widgetButton)
        
        // Set initial badge
        widgetButton.updateUnreadCount(badgeCount)
        
        Log.d(TAG, "✅ Local SDK widget initialized successfully")
        Log.d(TAG, "🌐 Widget will load: ${config.baseUrl}/widget?website_token=${config.websiteToken}")
        
        Toast.makeText(this, "✅ Local SDK ready with token: ${WEBSITE_TOKEN}", Toast.LENGTH_LONG).show()
    }
    
    private fun openWidget(
        instanceId: String = WidgetActivity.INSTANCE_ID_PRIMARY,
        manageToken: Boolean = true
    ) {
        Log.d(TAG, "🚀 Opening full-screen widget with local SDK")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra(WidgetActivity.EXTRA_INSTANCE_ID, instanceId)
            putExtra(WidgetActivity.EXTRA_MANAGE_TOKEN, manageToken)
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with custom string message
     */
    private fun openWidgetWithMessage(
        message: String,
        instanceId: String = WidgetActivity.INSTANCE_ID_PRIMARY,
        manageToken: Boolean = true
    ) {
        Log.d(TAG, "🚀 Opening full-screen widget with custom message: $message")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message", message)
            putExtra(WidgetActivity.EXTRA_INSTANCE_ID, instanceId)
            putExtra(WidgetActivity.EXTRA_MANAGE_TOKEN, manageToken)
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with custom message object
     */
    private fun openWidgetWithMessage(
        messageData: Map<String, Any>,
        instanceId: String = WidgetActivity.INSTANCE_ID_PRIMARY,
        manageToken: Boolean = true
    ) {
        Log.d(TAG, "🚀 Opening full-screen widget with custom message data: $messageData")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message_data", HashMap(messageData))
            putExtra(WidgetActivity.EXTRA_INSTANCE_ID, instanceId)
            putExtra(WidgetActivity.EXTRA_MANAGE_TOKEN, manageToken)
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
