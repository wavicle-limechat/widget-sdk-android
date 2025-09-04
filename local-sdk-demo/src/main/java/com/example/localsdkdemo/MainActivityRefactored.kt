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
import ai.limechat.widget.LimechatWidgetButtonRefactored
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage

/**
 * Refactored main activity with improved architecture
 * 
 * Key improvements:
 * - Better separation of concerns
 * - Proper lifecycle management
 * - Enhanced error handling
 * - Cleaner code organization
 * - Memory leak prevention
 */
class MainActivityRefactored : AppCompatActivity() {
    
    companion object {
        private const val TAG = "RefactoredMainActivity"
        private const val WEBSITE_TOKEN = "PN5LeU9Cyng1CRiCXTGNMm3x"
        private const val BASE_URL = "https://cf2e01f8e319.ngrok-free.app"
    }
    
    // UI components
    private var widgetButton: LimechatWidgetButtonRefactored? = null
    private var statusText: TextView? = null
    
    // State
    private var badgeCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 REFACTORED SDK DEMO - Starting with improved architecture")
        
        initializeViews()
        setupWidgetButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
        Log.d(TAG, "🧹 Refactored demo cleaned up")
    }

    // Private implementation methods

    private fun initializeViews() {
        statusText = findViewById<TextView>(R.id.tvStatus)?.apply {
            text = "✅ Using REFACTORED LOCAL SDK"
        }
        
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Test button - opens widget directly
        findViewById<Button>(R.id.btnTestWidget)?.setOnClickListener {
            handleTestButtonClick()
        }
        
        // Badge test button  
        findViewById<Button>(R.id.btnUpdateBadge)?.setOnClickListener {
            handleBadgeUpdateClick()
        }
        
        // Custom message button
        findViewById<Button>(R.id.btnOpenWithCustomMessage)?.setOnClickListener {
            handleCustomMessageClick()
        }
    }

    private fun setupWidgetButton() {
        try {
            val config = createWidgetConfig()
            
            widgetButton = LimechatWidgetButtonRefactored(this).apply {
                layoutParams = createWidgetButtonLayoutParams()
                
                // Initialize the button
                initialize(config)
                
                // Set click listener for demonstration
                setOnClickListener { 
                    handleWidgetButtonClick()
                }
                
                // Set initial badge count
                updateUnreadCount(badgeCount)
            }
            
            // Add to main container
            findViewById<FrameLayout>(R.id.mainContainer)?.addView(widgetButton)
            
            showSuccessMessage()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup widget button", e)
            showErrorMessage("Failed to initialize widget: ${e.message}")
        }
    }

    private fun createWidgetConfig(): WidgetConfig {
        return WidgetConfig(
            websiteToken = WEBSITE_TOKEN,
            baseUrl = BASE_URL,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = "Refactored SDK Demo User",
                email = "refactored@demo.com"
            ),
            customAttributes = mapOf(
                "demo_type" to "refactored_local_sdk",
                "source" to "improved_architecture",
                "version" to LimechatWidgetButtonRefactored.getSDKVersion()
            )
        )
    }

    private fun createWidgetButtonLayoutParams(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, dpToPx(16), dpToPx(16))
        }
    }

    private fun handleTestButtonClick() {
        Log.d(TAG, "🚀 Test button clicked - opening widget")
        showToast("Opening refactored SDK widget...")
        
        openWidgetActivity()
    }

    private fun handleBadgeUpdateClick() {
        badgeCount = (1..9).random()
        widgetButton?.updateUnreadCount(badgeCount)
        
        showToast("Badge updated to $badgeCount")
        Log.d(TAG, "📱 Badge count updated: $badgeCount")
    }

    private fun handleCustomMessageClick() {
        val customMessage = WidgetMessage.Text("Hi, I wanted to ask something!")
        Log.d(TAG, "🗨️ Opening widget with custom message: ${customMessage.content}")
        
        showToast("Opening with custom message...")
        openWidgetActivity(customMessage)
    }

    private fun handleWidgetButtonClick() {
        Log.d(TAG, "🎯 Widget button clicked - demonstrating functionality")
        
        // Demonstrate different message types
        val messageType = (1..3).random()
        when (messageType) {
            1 -> {
                val message = WidgetMessage.Text("Hello! I need help with order #${(1000..9999).random()}")
                openWidgetActivity(message)
            }
            2 -> {
                val message = WidgetMessage.Structured(
                    content = "I have a question about your pricing",
                    properties = mapOf(
                        "type" to "inquiry",
                        "priority" to "medium",
                        "source" to "widget_button"
                    )
                )
                openWidgetActivity(message)
            }
            else -> {
                openWidgetActivity()
            }
        }
    }

    private fun openWidgetActivity(message: WidgetMessage? = null) {
        try {
            val intent = Intent(this, WidgetActivityRefactored::class.java).apply {
                putExtra("website_token", WEBSITE_TOKEN)
                putExtra("base_url", BASE_URL)
                putExtra("user_name", "Refactored SDK Demo User")
                putExtra("user_email", "refactored@demo.com")
                
                message?.let { msg ->
                    when (msg) {
                        is WidgetMessage.Text -> {
                            putExtra("message_type", "text")
                            putExtra("message_content", msg.content)
                        }
                        is WidgetMessage.Structured -> {
                            putExtra("message_type", "structured")
                            putExtra("message_content", msg.content)
                            putExtra("message_properties", HashMap(msg.properties))
                        }
                    }
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open widget activity", e)
            showErrorMessage("Failed to open widget: ${e.message}")
        }
    }

    private fun showSuccessMessage() {
        val message = "✅ Refactored SDK ready with token: $WEBSITE_TOKEN"
        showToast(message, isLong = true)
        Log.d(TAG, message)
    }

    private fun showErrorMessage(message: String) {
        showToast("❌ $message", isLong = true)
        Log.e(TAG, message)
    }

    private fun showToast(message: String, isLong: Boolean = false) {
        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(this, message, duration).show()
    }

    private fun cleanupResources() {
        try {
            widgetButton?.cleanup()
            widgetButton = null
            statusText = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}