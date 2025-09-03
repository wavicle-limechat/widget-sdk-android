package com.example.localsdkdemo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage

/**
 * Refactored full-screen widget activity with improved architecture
 * 
 * Key improvements:
 * - Better error handling and recovery
 * - Proper lifecycle management
 * - Clean separation of concerns
 * - Enhanced logging and debugging
 * - Memory leak prevention
 */
class WidgetActivityRefactored : FragmentActivity() {
    
    companion object {
        private const val TAG = "RefactoredWidgetActivity"
    }
    
    // Core components
    private var widgetView: LimechatWidgetViewRefactored? = null
    private var widgetFilePicker: WidgetFilePicker? = null
    
    // Configuration
    private lateinit var config: WidgetConfig
    private var initialMessage: WidgetMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "🚀 Opening refactored widget activity")
        
        try {
            parseIntentExtras()
            setupWidgetView()
            initializeWidget()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize widget activity", e)
            handleInitializationError(e)
        }
    }

    override fun onBackPressed() {
        val handled = widgetView?.handleBackPress() ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
        Log.d(TAG, "🧹 Refactored widget activity cleaned up")
    }

    // Private implementation methods

    private fun parseIntentExtras() {
        val websiteToken = intent.getStringExtra("website_token") ?: "PN5LeU9Cyng1CRiCXTGNMm3x"
        val baseUrl = intent.getStringExtra("base_url") ?: "https://cf2e01f8e319.ngrok-free.app"
        val userName = intent.getStringExtra("user_name") ?: "Demo User"
        val userEmail = intent.getStringExtra("user_email") ?: "demo@refactored.com"
        
        config = WidgetConfig(
            websiteToken = websiteToken,
            baseUrl = baseUrl,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = userName,
                email = userEmail,
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "refactored_sdk_demo",
                "type" to "improved_architecture",
                "activity" to "WidgetActivityRefactored"
            )
        )
        
        // Parse initial message if provided
        initialMessage = parseInitialMessage()
        
        Log.d(TAG, "Configuration parsed - Token: $websiteToken, User: $userName")
        initialMessage?.let { msg ->
            Log.d(TAG, "Initial message parsed: ${msg.getMessageContent()}")
        }
    }

    private fun parseInitialMessage(): WidgetMessage? {
        return try {
            when (intent.getStringExtra("message_type")) {
                "text" -> {
                    val content = intent.getStringExtra("message_content") ?: return null
                    WidgetMessage.Text(content)
                }
                "structured" -> {
                    val content = intent.getStringExtra("message_content") ?: return null
                    @Suppress("UNCHECKED_CAST")
                    val properties = intent.getSerializableExtra("message_properties") as? Map<String, Any> ?: emptyMap()
                    WidgetMessage.Structured(content, properties)
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse initial message", e)
            null
        }
    }

    private fun setupWidgetView() {
        widgetView = LimechatWidgetViewRefactored(this)
        setContentView(widgetView)
        
        // Set up file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView?.attachFilePicker(widgetFilePicker!!)
    }

    private fun initializeWidget() {
        val callback = createWidgetCallback()
        
        widgetView?.initialize(config, callback, initialMessage)
        
        Log.d(TAG, "✅ Refactored widget initialized")
    }

    private fun createWidgetCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "✅ Refactored widget loaded successfully")
                handleWidgetLoaded()
            }
            
            override fun onClose() {
                Log.d(TAG, "🚪 Widget close requested")
                finish()
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "❌ Widget error: ${error.message}", error.cause)
                handleWidgetError(error)
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d(TAG, "📨 Widget message received: $message")
                handleWidgetMessage(message)
            }
        }
    }

    private fun handleWidgetLoaded() {
        // Widget is ready - no additional actions needed since initial message
        // is handled during initialization
        Log.d(TAG, "Widget ready for interaction")
    }

    private fun handleWidgetError(error: WidgetError) {
        // Log error details for debugging
        Log.e(TAG, "Widget error details - Code: ${error.code}, Message: ${error.message}")
        error.context?.forEach { (key, value) ->
            Log.e(TAG, "Error context - $key: $value")
        }
        
        // In a production app, you might want to show error UI or retry logic
        // For now, we'll just log it
    }

    private fun handleWidgetMessage(message: Map<String, Any>) {
        // Handle custom widget messages if needed
        // This could include analytics, custom actions, etc.
        val event = message["event"] as? String
        when (event) {
            "custom_action" -> {
                Log.d(TAG, "Handling custom action: ${message["action"]}")
            }
            "analytics_event" -> {
                Log.d(TAG, "Analytics event: ${message["data"]}")
            }
            else -> {
                Log.d(TAG, "Unhandled widget message: $message")
            }
        }
    }

    private fun handleInitializationError(error: Throwable) {
        Log.e(TAG, "Critical error during initialization", error)
        
        // In a production app, you might want to show an error screen
        // or fallback UI. For now, we'll just finish the activity
        finish()
    }

    private fun cleanupResources() {
        try {
            widgetView?.cleanup()
            widgetView = null
            widgetFilePicker = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}