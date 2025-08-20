package ai.limechat.widget.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class ButtonDemoActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "ButtonDemoActivity"
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    private lateinit var customButtonContainer: FrameLayout
    private lateinit var toggleCustomButton: Switch
    private lateinit var updateUnreadButton: Button
    private lateinit var unreadCountInput: EditText
    private lateinit var logText: TextView
    
    private var useCustomButton = false
    private var unreadCount = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_demo)
        
        initViews()
        setupListeners()
        initWidgetButton()
        
        appendLog("Button demo initialized")
    }
    
    private fun initViews() {
        customButtonContainer = findViewById(R.id.customButtonContainer)
        toggleCustomButton = findViewById(R.id.toggleCustomButton)
        updateUnreadButton = findViewById(R.id.updateUnreadButton)
        unreadCountInput = findViewById(R.id.unreadCountInput)
        logText = findViewById(R.id.logText)
    }
    
    private fun setupListeners() {
        toggleCustomButton.setOnCheckedChangeListener { _, isChecked ->
            useCustomButton = isChecked
            appendLog("Custom button ${if (isChecked) "enabled" else "disabled"}")
            initWidgetButton()
        }
        
        updateUnreadButton.setOnClickListener {
            val count = unreadCountInput.text.toString().toIntOrNull() ?: 0
            unreadCount = count
            widgetButton.updateUnreadCount(count)
            appendLog("Updated unread count to $count")
        }
    }
    
    private fun initWidgetButton() {
        // Remove existing button if any
        customButtonContainer.removeAllViews()
        
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
        
        // Create widget config
        val config = WidgetConfig(
            websiteToken = "MEFFACy4xaovJayhLjSt836h",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT
        )
        
        // Initialize with or without custom button
        if (useCustomButton) {
            val customButton = createCustomButton()
            widgetButton.init(config, customButton)
            appendLog("Widget button initialized with custom button")
        } else {
            widgetButton.init(config)
            appendLog("Widget button initialized with default icon (fetching from API)")
        }
        
        // Set click listener
        widgetButton.setOnClickListener {
            openWidget()
        }
        
        // Add to container
        customButtonContainer.addView(widgetButton)
        
        // Set initial unread count
        widgetButton.updateUnreadCount(unreadCount)
    }
    
    private fun createCustomButton(): View {
        return Button(this).apply {
            text = "Chat with us"
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            // Use Material Design colors
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }
    
    private fun openWidget() {
        appendLog("Opening widget in full-screen mode")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra(WidgetActivity.EXTRA_USER_NAME, "Demo User")
            putExtra(WidgetActivity.EXTRA_USER_EMAIL, "demo@example.com")
            putExtra(WidgetActivity.EXTRA_USER_PHONE, "+1234567890")
        }
        
        startActivity(intent)
        appendLog("Widget activity launched")
    }
    
    private fun appendLog(logMessage: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val currentLog = logText.text.toString()
        val newLog = if (currentLog.isEmpty()) {
            "[$timestamp] $logMessage"
        } else {
            "$currentLog\n[$timestamp] $logMessage"
        }
        logText.text = newLog
        
        // Scroll to bottom
        logText.post {
            val scrollView = findViewById<ScrollView>(R.id.logScrollView)
            scrollView?.fullScroll(View.FOCUS_DOWN)
        }
        
        Log.d(TAG, logMessage)
    }
    
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        widgetButton.destroy()
    }
}