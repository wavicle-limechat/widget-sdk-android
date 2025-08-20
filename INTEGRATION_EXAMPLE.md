# LimeChat Widget Android SDK - Integration Example

This page provides a complete example of how to integrate the published LimeChat Widget Android SDK into your Android application.

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK 23 (Android 6.0)
- Target SDK 35 (Android 15)
- Kotlin or Java project

## 🚀 Quick Start Example

### Step 1: Add Dependencies

Add JitPack repository and the SDK dependency to your app:

#### `settings.gradle.kts` (Gradle 8.0+)
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### Or `build.gradle` (Project level, older Gradle)
```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### `app/build.gradle.kts`
```kotlin
dependencies {
    // LimeChat Widget SDK
    implementation("com.github.wavicle-limechat:widget-sdk-android:1.0.0")
    
    // Required dependencies (if not already included)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.android.material:material:1.11.0")
}
```

### Step 2: Add Permissions

Add internet permission to `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Your app content -->
    
</manifest>
```

## 📱 Complete Example Activities

### Option 1: Widget Button Integration (Recommended)

This example shows how to integrate the new widget button that supports both custom and default icons.

#### `MainActivity.kt`
```kotlin
package com.example.limechatdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class MainActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var widgetButton: LimechatWidgetButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupLimeChatWidget()
    }
    
    private fun setupLimeChatWidget() {
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // Replace with your actual token
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john.doe@example.com",
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "source" to "android_app",
                "version" to "1.0.0",
                "user_type" to "premium"
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
        
        // Initialize widget button (will fetch icon from API by default)
        widgetButton.init(config)
        
        // Set click listener to open full-screen widget
        widgetButton.setOnClickListener {
            openFullScreenWidget()
        }
        
        // Add button to main container
        val mainContainer = findViewById<FrameLayout>(R.id.mainContainer)
        mainContainer.addView(widgetButton)
        
        // Update unread count (example)
        widgetButton.updateUnreadCount(3)
        
        Log.d(TAG, "LimeChat widget button initialized")
    }
    
    private fun openFullScreenWidget() {
        Log.d(TAG, "Opening full-screen widget")
        
        val intent = Intent(this, WidgetActivity::class.java).apply {
            putExtra("website_token", "YOUR_WEBSITE_TOKEN")
            putExtra("user_name", "John Doe")
            putExtra("user_email", "john.doe@example.com")
        }
        startActivity(intent)
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
```

#### `activity_main.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/mainContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/white">

    <!-- Your main app content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Welcome to LimeChat Demo"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="32dp" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="This app demonstrates the LimeChat Widget SDK integration. The widget button will appear in the bottom-right corner."
            android:textSize="16sp"
            android:gravity="center"
            android:layout_marginBottom="24dp" />

        <Button
            android:id="@+id/updateUnreadButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Simulate New Messages (Update Badge)"
            android:onClick="onUpdateUnreadClick" />

    </LinearLayout>

    <!-- Widget button will be added programmatically here -->

</FrameLayout>
```

### Option 2: Custom Button Integration

Example showing how to use your own custom button:

#### `CustomButtonActivity.kt`
```kotlin
package com.example.limechatdemo

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class CustomButtonActivity : FragmentActivity() {
    
    private lateinit var widgetButton: LimechatWidgetButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_button)
        
        setupCustomWidgetButton()
    }
    
    private fun setupCustomWidgetButton() {
        // Create your custom button
        val customButton = Button(this).apply {
            text = "💬 Need Help?"
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            setBackgroundColor(0xFF4CAF50.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
        }
        
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.AUTO
        )
        
        // Create widget button with custom button
        widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config, customButton)
        
        // The SDK handles click events automatically
        widgetButton.setOnClickListener {
            // Your custom logic here (optional)
            // The widget will still open automatically
        }
        
        // Add to layout
        val container = findViewById<FrameLayout>(R.id.customButtonContainer)
        container.addView(widgetButton)
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
```

### Option 3: Full-Screen Widget Integration

Example of a dedicated full-screen widget activity:

#### `WidgetActivity.kt`
```kotlin
package com.example.limechatdemo

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
    }
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make activity full-screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setContentView(R.layout.activity_widget)
        
        initWidget()
    }
    
    private fun initWidget() {
        widgetView = findViewById(R.id.widgetView)
        
        // Initialize file picker for file uploads
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get data from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "YOUR_DEFAULT_TOKEN"
        val userName = intent.getStringExtra("user_name")
        val userEmail = intent.getStringExtra("user_email")
        
        // Create user object
        val user = WidgetConfig.User(
            name = userName,
            email = userEmail
        )
        
        // Create widget configuration
        val config = WidgetConfig(
            websiteToken = websiteToken,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = user,
            customAttributes = mapOf(
                "source" to "android_fullscreen",
                "session_id" to System.currentTimeMillis().toString()
            )
        )
        
        // Initialize widget with callbacks
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d(TAG, "Widget loaded successfully")
                // Hide loading indicator if you have one
            }
            
            override fun onClose() {
                Log.d(TAG, "Widget close requested")
                finish() // Close the activity
            }
            
            override fun onError(error: WidgetError) {
                Log.e(TAG, "Widget error [${error.code}]: ${error.message}")
                // Handle error - show message to user, retry, etc.
                handleWidgetError(error)
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d(TAG, "Widget message received: $message")
                // Handle custom messages from widget
                handleWidgetMessage(message)
            }
        })
    }
    
    private fun handleWidgetError(error: WidgetError) {
        when (error.code) {
            WidgetError.ErrorCode.CONFIG_ERROR -> {
                // Handle configuration errors
                showError("Configuration Error: ${error.message}")
            }
            WidgetError.ErrorCode.NETWORK_ERROR -> {
                // Handle network errors
                showError("Network Error: Please check your internet connection")
            }
            WidgetError.ErrorCode.WEBVIEW_ERROR -> {
                // Handle WebView errors
                showError("Loading Error: ${error.message}")
            }
            else -> {
                showError("Error: ${error.message}")
            }
        }
    }
    
    private fun handleWidgetMessage(message: Map<String, Any>) {
        val event = message["event"] as? String
        
        when (event) {
            "unread-count-changed" -> {
                val count = message["count"] as? Int ?: 0
                // Update your app's unread count badge
                updateUnreadCount(count)
            }
            "conversation-started" -> {
                // Handle conversation started event
                Log.d(TAG, "Conversation started")
            }
            "agent-joined" -> {
                // Handle agent joined event  
                Log.d(TAG, "Agent joined the conversation")
            }
        }
    }
    
    private fun updateUnreadCount(count: Int) {
        // Update your app's unread count badge
        // This could be a notification badge, app badge, etc.
        Log.d(TAG, "Unread count updated: $count")
    }
    
    private fun showError(message: String) {
        // Show error to user (Toast, Snackbar, Dialog, etc.)
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
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
```

#### `activity_widget.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<ai.limechat.widget.LimechatWidgetView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/widgetView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## ⚙️ Configuration Options

### Basic Configuration
```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN" // Required
)
```

### Advanced Configuration
```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN",
    locale = "en", // or "es", "fr", etc.
    colorScheme = WidgetConfig.ColorScheme.AUTO, // LIGHT, DARK, or AUTO
    user = WidgetConfig.User(
        name = "User Name",
        email = "user@example.com",
        phoneNumber = "+1234567890",
        identifierHash = "unique_user_hash"
    ),
    customAttributes = mapOf(
        "department" to "support",
        "priority" to "high",
        "plan" to "premium",
        "user_id" to "12345"
    ),
    baseUrl = "https://app.limechat.ai" // Custom base URL if needed
)
```

### Unread Count Management
```kotlin
// Update unread count
widgetButton.updateUnreadCount(5)

// Clear unread count
widgetButton.updateUnreadCount(0)

// Handle unread count changes from widget
override fun onMessage(message: Map<String, Any>) {
    val event = message["event"] as? String
    if (event == "unread-count-changed") {
        val count = message["count"] as? Int ?: 0
        widgetButton.updateUnreadCount(count)
    }
}
```

## 🎨 Customization Examples

### Custom Button Styles
```kotlin
val customButton = Button(this).apply {
    text = "Contact Support"
    
    // Styling
    setBackgroundColor(Color.parseColor("#FF6B35"))
    setTextColor(Color.WHITE)
    textSize = 16f
    typeface = Typeface.DEFAULT_BOLD
    
    // Padding
    setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10))
    
    // Margins
    layoutParams = ViewGroup.MarginLayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
    }
}

widgetButton.init(config, customButton)
```

### Material Design Button
```kotlin
val materialButton = MaterialButton(this).apply {
    text = "Chat Now"
    icon = ContextCompat.getDrawable(context, R.drawable.ic_chat)
    iconGravity = MaterialButton.ICON_GRAVITY_START
    
    setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
    setTextColor(ContextCompat.getColor(context, R.color.onPrimary))
    
    cornerRadius = dpToPx(8)
    elevation = dpToPx(4).toFloat()
}
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Widget Not Loading
```kotlin
// Enable WebView debugging for development
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

#### 2. File Uploads Not Working
- Ensure your activity extends `FragmentActivity`
- Initialize `WidgetFilePicker` in `onCreate()`
- Attach file picker before initializing widget

#### 3. Network Issues
```kotlin
override fun onError(error: WidgetError) {
    when (error.code) {
        WidgetError.ErrorCode.NETWORK_ERROR -> {
            // Retry logic
            retryConnection()
        }
    }
}

private fun retryConnection() {
    // Implement retry logic
    Handler(Looper.getMainLooper()).postDelayed({
        widgetView.init(config, callback)
    }, 3000)
}
```

### ProGuard/R8 Rules

If using code obfuscation, add these rules to `proguard-rules.pro`:

```proguard
# LimeChat Widget SDK
-keep class ai.limechat.widget.** { *; }
-keep interface ai.limechat.widget.** { *; }

# WebView JavaScript Interface
-keepclassmembers class ai.limechat.widget.** {
    @android.webkit.JavascriptInterface <methods>;
}
```

## 📱 Testing Your Integration

### 1. Basic Functionality Test
```kotlin
class WidgetIntegrationTest {
    
    @Test
    fun testWidgetConfiguration() {
        val config = WidgetConfig(
            websiteToken = "test-token",
            locale = "en"
        )
        
        assertNotNull(config)
        assertEquals("test-token", config.websiteToken)
        assertEquals("en", config.locale)
    }
    
    @Test
    fun testWidgetButton() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widgetButton = LimechatWidgetButton(context as FragmentActivity)
        
        assertNotNull(widgetButton)
    }
}
```

### 2. Manual Testing Checklist
- [ ] Widget button appears correctly
- [ ] Click opens widget/activity
- [ ] File upload works
- [ ] Back navigation works
- [ ] Error handling works
- [ ] Unread count updates
- [ ] Custom attributes are sent
- [ ] User information is passed

## 📚 Additional Resources

- **SDK Documentation**: [Widget Button Guide](WIDGET_BUTTON_GUIDE.md)
- **API Reference**: [README.md](README.md)
- **JitPack Status**: https://jitpack.io/#wavicle-limechat/widget-sdk-android
- **Sample App**: See `sample-app/` directory

## 💡 Best Practices

1. **Always handle errors** in your `WidgetCallback`
2. **Initialize file picker in onCreate()** for file uploads
3. **Clean up resources** in `onDestroy()`
4. **Use appropriate color schemes** for user experience
5. **Test on different devices** and Android versions
6. **Handle network connectivity** issues gracefully

## 🆘 Support

If you encounter issues:

1. Check the [troubleshooting section](#-troubleshooting)
2. Enable WebView debugging for development
3. Review the sample app implementation
4. Create an issue on GitHub with logs and device information

---

**Happy coding! 🚀**