package com.example.publishedsdkdemo

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

// Import from JitPack published SDK - NOT local project
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

/**
 * PUBLISHED SDK DEMO
 * 
 * This app uses ONLY the JitPack published SDK:
 * implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.9-beta-4")
 * 
 * NO local project dependencies - proving the SDK works independently
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "PublishedSDKDemo"
        private const val WEBSITE_TOKEN = "MEFFACy4xaovJayhLjSt836h"
        private const val BASE_URL = "https://app.limechat.ai"
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private var badgeCount = 3
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 PUBLISHED SDK DEMO - Using JitPack dependency")
        
        setupUI()
        setupWidgetWithPublishedSDK()
    }
    
    private fun setupUI() {
        // Update status text
        findViewById<TextView>(R.id.tvStatus).text = "✅ Using PUBLISHED SDK from JitPack"
        
        // Test button - opens widget directly
        findViewById<Button>(R.id.btnTestWidget).setOnClickListener {
            Toast.makeText(this, "Opening published SDK widget...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "🚀 Test button clicked - opening published SDK widget")
            openWidget()
        }
        
        // Badge test button  
        findViewById<Button>(R.id.btnUpdateBadge).setOnClickListener {
            badgeCount = (1..9).random()
            widgetButton.updateUnreadCount(badgeCount)
            Toast.makeText(this, "Badge updated to $badgeCount", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "📱 Badge count updated: $badgeCount")
        }
    }
    
    private fun setupWidgetWithPublishedSDK() {
        Log.d(TAG, "🔧 Initializing widget with PUBLISHED SDK...")
        
        // Create widget configuration using builder pattern
        val config = WidgetConfig.builder(WEBSITE_TOKEN)
            .setLocale("en")
            .setColorScheme(WidgetConfig.ColorScheme.LIGHT)
            .setUser(WidgetConfig.User(
                name = "Published SDK Demo User",
                email = "demo@published.com"
            ))
            .setCustomAttributes(mapOf(
                "demo_type" to "published_jitpack_sdk",
                "source" to "jitpack_dependency"
            ))
            .setInstanceId("published-sdk-demo-floating-button")
            .build()
        
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
        
        // Initialize widget
        widgetButton.init(config)
        
        // Set click listener to open actual widget (same instance ID as floating button)
        widgetButton.setOnClickListener {
            Log.d(TAG, "🎯 Published SDK widget clicked - opening full widget")
            val intent = Intent(this, WidgetActivity::class.java).apply {
                putExtra("website_token", WEBSITE_TOKEN)
                putExtra("user_name", "Published SDK Demo User")
                putExtra("user_email", "demo@published.com")
                putExtra("instance_id", "published-sdk-demo-floating-button") // Same as floating button config
            }
            startActivity(intent)
        }
        
        // Add to layout
        findViewById<FrameLayout>(R.id.mainContainer).addView(widgetButton)
        
        // Set initial badge
        widgetButton.updateUnreadCount(badgeCount)
        
        Log.d(TAG, "✅ Published SDK widget initialized successfully")
        Log.d(TAG, "🌐 Widget will load: ${config.baseUrl}/widget?website_token=${config.websiteToken}")
        
        Toast.makeText(this, "✅ Published SDK ready with token: ${WEBSITE_TOKEN}", Toast.LENGTH_LONG).show()
    }
    
    private fun openWidget() {
        Log.d(TAG, "🚀 Opening full-screen widget with published SDK")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", WEBSITE_TOKEN)
            putExtra("user_name", "Published SDK Demo User")
            putExtra("user_email", "demo@published.com")
            putExtra("instance_id", "published-sdk-demo-test-button")
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
        Log.d(TAG, "🧹 Published SDK demo cleaned up")
    }
}