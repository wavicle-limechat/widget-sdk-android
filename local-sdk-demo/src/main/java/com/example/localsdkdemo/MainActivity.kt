package com.example.localsdkdemo

import android.app.Activity
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
        private const val WIDGET_ACTIVITY_REQUEST_CODE = 1001
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private var badgeCount = 3
    private var sharedConversationToken: String? = null
    
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
        
        // Test button - opens widget directly (Button 1 - shares conversation)
        findViewById<Button>(R.id.btnTestWidget).setOnClickListener {
            Toast.makeText(this, "Opening widget (Button 1 - shares conversation)...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🚀 Button 1 clicked - opening widget with shared conversation token: $sharedConversationToken")
            openWidgetWithConversation()
        }
        
        // Badge test button  
        findViewById<Button>(R.id.btnUpdateBadge).setOnClickListener {
            badgeCount = (1..9).random()
            widgetButton.updateUnreadCount(badgeCount)
            Toast.makeText(this, "Badge updated to $badgeCount", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "📱 Badge count updated: $badgeCount")
        }
        
        // Custom message button (Button 2 - shares conversation)
        findViewById<Button>(R.id.btnOpenWithCustomMessage).setOnClickListener {
            val customMessage = "Hi, I wanted to ask something!"
            Log.d(TAG, "🗨️ Button 2 clicked - opening widget with custom message and shared conversation: $sharedConversationToken")
            Toast.makeText(this, "Opening with message (Button 2 - shares conversation): $customMessage", Toast.LENGTH_SHORT).show()
            openWidgetWithMessageAndConversation(customMessage)
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
        
        // Set click listener to demonstrate custom message functionality with conversation persistence
        widgetButton.setOnClickListener {
            Log.d(TAG, "🎯 Floating widget button clicked - using conversation persistence")
            
            // Alternate between different message types for demo, all with conversation persistence
            val randomChoice = (1..3).random()
            when (randomChoice) {
                1 -> {
                    val message = "Hello! I need help with my order #${(1000..9999).random()}"
                    Log.d(TAG, "🗨️ Opening widget with string message and conversation persistence: $message")
                    openWidgetWithMessageAndConversation(message)
                }
                2 -> {
                    val messageData = mapOf(
                        "content" to "I have a question about your pricing plans",
                        "type" to "inquiry",
                        "priority" to "medium"
                    )
                    Log.d(TAG, "🗨️ Opening widget with object message and conversation persistence: $messageData")
                    openWidgetWithMessageAndConversation(messageData)
                }
                else -> {
                    Log.d(TAG, "🎯 Opening widget with conversation persistence")
                    openWidgetWithConversation()
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
    
    private fun openWidget() {
        Log.d(TAG, "🚀 Opening full-screen widget with local SDK")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with conversation persistence (Button 1)
     */
    private fun openWidgetWithConversation() {
        Log.d(TAG, "🚀 Opening widget with conversation persistence (Button 1)")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("conversation_token", sharedConversationToken)
            putExtra("button_id", "button_1")
        }
        startActivityForResult(intent, WIDGET_ACTIVITY_REQUEST_CODE)
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
        }
        startActivity(intent)
    }
    
    /**
     * Open widget with custom message and conversation persistence (Button 2)
     */
    private fun openWidgetWithMessageAndConversation(message: String) {
        Log.d(TAG, "🚀 Opening widget with custom message and conversation persistence (Button 2)")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message", message)
            putExtra("conversation_token", sharedConversationToken)
            putExtra("button_id", "button_2")
        }
        startActivityForResult(intent, WIDGET_ACTIVITY_REQUEST_CODE)
    }
    
    /**
     * Open widget with custom message data and conversation persistence (Floating Button)
     */
    private fun openWidgetWithMessageAndConversation(messageData: Map<String, Any>) {
        Log.d(TAG, "🚀 Opening widget with custom message data and conversation persistence (Floating Button)")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Local SDK Demo User")
            putExtra("user_email", "demo@local.com")
            putExtra("custom_message_data", HashMap(messageData))
            putExtra("conversation_token", sharedConversationToken)
            putExtra("button_id", "floating_button")
        }
        startActivityForResult(intent, WIDGET_ACTIVITY_REQUEST_CODE)
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
        }
        startActivity(intent)
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == WIDGET_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val conversationToken = data?.getStringExtra("conversation_token")
            val buttonId = data?.getStringExtra("button_id")
            
            if (conversationToken != null) {
                sharedConversationToken = conversationToken
                Log.d(TAG, "🔄 Conversation token updated from $buttonId: $conversationToken")
                Toast.makeText(this, "Conversation token updated: ${conversationToken.take(10)}...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetButton.isInitialized) {
            widgetButton.destroy()
        }
        Log.d(TAG, "🧹 Local SDK demo cleaned up")
    }
}
