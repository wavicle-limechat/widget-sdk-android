package ai.limechat.widget.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig

class MainActivity : androidx.fragment.app.FragmentActivity() {
    
    companion object {
        private const val TAG = "SampleApp"
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    private lateinit var websiteTokenInput: EditText
    private lateinit var userNameInput: EditText
    private lateinit var userEmailInput: EditText
    private lateinit var userPhoneInput: EditText
    private lateinit var localeSpinner: Spinner
    private lateinit var colorSchemeSpinner: Spinner
    private lateinit var initButton: Button
    private lateinit var sendMessageButton: Button
    private lateinit var messageInput: EditText
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var customTriggerButton: Button
    private lateinit var hideWidgetButton: Button
    private lateinit var widgetPlaceholder: TextView
    
    private var isWidgetInitialized = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupSpinners()
        setupListeners()
        
        // Initialize file picker in onCreate to register ActivityResultLauncher properly
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Pre-fill with demo values
        websiteTokenInput.setText("demo-token") // Replace with actual demo token
    }
    
    private fun initViews() {
        widgetView = findViewById(R.id.widgetView)
        websiteTokenInput = findViewById(R.id.websiteTokenInput)
        userNameInput = findViewById(R.id.userNameInput)
        userEmailInput = findViewById(R.id.userEmailInput)
        userPhoneInput = findViewById(R.id.userPhoneInput)
        localeSpinner = findViewById(R.id.localeSpinner)
        colorSchemeSpinner = findViewById(R.id.colorSchemeSpinner)
        initButton = findViewById(R.id.initButton)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageInput = findViewById(R.id.messageInput)
        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        customTriggerButton = findViewById(R.id.customTriggerButton)
        hideWidgetButton = findViewById(R.id.hideWidgetButton)
        widgetPlaceholder = findViewById(R.id.widgetPlaceholder)
    }
    
    private fun setupSpinners() {
        // Locale spinner
        val locales = arrayOf("en", "es", "fr", "de", "it", "pt")
        val localeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locales)
        localeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        localeSpinner.adapter = localeAdapter
        
        // Color scheme spinner
        val colorSchemes = arrayOf("Light", "Dark", "Auto")
        val colorSchemeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorSchemes)
        colorSchemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSchemeSpinner.adapter = colorSchemeAdapter
    }
    
    private fun setupListeners() {
        initButton.setOnClickListener {
            initWidget()
        }
        
        sendMessageButton.setOnClickListener {
            sendMessage()
        }
        
        customTriggerButton.setOnClickListener {
            toggleWidget(true)
        }
        
        hideWidgetButton.setOnClickListener {
            toggleWidget(false)
        }
    }
    
    private fun initWidget() {
        val websiteToken = websiteTokenInput.text.toString().trim()
        
        if (websiteToken.isEmpty()) {
            showError("Website token is required")
            return
        }
        
        try {
            val user = WidgetConfig.User(
                name = userNameInput.text.toString().takeIf { it.isNotBlank() },
                email = userEmailInput.text.toString().takeIf { it.isNotBlank() },
                phoneNumber = userPhoneInput.text.toString().takeIf { it.isNotBlank() }
            )
            
            val locale = localeSpinner.selectedItem as String
            val colorScheme = when (colorSchemeSpinner.selectedItemPosition) {
                0 -> WidgetConfig.ColorScheme.LIGHT
                1 -> WidgetConfig.ColorScheme.DARK
                2 -> WidgetConfig.ColorScheme.AUTO
                else -> WidgetConfig.ColorScheme.LIGHT
            }
            
            val customAttributes = mapOf(
                "sample_app" to "android",
                "version" to "1.0.0"
            )
            
            val config = WidgetConfig(
                websiteToken = websiteToken,
                locale = locale,
                colorScheme = colorScheme,
                user = user,
                customAttributes = customAttributes
            )
            
            widgetView.init(config, object : WidgetCallback {
                override fun onLoaded() {
                    runOnUiThread {
                        updateStatus("Widget loaded successfully")
                        appendLog("Widget loaded")
                        sendMessageButton.isEnabled = true
                        isWidgetInitialized = true
                        widgetView.visibility = View.VISIBLE
                        widgetPlaceholder.visibility = View.GONE
                    }
                }
                
                override fun onClose() {
                    runOnUiThread {
                        updateStatus("Widget closed")
                        appendLog("Widget closed")
                    }
                }
                
                override fun onError(error: WidgetError) {
                    runOnUiThread {
                        updateStatus("Error: ${error.message}")
                        appendLog("Error [${error.code}]: ${error.message}")
                    }
                }
                
                override fun onMessage(message: Map<String, Any>) {
                    runOnUiThread {
                        val messageStr = message.toString()
                        appendLog("Message received: $messageStr")
                    }
                }
            })
            
            updateStatus("Initializing widget...")
            appendLog("Widget initialization started")
            
        } catch (e: Exception) {
            showError("Failed to initialize widget: ${e.message}")
        }
    }
    
    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        
        if (message.isEmpty()) {
            showError("Message cannot be empty")
            return
        }
        
        try {
            widgetView.sendMessage("custom-message", mapOf("text" to message))
            appendLog("Sent message: $message")
            messageInput.text.clear()
        } catch (e: Exception) {
            showError("Failed to send message: ${e.message}")
        }
    }
    
    private fun toggleWidget(show: Boolean) {
        if (!isWidgetInitialized) {
            showError("Please initialize the widget first")
            return
        }
        
        if (show) {
            widgetView.visibility = View.VISIBLE
            widgetPlaceholder.visibility = View.GONE
            customTriggerButton.text = "Widget Shown"
            customTriggerButton.isEnabled = false
            hideWidgetButton.isEnabled = true
            appendLog("Widget shown via custom button")
        } else {
            widgetView.visibility = View.GONE
            widgetPlaceholder.visibility = View.VISIBLE
            customTriggerButton.text = "Show Widget"
            customTriggerButton.isEnabled = true
            hideWidgetButton.isEnabled = false
            appendLog("Widget hidden via custom button")
        }
    }
    
    private fun updateStatus(status: String) {
        statusText.text = "Status: $status"
        Log.d(TAG, status)
    }
    
    private fun appendLog(logMessage: String) {
        val currentLog = logText.text.toString()
        val newLog = if (currentLog.isEmpty()) {
            logMessage
        } else {
            "$currentLog\n$logMessage"
        }
        logText.text = newLog
        
        // Scroll to bottom
        logText.post {
            val scrollView = findViewById<ScrollView>(R.id.logScrollView)
            scrollView?.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun showError(error: String) {
        updateStatus(error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
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
}