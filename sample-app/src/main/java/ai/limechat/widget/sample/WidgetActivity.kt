package ai.limechat.widget.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig

class WidgetActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "WidgetActivity"
        const val EXTRA_USER_NAME = "user_name"
        const val EXTRA_USER_EMAIL = "user_email"
        const val EXTRA_USER_PHONE = "user_phone"
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make the activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // Hide system UI for true full screen
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContentView(R.layout.activity_widget)
        
        initWidget()
    }
    
    private fun initWidget() {
        widgetView = findViewById(R.id.widgetView)
        
        // Initialize file picker
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get user details from intent
        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL)
        val userPhone = intent.getStringExtra(EXTRA_USER_PHONE)
        
        // Create user object
        val user = WidgetConfig.User(
            name = userName,
            email = userEmail,
            phoneNumber = userPhone
        )
        
        // Create widget config with hardcoded token
        val config = WidgetConfig(
            websiteToken = "MEFFACy4xaovJayhLjSt836h",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = user,
            customAttributes = mapOf(
                "source" to "android_sample_app",
                "version" to "1.0.0"
            )
        )
        
        // Initialize widget with callback
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "Widget loaded successfully")
            }
            
            override fun onClose() {
                Log.d(TAG, "Widget close requested")
                finish() // Close the activity when widget requests close
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "Widget error [${error.code}]: ${error.message}")
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d(TAG, "Widget message received: $message")
            }
        })
    }
    
    override fun onBackPressed() {
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        widgetView.destroy()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-hide system UI when focus is regained
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
}