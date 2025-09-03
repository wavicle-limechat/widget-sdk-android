# LimeChat Widget Android SDK - Client Integration Guide

**Official Android SDK for integrating LimeChat widget into your Android applications**

Version: **0.0.5** | Repository: `com.github.wavicle-limechat:widget-sdk-android` | Platform: **Android API 23+**

---

## 📋 Table of Contents

1. [Quick Start](#-quick-start)
2. [Installation](#-installation)
3. [Basic Integration](#-basic-integration)
4. [Advanced Configuration](#-advanced-configuration)
5. [Widget Button Integration](#-widget-button-integration)
6. [Full-Screen Widget](#-full-screen-widget)
7. [User Management](#-user-management)
8. [Customization Options](#-customization-options)
9. [File Upload Support](#-file-upload-support)
10. [Event Handling](#-event-handling)
11. [Troubleshooting](#-troubleshooting)
12. [API Reference](#-api-reference)

---

## 🚀 Quick Start

Get up and running with LimeChat widget in 5 minutes:

### 1. Add Dependency
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.5")
}
```

### 2. Add Repository
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 3. Basic Widget Integration
```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // Get this from LimeChat dashboard
            locale = "en",
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            )
        )
        
        // Create and add widget button
        val widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)
        
        // Position in bottom-right corner
        val container = findViewById<FrameLayout>(R.id.main_container)
        widgetButton.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 16.dp, 16.dp)
        }
        
        container.addView(widgetButton)
    }
}
```

---

## 📦 Installation

### Requirements

- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 1.8+
- **AndroidX**: Required

### Step 1: Repository Configuration

Add JitPack repository to your project:

**For Gradle 7.0+** (recommended):
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**For older Gradle versions**:
```kotlin
// build.gradle (Project level)
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add Dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.5")
    
    // Required AndroidX dependencies (if not already included)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.11.0")
}
```

### Step 3: AndroidX Configuration

Ensure your `gradle.properties` includes:
```properties
android.useAndroidX=true
android.enableJetifier=true
android.nonTransitiveRClass=true
```

### Step 4: Permissions

Add internet permission to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🎯 Basic Integration

### Widget Button (Floating Button)

The simplest integration - adds a floating chat button to your app:

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var widgetButton: LimechatWidgetButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupLimeChatWidget()
    }
    
    private fun setupLimeChatWidget() {
        // Configuration
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.AUTO,
            user = WidgetConfig.User(
                name = "Customer Name",
                email = "customer@company.com",
                phoneNumber = "+1234567890"
            )
        )
        
        // Create widget button
        widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)
        
        // Optional: Set click listener for custom behavior
        widgetButton.setOnClickListener {
            // Widget opens automatically, but you can add custom logic here
            Log.d("LimeChat", "Widget button clicked")
        }
        
        // Add to your layout
        val mainContainer = findViewById<FrameLayout>(R.id.main_container)
        widgetButton.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, dpToPx(16), dpToPx(16))
        }
        
        mainContainer.addView(widgetButton)
    }
    
    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetButton.isInitialized) {
            widgetButton.destroy()
        }
    }
}
```

---

## ⚙️ Advanced Configuration

### WidgetConfig Options

```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN",    // Required: Your LimeChat website token
    locale = "en",                          // Optional: Language code (en, es, fr, etc.)
    colorScheme = WidgetConfig.ColorScheme.AUTO, // Optional: LIGHT, DARK, AUTO
    user = WidgetConfig.User(               // Optional: User information
        name = "John Doe",
        email = "john@company.com", 
        phoneNumber = "+1234567890",
        identifierHash = "user_hash_123"    // Optional: For secure mode
    ),
    customAttributes = mapOf(               // Optional: Custom metadata
        "user_type" to "premium",
        "plan" to "enterprise",
        "source" to "mobile_app"
    ),
    baseUrl = "https://app.limechat.ai"     // Optional: Custom base URL
)
```

### Color Schemes

```kotlin
// Adapts to system theme (recommended)
colorScheme = WidgetConfig.ColorScheme.AUTO

// Always light theme
colorScheme = WidgetConfig.ColorScheme.LIGHT

// Always dark theme  
colorScheme = WidgetConfig.ColorScheme.DARK
```

### Custom Attributes

Pass custom data to your chat widget:

```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    customAttributes = mapOf(
        "user_id" to "12345",
        "subscription_tier" to "premium",
        "app_version" to BuildConfig.VERSION_NAME,
        "platform" to "android",
        "session_id" to UUID.randomUUID().toString()
    )
)
```

---

## 🎈 Widget Button Integration

### Default Widget Button

The SDK automatically fetches your widget icon from LimeChat dashboard:

```kotlin
// Minimal setup - uses default icon from your LimeChat settings
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config)
```

### Custom Widget Button

Use your own button design:

```kotlin
// Create your custom button
val customButton = Button(this).apply {
    text = "Chat with us"
    background = ContextCompat.getDrawable(context, R.drawable.custom_chat_button)
}

// Initialize widget with custom button
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config, customButton)
```

### Widget Button Positioning

```kotlin
// Bottom-right (recommended)
widgetButton.layoutParams = FrameLayout.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
    gravity = Gravity.BOTTOM or Gravity.END
    setMargins(0, 0, 16.dp, 16.dp)
}

// Bottom-left
widgetButton.layoutParams = FrameLayout.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
    gravity = Gravity.BOTTOM or Gravity.START
    setMargins(16.dp, 0, 0, 16.dp)
}
```

### Unread Message Badge

Display unread message count on the widget button:

```kotlin
// Update badge count (call this when you receive unread count from your backend)
widgetButton.updateUnreadCount(5)

// Hide badge
widgetButton.updateUnreadCount(0)
```

---

## 🖥️ Full-Screen Widget

For immersive chat experience, open widget in full-screen mode:

### Create Widget Activity

```kotlin
class ChatActivity : FragmentActivity() {
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create full-screen widget
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        // Setup file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get configuration (passed from previous activity or create new)
        val websiteToken = intent.getStringExtra("website_token") ?: "YOUR_TOKEN"
        val userName = intent.getStringExtra("user_name") ?: "User"
        val userEmail = intent.getStringExtra("user_email") ?: ""
        
        val config = WidgetConfig(
            websiteToken = websiteToken,
            user = WidgetConfig.User(name = userName, email = userEmail)
        )
        
        // Initialize with callbacks
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d("LimeChat", "Widget loaded successfully")
            }
            
            override fun onClose() {
                finish() // Close activity when user closes widget
            }
            
            override fun onError(error: WidgetError) {
                Log.e("LimeChat", "Widget error: ${error.message}")
                showErrorAndClose(error)
            }
            
            override fun onMessage(message: Map<String, Any>) {
                handleWidgetMessage(message)
            }
        })
    }
    
    override fun onBackPressed() {
        // Let widget handle navigation first
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (this::widgetView.isInitialized) {
            widgetView.destroy()
        }
    }
    
    private fun showErrorAndClose(error: WidgetError) {
        Toast.makeText(this, "Chat unavailable: ${error.message}", Toast.LENGTH_LONG).show()
        finish()
    }
    
    private fun handleWidgetMessage(message: Map<String, Any>) {
        when (message["event"]) {
            "unread-count-changed" -> {
                val count = message["count"] as? Int ?: 0
                // Update your app's notification badge
                updateAppBadgeCount(count)
            }
            "conversation-started" -> {
                // Track analytics event
            }
        }
    }
}
```

### Add to AndroidManifest.xml

```xml
<activity
    android:name=".ChatActivity"
    android:exported="false"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:windowSoftInputMode="adjustResize" />
```

### Launch Full-Screen Widget

```kotlin
// From your main activity
private fun openFullScreenChat() {
    val intent = Intent(this, ChatActivity::class.java).apply {
        putExtra("website_token", "YOUR_TOKEN")
        putExtra("user_name", "John Doe")
        putExtra("user_email", "john@company.com")
    }
    startActivity(intent)
}
```

---

## 👤 User Management

### Basic User Information

```kotlin
val user = WidgetConfig.User(
    name = "John Doe",
    email = "john@company.com",
    phoneNumber = "+1234567890"
)

val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    user = user
)
```

### Dynamic User Updates

Update user information after login:

```kotlin
// After user logs in
private fun updateChatUserInfo(user: User) {
    val chatUser = WidgetConfig.User(
        name = user.fullName,
        email = user.email,
        phoneNumber = user.phoneNumber
    )
    
    val newConfig = WidgetConfig(
        websiteToken = "YOUR_TOKEN",
        user = chatUser,
        customAttributes = mapOf(
            "user_id" to user.id,
            "account_type" to user.accountType,
            "registration_date" to user.registrationDate
        )
    )
    
    // Reinitialize widget with new user info
    widgetButton.init(newConfig)
}
```

### Secure Mode (Hash Verification)

For enhanced security, use identifier hash:

```kotlin
// Generate hash on your backend using the user's ID and LimeChat secret
val userHash = generateUserHash(userId, limeChatSecret) // Implement this on backend

val user = WidgetConfig.User(
    name = "John Doe",
    email = "john@company.com",
    identifierHash = userHash
)
```

---

## 🎨 Customization Options

### Widget Button Styling

```kotlin
// Custom button with your branding
val customButton = MaterialButton(this).apply {
    text = "💬 Chat Support"
    setBackgroundColor(ContextCompat.getColor(context, R.color.brand_primary))
    setTextColor(ContextCompat.getColor(context, R.color.white))
    cornerRadius = 24.dp
    elevation = 8f
    icon = ContextCompat.getDrawable(context, R.drawable.ic_chat)
}

widgetButton.init(config, customButton)
```

### Theme Integration

Match your app's theme:

```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    colorScheme = when {
        isDarkModeEnabled() -> WidgetConfig.ColorScheme.DARK
        isLightModeEnabled() -> WidgetConfig.ColorScheme.LIGHT
        else -> WidgetConfig.ColorScheme.AUTO
    }
)
```

---

## 📎 File Upload Support

Enable file uploads in chat conversations:

### Setup File Picker

```kotlin
class ChatActivity : FragmentActivity() {
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        // IMPORTANT: Create file picker in onCreate for proper activity result handling
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // ... rest of initialization
    }
}
```

### File Upload Permissions

Add to `AndroidManifest.xml` if needed:

```xml
<!-- For camera access -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- For file access (API < 29) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### File Upload Events

Handle file upload events:

```kotlin
override fun onMessage(message: Map<String, Any>) {
    when (message["event"]) {
        "file-upload-started" -> {
            val fileName = message["file_name"] as? String
            showFileUploadProgress(fileName)
        }
        "file-upload-completed" -> {
            val fileUrl = message["file_url"] as? String
            hideFileUploadProgress()
        }
        "file-upload-failed" -> {
            val error = message["error"] as? String
            showFileUploadError(error)
        }
    }
}
```

---

## 📡 Event Handling

### Widget Callback Implementation

```kotlin
val callback = object : WidgetCallback {
    override fun onLoaded() {
        Log.d("LimeChat", "✅ Widget loaded successfully")
        hideLoadingIndicator()
    }
    
    override fun onClose() {
        Log.d("LimeChat", "🚪 Widget close requested")
        finish() // Close activity or handle as needed
    }
    
    override fun onError(error: WidgetError) {
        Log.e("LimeChat", "❌ Widget error: ${error.message}")
        when (error.code) {
            WidgetError.ErrorCode.NETWORK_ERROR -> {
                showRetryDialog("Network connection failed")
            }
            WidgetError.ErrorCode.CONFIG_ERROR -> {
                showErrorDialog("Configuration error: ${error.message}")
            }
            else -> {
                showErrorDialog("An error occurred: ${error.message}")
            }
        }
    }
    
    override fun onMessage(message: Map<String, Any>) {
        handleWidgetMessage(message)
    }
}

widgetView.init(config, callback)
```

### Message Event Types

```kotlin
private fun handleWidgetMessage(message: Map<String, Any>) {
    val event = message["event"] as? String
    
    when (event) {
        "unread-count-changed" -> {
            val count = message["count"] as? Int ?: 0
            updateAppBadge(count)
        }
        
        "conversation-started" -> {
            val conversationId = message["conversation_id"] as? String
            trackAnalytics("chat_started", mapOf("conversation_id" to conversationId))
        }
        
        "agent-joined" -> {
            val agentName = message["agent_name"] as? String
            showNotification("$agentName joined the conversation")
        }
        
        "message-received" -> {
            val messageText = message["text"] as? String
            val senderId = message["sender_id"] as? String
            // Handle new message
        }
        
        "typing-started" -> {
            showTypingIndicator()
        }
        
        "typing-stopped" -> {
            hideTypingIndicator()
        }
    }
}
```

### Send Custom Messages

```kotlin
// Send custom events to the widget
widgetView.sendMessage("user_action", mapOf(
    "action" to "viewed_product",
    "product_id" to "12345",
    "product_name" to "Premium Plan"
))
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. "Could not resolve dependency" Error

**Problem**: JitPack repository not configured or dependency not found.

**Solution**:
```kotlin
// Ensure JitPack is in your repositories
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

// Use exact version
implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.5")
```

#### 2. AndroidX Compatibility Issues

**Problem**: "Configuration ':app:debugRuntimeClasspath' contains AndroidX dependencies"

**Solution**: Add to `gradle.properties`:
```properties
android.useAndroidX=true
android.enableJetifier=true
```

#### 3. Widget Not Loading

**Problem**: Widget shows blank or loading indefinitely.

**Solutions**:
- Verify your website token is correct
- Check internet permission in AndroidManifest.xml
- Ensure WebView is enabled on device
- Check logs for error messages

```kotlin
// Add logging to debug
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN" // Verify this token
)

widgetView.init(config, object : WidgetCallback {
    override fun onError(error: WidgetError) {
        Log.e("LimeChat", "Widget error: ${error.message}")
        Log.e("LimeChat", "Error code: ${error.code}")
        Log.e("LimeChat", "Error context: ${error.context}")
    }
})
```

#### 4. File Upload Not Working

**Problem**: File picker doesn't open or uploads fail.

**Solutions**:
- Ensure WidgetFilePicker is created in onCreate()
- Add required permissions to AndroidManifest.xml
- Use FragmentActivity instead of AppCompatActivity

```kotlin
// Correct file picker setup
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    widgetView = LimechatWidgetView(this)
    // IMPORTANT: Create in onCreate for proper lifecycle
    widgetFilePicker = WidgetFilePicker(this)
    widgetView.attachFilePicker(widgetFilePicker)
}
```

#### 5. ProGuard/R8 Issues

**Problem**: Widget crashes in release builds.

**Solution**: Add to `proguard-rules.pro`:
```proguard
# LimeChat Widget SDK
-keep class ai.limechat.widget.** { *; }
-keep interface ai.limechat.widget.** { *; }

# WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView related classes
-keep class * extends android.webkit.WebViewClient
-keep class * extends android.webkit.WebChromeClient
```

### Debug Mode

Enable detailed logging:

```kotlin
// Add to your Application class or main activity
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

### Network Issues

Test widget connectivity:

```kotlin
private fun testWidgetConnectivity() {
    val testUrl = "https://app.limechat.ai/widget?website_token=YOUR_TOKEN"
    
    // Test URL accessibility
    Thread {
        try {
            val connection = URL(testUrl).openConnection()
            connection.connectTimeout = 5000
            connection.connect()
            Log.d("LimeChat", "✅ Widget URL accessible")
        } catch (e: Exception) {
            Log.e("LimeChat", "❌ Widget URL not accessible: ${e.message}")
        }
    }.start()
}
```

---

## 📚 API Reference

### WidgetConfig

```kotlin
data class WidgetConfig(
    val websiteToken: String,                    // Required: Your LimeChat website token
    val locale: String = "en",                   // Language code
    val colorScheme: ColorScheme = ColorScheme.LIGHT, // Theme preference
    val user: User? = null,                      // User information
    val customAttributes: Map<String, Any>? = null, // Custom metadata
    val baseUrl: String = "https://app.limechat.ai" // Base URL for widget
)

data class User(
    val name: String? = null,                    // User's display name
    val email: String? = null,                   // User's email address
    val phoneNumber: String? = null,             // User's phone number
    val identifierHash: String? = null           // Security hash for verification
)

enum class ColorScheme {
    LIGHT,    // Light theme
    DARK,     // Dark theme  
    AUTO      // Follows system theme
}
```

### LimechatWidgetButton

```kotlin
class LimechatWidgetButton(context: Context) {
    
    // Initialize widget button
    fun init(config: WidgetConfig, customButton: View? = null)
    
    // Update unread message count badge
    fun updateUnreadCount(count: Int)
    
    // Set click listener
    fun setOnClickListener(listener: OnClickListener?)
    
    // Clean up resources
    fun destroy()
    
    // Get SDK version
    companion object {
        fun getSDKVersion(): String
    }
}
```

### LimechatWidgetView

```kotlin
class LimechatWidgetView(context: Context) {
    
    // Initialize full-screen widget
    fun init(config: WidgetConfig, callback: WidgetCallback? = null)
    
    // Attach file picker for uploads
    fun attachFilePicker(filePicker: WidgetFilePicker)
    
    // Send custom messages to widget
    fun sendMessage(event: String, data: Map<String, Any> = emptyMap())
    
    // Handle back button press
    fun onBackPressed(): Boolean
    
    // Clean up resources
    fun destroy()
}
```

### WidgetCallback

```kotlin
interface WidgetCallback {
    fun onLoaded()                              // Widget finished loading
    fun onClose()                              // User requested to close widget
    fun onError(error: WidgetError)            // An error occurred
    fun onMessage(message: Map<String, Any>)   // Message received from widget
}
```

### WidgetError

```kotlin
data class WidgetError(
    val code: ErrorCode,        // Error type
    val message: String,        // Human readable message
    val context: String? = null // Additional context
)

enum class ErrorCode {
    CONFIG_ERROR,       // Configuration problem
    NETWORK_ERROR,      // Network connectivity issue
    WEBVIEW_ERROR,      // WebView loading problem
    JAVASCRIPT_ERROR,   // JavaScript execution error
    FILE_PICKER_ERROR,  // File upload problem
    UNKNOWN_ERROR       // Other errors
}
```

---

## 🎯 Best Practices

### Performance Optimization

1. **Initialize widget lazily**:
```kotlin
private val widgetButton by lazy { LimechatWidgetButton(this) }
```

2. **Properly clean up resources**:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    widgetButton.destroy()
    widgetView?.destroy()
}
```

3. **Handle configuration changes**:
```kotlin
// Add to AndroidManifest.xml
android:configChanges="orientation|keyboardHidden|screenSize"
```

### Security Considerations

1. **Use identifier hash for user verification**
2. **Validate website token on your backend**
3. **Implement proper session management**
4. **Sanitize custom attributes before sending**

### User Experience

1. **Show loading states while widget initializes**
2. **Handle network errors gracefully**
3. **Provide fallback contact methods**
4. **Test on various screen sizes and orientations**

---

## 📞 Support

### Getting Help

- **Documentation**: This integration guide
- **Issues**: Report bugs or feature requests on GitHub
- **Support**: Contact LimeChat support team
- **Community**: Join our developer community

### Version Updates

Stay updated with the latest SDK versions:
- **Current Version**: 0.0.5
- **Changelog**: Check GitHub releases
- **Migration Guides**: Available for major version updates

---

## 📄 License

LimeChat Widget Android SDK is distributed under the MIT License. See LICENSE file for details.

---

**Ready to integrate? Start with the [Quick Start](#-quick-start) section and have your chat widget running in minutes!**

For more examples, check out our [sample applications](./published-sdk-demo/) in this repository.