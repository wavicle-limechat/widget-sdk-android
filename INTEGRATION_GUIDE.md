# Limechat Android SDK Integration Guide

## Table of Contents
- [Installation](#installation)
- [Basic Integration](#basic-integration)
- [Full-Screen Modal Implementation](#full-screen-modal-implementation)
- [Customization Options](#customization-options)
- [API Reference](#api-reference)
- [Comparison with React Native SDK](#comparison-with-react-native-sdk)

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
        
        // Configure and initialize
        val user = WidgetConfig.User(
            name = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+1234567890",
            identifierHash = "optional_hash" // Optional for secure user identification
        )
        
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // e.g., "MEFFACy4xaovJayhLjSt836h"
            locale = "en", // Supported: en, es, fr, de, it, pt
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
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d("Limechat", "Message received: $message")
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
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import ai.limechat.widget.*
import ai.limechat.widget.models.WidgetConfig

class LimechatWidgetActivity : FragmentActivity() {
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // Hide system UI
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContentView(R.layout.activity_limechat_widget)
        
        initWidget()
    }
    
    private fun initWidget() {
        widgetView = findViewById(R.id.widgetView)
        
        // Setup file picker
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        // Get user details (from intent or your user session)
        val userName = intent.getStringExtra("user_name") ?: ""
        val userEmail = intent.getStringExtra("user_email") ?: ""
        val userPhone = intent.getStringExtra("user_phone") ?: ""
        
        val user = WidgetConfig.User(
            name = userName,
            email = userEmail,
            phoneNumber = userPhone
        )
        
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = user,
            customAttributes = mapOf(
                "source" to "android_app"
            )
        )
        
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                // Widget loaded
            }
            
            override fun onClose() {
                finish() // Close the activity
            }
            
            override fun onError(error: WidgetError) {
                // Handle error
            }
            
            override fun onMessage(message: Map<String, Any>) {
                // Handle custom messages
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

### Step 4: Launch Widget

From your main activity:

```kotlin
// Launch widget with user details
val intent = Intent(this, LimechatWidgetActivity::class.java).apply {
    putExtra("user_name", "John Doe")
    putExtra("user_email", "john.doe@example.com")
    putExtra("user_phone", "+1234567890")
}
startActivity(intent)
```

## Customization Options

### Configuration Parameters

| Parameter | Type | Description | Required |
|-----------|------|-------------|----------|
| `websiteToken` | String | Your Limechat website token | Yes |
| `locale` | String | Language locale (en, es, fr, de, it, pt) | No (default: "en") |
| `colorScheme` | ColorScheme | LIGHT, DARK, or AUTO | No (default: LIGHT) |
| `user` | User | User details object | No |
| `customAttributes` | Map<String, Any> | Custom data to attach | No |
| `baseUrl` | String | Custom base URL | No (default: production) |

### User Object

```kotlin
val user = WidgetConfig.User(
    name = "John Doe",           // Optional
    email = "john@example.com",  // Optional
    phoneNumber = "+1234567890",  // Optional
    identifierHash = "hash123"    // Optional - for secure identification
)
```

### Color Schemes

```kotlin
// Light theme
config.colorScheme = WidgetConfig.ColorScheme.LIGHT

// Dark theme
config.colorScheme = WidgetConfig.ColorScheme.DARK

// Follow system theme
config.colorScheme = WidgetConfig.ColorScheme.AUTO
```

## API Reference

### LimechatWidgetView Methods

#### `init(config: WidgetConfig, callback: WidgetCallback?)`
Initialize the widget with configuration and optional callbacks.

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
class WidgetError(
    val code: ErrorCode,
    val message: String,
    val cause: Throwable? = null,
    val context: Map<String, Any>? = null
)

enum class ErrorCode {
    CONFIGURATION_ERROR,
    NETWORK_ERROR,
    WEBVIEW_ERROR,
    JAVASCRIPT_ERROR,
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

### Key Differences

| Feature | React Native SDK | Android Native SDK |
|---------|-----------------|-------------------|
| **Integration** | JSX Component | View/Activity |
| **Custom Button** | `customButton` prop with JSX | Launch via Intent |
| **Unread Count** | Built-in with styling props | Implement manually |
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

// Initialize with callbacks
widgetView.init(config, object : WidgetCallback {
    override fun onLoaded() { /* handleWidgetLoad */ }
    override fun onClose() { /* handleWidgetClose */ }
    override fun onError(error: WidgetError) { /* handleError */ }
})

// Custom button - Launch as Activity
button.setOnClickListener {
    startActivity(Intent(this, LimechatWidgetActivity::class.java))
}
```

### Feature Parity Notes

1. **Custom Button**: In Android, implement your own button that launches the widget activity
2. **Unread Count**: Not built-in for Android - implement using `sendMessage()` and custom UI
3. **Modal Presentation**: Use a dedicated full-screen Activity in Android
4. **Safe Area**: Handled automatically in the SDK via window insets

## Best Practices

1. **Always use Full-Screen Mode** for better user experience
2. **Initialize File Picker** in `onCreate()` to handle file uploads
3. **Handle Back Navigation** properly using `onBackPressed()`
4. **Clean Up Resources** by calling `destroy()` in `onDestroy()`
5. **Pass User Details** for personalized experience
6. **Use Custom Attributes** for tracking and analytics

## Troubleshooting

### Widget Not Loading
- Verify internet permission in manifest
- Check website token is correct
- Ensure WebView is enabled in settings

### File Upload Not Working
- Ensure `WidgetFilePicker` is initialized in `onCreate()`
- Call `attachFilePicker()` before `init()`

### Full Screen Issues
- Use the provided theme configuration
- Set proper window flags
- Handle system UI visibility

## Support

For issues or questions:
- GitHub: [limechat-widget-sdks](https://github.com/limechat/widget-sdks)
- Email: support@limechat.ai