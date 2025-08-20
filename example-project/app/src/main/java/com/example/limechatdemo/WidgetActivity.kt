package com.example.limechatdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig

/**
 * Example of full-screen widget integration
 * This activity demonstrates:
 * - Full-screen widget view
 * - File upload support
 * - Error handling
 * - Custom message handling
 * - Back navigation
 */
class WidgetActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "WidgetActivity"
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)
        
        initWidget()
    }
    
    private fun initWidget() {
        widgetView = findViewById(R.id.widgetView)
        
        // Initialize file picker for file uploads (IMPORTANT: Do this in onCreate)
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get data from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "YOUR_WEBSITE_TOKEN"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@example.com"
        
        // Create user object
        val user = WidgetConfig.User(
            name = userName,
            email = userEmail,
            phoneNumber = "+1234567890"
        )
        
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = websiteToken,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.AUTO,
            user = user,
            customAttributes = mapOf(
                "source" to "android_fullscreen_demo",
                "session_id" to System.currentTimeMillis().toString(),
                "activity" to "WidgetActivity"
            )
        )
        
        // Initialize widget with comprehensive callbacks
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Widget loaded successfully")
                // Widget is now ready for interaction
            }
            
            override fun onClose() {
                Log.d(TAG, "🚪 Widget close requested")
                // User requested to close the widget
                finish() // Close this activity
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "❌ Widget error [${error.code}]: ${error.message}")
                handleWidgetError(error)
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d(TAG, "📨 Widget message received: $message")
                handleWidgetMessage(message)
            }
        })
        
        Log.d(TAG, "Widget initialized with token: $websiteToken")
    }
    
    /**
     * Handle different types of widget errors
     */
    private fun handleWidgetError(error: WidgetError) {
        val errorMessage = when (error.code) {
            WidgetError.ErrorCode.CONFIG_ERROR -> {
                "Configuration Error: ${error.message}"
            }
            WidgetError.ErrorCode.NETWORK_ERROR -> {
                "Network Error: Please check your internet connection"
            }
            WidgetError.ErrorCode.WEBVIEW_ERROR -> {
                "Loading Error: ${error.message}"
            }
            WidgetError.ErrorCode.JAVASCRIPT_ERROR -> {
                "Script Error: ${error.message}"
            }
            WidgetError.ErrorCode.FILE_PICKER_ERROR -> {
                "File Upload Error: ${error.message}"
            }
            WidgetError.ErrorCode.UNKNOWN_ERROR -> {
                "Unknown Error: ${error.message}"
            }
        }
        
        // Show error to user
        android.widget.Toast.makeText(this, errorMessage, android.widget.Toast.LENGTH_LONG).show()
        
        // Log additional error context if available
        error.context?.let { context ->
            Log.d(TAG, "Error context: $context")
        }
    }
    
    /**
     * Handle messages from the widget
     */
    private fun handleWidgetMessage(message: Map<String, Any>) {
        val event = message["event"] as? String
        
        when (event) {
            "unread-count-changed" -> {
                val count = message["count"] as? Int ?: 0
                Log.d(TAG, "📬 Unread count changed: $count")
                // Update your app's unread count badge, notification, etc.
                updateUnreadCount(count)
            }
            
            "conversation-started" -> {
                Log.d(TAG, "🗨️ Conversation started")
                // Handle conversation started event
                // Maybe send analytics event or update UI
            }
            
            "agent-joined" -> {
                val agentName = message["agent_name"] as? String
                Log.d(TAG, "👤 Agent joined: $agentName")
                // Handle agent joined event
            }
            
            "message-sent" -> {
                val messageText = message["text"] as? String
                Log.d(TAG, "📤 Message sent: $messageText")
                // Handle message sent event
            }
            
            "file-uploaded" -> {
                val fileName = message["file_name"] as? String
                Log.d(TAG, "📎 File uploaded: $fileName")
                // Handle file upload success
            }
            
            else -> {
                Log.d(TAG, "🔔 Unknown event: $event")
                // Handle unknown events
            }
        }
    }
    
    /**
     * Update unread count in your app
     * This could update a badge, notification, or other UI elements
     */
    private fun updateUnreadCount(count: Int) {
        // Example: Update notification badge
        // Example: Update app icon badge
        // Example: Send broadcast to update other activities
        
        Log.d(TAG, "Updating app unread count to: $count")
        
        // For demo purposes, just show a toast
        if (count > 0) {
            android.widget.Toast.makeText(
                this, 
                "New messages: $count", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Handle back button press - let widget handle navigation first
     */
    override fun onBackPressed() {
        // Let the widget handle back navigation first (for WebView history)
        if (!widgetView.onBackPressed()) {
            // Widget couldn't handle it, so close the activity
            super.onBackPressed()
        }
    }
    
    /**
     * Clean up resources when activity is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 Cleaning up widget resources")
        
        if (this::widgetView.isInitialized) {
            widgetView.destroy()
        }
    }
}