# Limechat Android SDK Integration Guide

## Table of Contents

- [Installation](#installation)
- [Basic Integration](#basic-integration)
- [Full-Screen Modal Implementation](#full-screen-modal-implementation)
- [Custom Messages Feature](#custom-messages-feature)
- [Widget Button Integration](#widget-button-integration)
- [Customization Options](#customization-options)
- [API Reference](#api-reference)
- [Comparison with React Native SDK](#comparison-with-react-native-sdk)
- [Migration Guide](#migration-guide)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Installation

### Step 1: Add the SDK to your project

Add the Limechat Android SDK dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("ai.limechat:widget-sdk:1.0.0")
}
```

### Step 2: Add Internet Permission

In your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Basic Integration

### Step 1: Add Widget to Layout

Add the `LimechatWidgetView` to your activity layout:

```xml
<ai.limechat.widget.LimechatWidgetView
    android:id="@+id/widgetView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Step 2: Initialize in Activity

```kotlin
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage

class YourActivity : FragmentActivity() {

    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_activity_layout)

        // Initialize widget
        widgetView = findViewById(R.id.widgetView)

        // Setup file picker (required for file uploads)
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)

        // Configure user details
        val user = WidgetConfig.User(
            name = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+1234567890",
        )

        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // e.g., "MEFFACy4xaovJayhLjSt836h"
            colorScheme = WidgetConfig.ColorScheme.LIGHT, // LIGHT, DARK, or AUTO
            user = user,
            customAttributes = mapOf(
                "source" to "android_app",
                "version" to "1.0.0"
            )
        )

        // Initialize with callbacks
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d("Limechat", "Widget loaded successfully")
            }

            override fun onClose() {
                Log.d("Limechat", "Widget close requested")
                // Handle close action (e.g., hide widget or close activity)
            }

            override fun onError(error: WidgetError) {
                Log.e("Limechat", "Error: ${error.message}")
                // Handle different error types
                when (error.code) {
                    WidgetError.ErrorCode.NETWORK_ERROR -> {
                        // Handle network issues
                    }
                    WidgetError.ErrorCode.INITIALIZATION_ERROR -> {
                        // Handle initialization failures
                    }
                    else -> {
                        // Handle other errors
                    }
                }
            }

            override fun onMessage(message: Map<String, Any>) {
                Log.d("Limechat", "Message received: $message")
                // Handle custom messages from widget
            }
        })
    }

    override fun onBackPressed() {
        // Handle back button for WebView navigation
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

## Full-Screen Modal Implementation

For a full-screen modal experience (recommended), create a dedicated activity:

### Step 1: Create Widget Activity

```kotlin
package com.yourapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig
import ai.limechat.widget.models.WidgetMessage

class LimechatWidgetActivity : FragmentActivity() {

    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limechat_widget)

        initWidget()
    }

    private fun initWidget() {
        widgetView = findViewById(R.id.widgetView)

        // Setup file picker
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)

        // Get configuration from intent
        val websiteToken = intent.getStringExtra("website_token") ?: "YOUR_DEFAULT_TOKEN"
        val userName = intent.getStringExtra("user_name") ?: ""
        val userEmail = intent.getStringExtra("user_email") ?: ""
        val userPhone = intent.getStringExtra("user_phone") ?: ""

        val user = WidgetConfig.User(
            name = userName,
            email = userEmail,
            phoneNumber = userPhone
        )

        val config = WidgetConfig(
            websiteToken = websiteToken,
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = user,
            customAttributes = mapOf(
                "source" to "android_app",
                "activity" to "widget_modal"
            )
        )

        // Check for custom message
        val initialMessage = parseInitialMessage()

        // Initialize with optional initial message
        if (initialMessage != null) {
            widgetView.init(config, createCallback(), initialMessage)
        } else {
            widgetView.init(config, createCallback())
        }
    }

    private fun parseInitialMessage(): WidgetMessage? {
        return when (intent.getStringExtra("message_type")) {
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
    }

    private fun createCallback(): WidgetCallback {
        return object : WidgetCallback {
            override fun onLoaded() {
                Log.d("Limechat", "Widget loaded successfully")
            }

            override fun onClose() {
                Log.d("Limechat", "Widget close requested")
                finish() // Close the activity
            }

            override fun onError(error: WidgetError) {
                Log.e("Limechat", "Widget error: ${error.message}")
                // Handle error gracefully
            }

            override fun onMessage(message: Map<String, Any>) {
                Log.d("Limechat", "Widget message: $message")
                // Handle custom messages
            }
        }
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

### Step 2: Create Layout

`res/layout/activity_limechat_widget.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="false">

    <ai.limechat.widget.LimechatWidgetView
        android:id="@+id/widgetView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

### Step 3: Register Activity

In `AndroidManifest.xml`:

```xml
<activity
    android:name=".LimechatWidgetActivity"
    android:exported="false"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:windowSoftInputMode="adjustResize" />
```

## Custom Messages Feature

### Overview

The SDK now supports opening the widget with pre-filled custom messages, allowing for contextual conversations.

### Message Types

#### 1. Text Messages (Simple)

```kotlin
val message = WidgetMessage.Text("Hi, I need help with my order!")
```

#### 2. Structured Messages (Advanced)

```kotlin
val message = WidgetMessage.Structured(
    content = "I have a question about your pricing",
    properties = mapOf(
        "type" to "inquiry",
        "priority" to "high",
        "category" to "billing",
        "user_id" to "12345",
        "source" to "mobile_app"
    )
)
```

### Usage Examples

#### 1. Initialize Widget with Message

```kotlin
// During widget initialization
val initialMessage = WidgetMessage.Text("Hello! I need help with my account.")
widgetView.init(config, callback, initialMessage)
```

#### 2. Open Widget with Message (Runtime)

```kotlin
// After widget is loaded
val message = WidgetMessage.Structured(
    content = "I'm having trouble with checkout",
    properties = mapOf(
        "page" to "checkout",
        "error_code" to "payment_failed",
        "amount" to "99.99"
    )
)
widgetView.open(message)
```

#### 3. Launch Modal with Message

```kotlin
// Launch widget activity with custom message
fun openWidgetWithSupportRequest() {
    val intent = Intent(this, LimechatWidgetActivity::class.java).apply {
        putExtra("website_token", "YOUR_TOKEN")
        putExtra("user_name", "John Doe")
        putExtra("user_email", "john@example.com")
        
        // Add custom message
        putExtra("message_type", "structured")
        putExtra("message_content", "I need help with my recent order")
        putExtra("message_properties", HashMap(mapOf(
            "order_id" to "ORD-12345",
            "issue_type" to "refund",
            "urgency" to "high"
        )))
    }
    startActivity(intent)
}
```

### Real-World Use Cases

#### 1. Order Support

```kotlin
fun showOrderSupport(orderId: String) {
    val message = WidgetMessage.Structured(
        content = "I need help with my recent order",
        properties = mapOf(
            "order_id" to orderId,
            "issue_type" to "order_status",
            "source" to "order_details_page"
        )
    )
    
    val intent = Intent(this, LimechatWidgetActivity::class.java).apply {
        putExtra("message_type", "structured")
        putExtra("message_content", message.content)
        putExtra("message_properties", HashMap(message.properties))
    }
    startActivity(intent)
}
```

#### 2. Feature Feedback

```kotlin
fun showFeatureFeedback(featureName: String) {
    val message = WidgetMessage.Text("I have feedback about the $featureName feature")
    
    // Open widget with pre-filled message
    widgetView.open(message)
}
```

#### 3. Context-Aware Help

```kotlin
fun showContextualHelp(screenName: String, userAction: String) {
    val message = WidgetMessage.Structured(
        content = "I'm having trouble with this screen",
        properties = mapOf(
            "screen" to screenName,
            "last_action" to userAction,
            "timestamp" to System.currentTimeMillis().toString(),
            "app_version" to BuildConfig.VERSION_NAME
        )
    )
    
    widgetView.open(message)
}
```

## Widget Button Integration

For floating widget button functionality:

### Step 1: Add Widget Button

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var widgetButton: LimechatWidgetButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupWidgetButton()
    }
    
    private fun setupWidgetButton() {
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN",
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            )
        )
        
        widgetButton = LimechatWidgetButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, dpToPx(16), dpToPx(16))
            }
            
            // Initialize button
            init(config)
            
            // Set click listener
            setOnClickListener {
                openWidgetModal()
            }
            
            // Set initial unread count
            updateUnreadCount(3)
        }
        
        // Add to main container
        findViewById<FrameLayout>(R.id.mainContainer).addView(widgetButton)
    }
    
    private fun openWidgetModal() {
        val intent = Intent(this, LimechatWidgetActivity::class.java)
        startActivity(intent)
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
```

### Step 2: Custom Button with Messages

```kotlin
// Different click behaviors for different contexts
widgetButton.setOnClickListener {
    val messageType = determineMessageType()
    
    when (messageType) {
        "order_help" -> {
            openWidgetWithOrderHelp()
        }
        "general_inquiry" -> {
            openWidgetWithGeneralInquiry()
        }
        else -> {
            openWidgetModal() // Regular open
        }
    }
}

private fun openWidgetWithOrderHelp() {
    val message = WidgetMessage.Text("I need help with my order")
    
    val intent = Intent(this, LimechatWidgetActivity::class.java).apply {
        putExtra("message_type", "text")
        putExtra("message_content", message.content)
    }
    startActivity(intent)
}
```

## Customization Options

### Configuration Parameters

| Parameter | Type | Description | Required | Default |
|-----------|------|-------------|----------|---------|
| `websiteToken` | String | Your Limechat website token | Yes | - |
| `user` | User | User details object | No | null |
| `baseUrl` | String | Custom base URL | No | Production URL |
| `locale` | String | Widget language | No | "en" |
| `colorScheme` | ColorScheme | Theme preference | No | LIGHT |
| `customAttributes` | Map<String, String> | Custom metadata | No | empty |

### User Object

```kotlin
val user = WidgetConfig.User(
    name = "John Doe",           // Optional - User's display name
    email = "john@example.com",  // Optional - User's email
    phoneNumber = "+1234567890", // Optional - User's phone
    identifierHash = "hash123"   // Optional - Unique identifier
)
```

### Color Schemes

```kotlin
enum class ColorScheme {
    LIGHT,  // Light theme
    DARK,   // Dark theme  
    AUTO    // System preference
}
```

### Custom Attributes

```kotlin
val customAttributes = mapOf(
    "user_tier" to "premium",
    "source" to "mobile_app",
    "version" to BuildConfig.VERSION_NAME,
    "platform" to "android",
    "feature_flags" to "new_ui,advanced_search"
)
```

## API Reference

### LimechatWidgetView Methods

#### `init(config: WidgetConfig, callback: WidgetCallback?)`
Initialize the widget with configuration and optional callbacks.

#### `init(config: WidgetConfig, callback: WidgetCallback?, initialMessage: WidgetMessage?)`
Initialize the widget with an optional initial message.

#### `open(message: WidgetMessage)`
Open the widget with a custom message (runtime).

```kotlin
// Text message
widgetView.open(WidgetMessage.Text("Hello there!"))

// Structured message
widgetView.open(WidgetMessage.Structured(
    content = "I need assistance",
    properties = mapOf("type" to "urgent")
))
```

#### `sendMessage(event: String, data: Map<String, Any>)`
Send custom messages to the widget.

```kotlin
widgetView.sendMessage("custom-event", mapOf(
    "action" to "button_clicked",
    "value" to "subscribe"
))
```

#### `onBackPressed(): Boolean`
Handle back button press. Returns true if handled by WebView.

#### `destroy()`
Clean up resources when done.

#### `attachFilePicker(filePicker: WidgetFilePicker)`
Attach file picker for handling file uploads.

### LimechatWidgetButton Methods

#### `init(config: WidgetConfig, customButton: View?)`
Initialize the button with optional custom view.

#### `updateUnreadCount(count: Int)`
Update the unread messages badge.

#### `setOnClickListener(listener: OnClickListener?)`
Set click handler for the button.

### WidgetMessage Classes

#### `WidgetMessage.Text(content: String)`
Simple text message.

#### `WidgetMessage.Structured(content: String, properties: Map<String, Any>)`
Structured message with additional metadata.

#### `getMessageContent(): String`
Get the display content of any message type.

#### `toUrlParameter(): String`
Convert message to URL parameter format (internal use).

### WidgetCallback Interface

```kotlin
interface WidgetCallback {
    fun onLoaded()                           // Widget loaded successfully
    fun onClose()                            // User requested to close widget
    fun onError(error: WidgetError)         // Error occurred
    fun onMessage(message: Map<String, Any>) // Custom message received
}
```

### WidgetError

```kotlin
data class WidgetError(
    val code: ErrorCode,
    override val message: String,
    val originalError: Throwable? = null,
    val context: Map<String, Any>? = null
) : Exception(message, originalError)

enum class ErrorCode {
    CONFIG_ERROR,
    WEBVIEW_ERROR,
    NETWORK_ERROR,
    JAVASCRIPT_ERROR,
    FILE_PICKER_ERROR,
    INITIALIZATION_ERROR,
    UNKNOWN_ERROR
}
```

## Comparison with React Native SDK

### Similarities

Both Android Native and React Native SDKs share:

- Same website token configuration
- User identification parameters
- Locale support
- Custom attributes
- Event callbacks (onLoad, onClose, onError)
- **Custom messages functionality** ✨

### Key Differences

| Feature | React Native SDK | Android Native SDK |
|---------|------------------|-------------------|
| **Integration** | JSX Component | View/Activity |
| **Custom Messages** | Props-based | WidgetMessage objects |
| **Custom Button** | `customButton` prop | Launch via Intent |
| **Unread Count** | Built-in styling props | Manual implementation |
| **Configuration** | Props-based | WidgetConfig object |
| **Styling** | Style props | Theme/Layout XML |
| **Full Screen** | Modal/Navigation | Dedicated Activity |

### React Native Example

```jsx
<LimeChatWidget
  websiteToken="MEFFACy4xaovJayhLjSt836h"
  user={user}
  locale="en"
  customAttributes={customAttributes}
  initialMessage="Hello! I need help."
  onWidgetLoad={handleWidgetLoad}
  onWidgetClose={handleWidgetClose}
  onError={handleError}
  customButton={<CustomButton />}
  unreadCountStyle={{ top: -20 }}
/>
```

### Android Native Equivalent

```kotlin
// Configuration
val config = WidgetConfig(
    websiteToken = "MEFFACy4xaovJayhLjSt836h",
    locale = "en",
    user = user,
    customAttributes = customAttributes
)

// Initial message
val initialMessage = WidgetMessage.Text("Hello! I need help.")

// Initialize with callbacks
widgetView.init(config, object : WidgetCallback {
    override fun onLoaded() { /* handleWidgetLoad */ }
    override fun onClose() { /* handleWidgetClose */ }
    override fun onError(error: WidgetError) { /* handleError */ }
}, initialMessage)

// Custom button - Launch as Activity
button.setOnClickListener {
    startActivity(Intent(this, LimechatWidgetActivity::class.java))
}
```

## Migration Guide

### From v0.0.4 to v1.0.0

#### 1. Update Dependencies
```kotlin
// Old
implementation("ai.limechat:widget-sdk:0.0.4")

// New  
implementation("ai.limechat:widget-sdk:1.0.0")
```

#### 2. Import New Classes
```kotlin
// Add these imports
import ai.limechat.widget.models.WidgetMessage
```

#### 3. Update Widget Initialization (Optional)
```kotlin
// Old way (still works)
widgetView.init(config, callback)

// New way with custom messages
widgetView.init(config, callback, WidgetMessage.Text("Welcome!"))
```

#### 4. Use New Message Features
```kotlin
// New capability - runtime message opening
widgetView.open(WidgetMessage.Text("Need help with this feature"))
```

All existing code continues to work unchanged. New features are additive.

## Best Practices

### 1. Widget Initialization
- Always initialize `WidgetFilePicker` in `onCreate()`
- Use full-screen activities for better UX
- Handle errors gracefully with proper fallbacks

### 2. Custom Messages
- Use structured messages for analytics and context
- Keep message content concise and user-friendly
- Include relevant metadata in properties

### 3. Resource Management
- Always call `destroy()` in `onDestroy()`
- Handle back navigation properly
- Clean up references to prevent leaks

### 4. User Experience
```kotlin
// Good: Contextual messages
val message = WidgetMessage.Structured(
    content = "I need help with checkout",
    properties = mapOf(
        "page" to "checkout",
        "cart_value" to "99.99",
        "payment_method" to "credit_card"
    )
)

// Avoid: Generic messages without context
val message = WidgetMessage.Text("Help")
```

### 5. Error Handling
```kotlin
override fun onError(error: WidgetError) {
    when (error.code) {
        WidgetError.ErrorCode.NETWORK_ERROR -> {
            // Show retry option
            showRetryDialog()
        }
        WidgetError.ErrorCode.INITIALIZATION_ERROR -> {
            // Log for debugging, show fallback
            Log.e("Widget", "Init failed", error.originalError)
            showFallbackSupport()
        }
        else -> {
            // Generic error handling
            showErrorMessage(error.message)
        }
    }
}
```

## Troubleshooting

### Widget Not Loading
- ✅ Verify internet permission in manifest
- ✅ Check website token is correct and active
- ✅ Ensure WebView is enabled in device settings
- ✅ Verify network connectivity

### Custom Messages Not Appearing
- ✅ Check message content is not empty
- ✅ Verify widget is fully loaded before sending messages
- ✅ Use proper message format (Text vs Structured)
- ✅ Check logs for JavaScript errors

### File Upload Not Working  
- ✅ Initialize `WidgetFilePicker` in `onCreate()`
- ✅ Call `attachFilePicker()` before `init()`
- ✅ Check file permissions if needed
- ✅ Verify file size limits

### Full Screen Issues
- ✅ Use recommended theme configuration
- ✅ Set proper window flags for immersive mode
- ✅ Handle system UI visibility correctly

### Performance Issues
- ✅ Always call `destroy()` to clean up resources
- ✅ Avoid creating multiple widget instances
- ✅ Use appropriate lifecycle methods

### Common Error Solutions

```kotlin
// Error: Widget not ready
if (widgetView.isReady()) {  // Check if ready before operations
    widgetView.open(message)
}

// Error: Memory leaks
override fun onDestroy() {
    super.onDestroy()
    widgetView.destroy()  // Always clean up
    widgetButton?.destroy()  // Don't forget the button
}

// Error: Custom message not working
// Make sure to use proper message format
val message = WidgetMessage.Text("Hello")  // Not just string
widgetView.open(message)
```

## Support & Resources

### Documentation
- [GitHub Repository](https://github.com/your-org/limechat-android-sdk)
- [API Documentation](https://docs.limechat.ai/android-sdk)
- [Integration Examples](https://github.com/your-org/limechat-android-examples)

### Support Channels
- **GitHub Issues**: Bug reports and feature requests
- **Developer Portal**: Technical documentation
- **Support Email**: android-sdk@limechat.ai

### Version History
- **v1.0.0**: Custom messages, improved architecture, enhanced error handling
- **v0.0.4**: Basic widget functionality, file uploads
- **v0.0.3**: Initial release

---

*Last updated: September 2025 | SDK Version: 1.0.0*