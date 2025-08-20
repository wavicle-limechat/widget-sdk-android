package ai.limechat.widget.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var toggleWidgetButton: Button
    private lateinit var buttonDemoButton: Button
    private lateinit var logText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        
        appendLog("App initialized")
    }
    
    private fun initViews() {
        toggleWidgetButton = findViewById(R.id.toggleWidgetButton)
        buttonDemoButton = findViewById(R.id.buttonDemoButton)
        logText = findViewById(R.id.logText)
    }
    
    private fun setupListeners() {
        toggleWidgetButton.setOnClickListener {
            openWidget()
        }
        
        buttonDemoButton.setOnClickListener {
            openButtonDemo()
        }
    }
    
    private fun openWidget() {
        appendLog("Opening widget in full-screen mode")
        
        // Create intent with hardcoded user details
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra(WidgetActivity.EXTRA_USER_NAME, "John Doe")
            putExtra(WidgetActivity.EXTRA_USER_EMAIL, "john.doe@example.com")
            putExtra(WidgetActivity.EXTRA_USER_PHONE, "+1234567890")
        }
        
        startActivity(intent)
        appendLog("Widget activity launched")
    }
    
    private fun openButtonDemo() {
        appendLog("Opening button demo activity")
        
        val intent = Intent(this, ButtonDemoActivity::class.java)
        startActivity(intent)
        appendLog("Button demo activity launched")
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
}