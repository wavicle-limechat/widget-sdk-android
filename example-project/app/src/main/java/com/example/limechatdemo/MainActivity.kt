package com.example.limechatdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

/**
 * Example showing LimeChat Widget Button integration
 * This activity demonstrates:
 * - Widget button with default icon (fetched from API)
 * - Unread count badge
 * - Click handling to open full-screen widget
 */
class MainActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val WEBSITE_TOKEN = "YOUR_WEBSITE_TOKEN" // Replace with your actual token
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private var currentUnreadCount = 3 // Example unread count
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupUI()
        setupLimeChatWidget()
    }
    
    private fun setupUI() {
        // Set up navigation buttons
        findViewById<Button>(R.id.buttonCustomExample).setOnClickListener {
            startActivity(Intent(this, CustomButtonActivity::class.java))
        }
        
        findViewById<Button>(R.id.buttonFullscreenExample).setOnClickListener {
            openFullScreenWidget()
        }
        
        findViewById<Button>(R.id.buttonUpdateBadge).setOnClickListener {
            currentUnreadCount = (1..10).random()
            widgetButton.updateUnreadCount(currentUnreadCount)
            
            val message = "Updated unread count to $currentUnreadCount"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, message)
        }
    }
    
    private fun setupLimeChatWidget() {
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = WEBSITE_TOKEN,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.AUTO, // Adapts to system theme
            user = WidgetConfig.User(
                name = "Demo User",
                email = "demo@example.com",
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "android_demo",
                "app_version" to "1.0.0",
                "user_type" to "demo"
            )
        )
        
        // Create widget button
        widgetButton = LimechatWidgetButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(16), dpToPx(16))
            }
        }
        
        // Initialize widget button (will fetch default icon from API)
        widgetButton.init(config)
        
        // Set click listener to open full-screen widget
        widgetButton.setOnClickListener {
            Log.d(TAG, "Widget button clicked - opening full-screen widget")
            openFullScreenWidget()
        }
        
        // Add button to main container
        val mainContainer = findViewById<FrameLayout>(R.id.mainContainer)
        mainContainer.addView(widgetButton)
        
        // Set initial unread count
        widgetButton.updateUnreadCount(currentUnreadCount)
        
        Log.d(TAG, "LimeChat widget button initialized with token: $WEBSITE_TOKEN")
        
        // Show token warning if not configured
        if (WEBSITE_TOKEN == "YOUR_WEBSITE_TOKEN") {
            showTokenWarning()
        }
    }
    
    private fun openFullScreenWidget() {
        Log.d(TAG, "Opening full-screen widget")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Demo User")
            putExtra("user_email", "demo@example.com")
        }
        startActivity(intent)
    }
    
    private fun showTokenWarning() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Configuration Required")
            .setMessage("Please replace 'YOUR_WEBSITE_TOKEN' with your actual LimeChat website token in MainActivity.kt")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        
        dialog.show()
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
    }
}