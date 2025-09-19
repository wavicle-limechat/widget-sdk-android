# LimeChat Widget Android SDK

**Official Android SDK for integrating LimeChat chat widget into your Android applications.**

[![JitPack](https://jitpack.io/v/wavicle-limechat/widget-sdk-android.svg)](https://jitpack.io/#wavicle-limechat/widget-sdk-android)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## ✨ Features

- 🎯 **Easy Integration**: Add chat to your app in minutes.
- 🎨 **Customizable UI**: Match your brand's design and feel.
- 📱 **Widget Button**: A floating button with an unread message count badge.
- 🖥️ **Full-Screen Chat**: An immersive conversation experience.
- 📎 **File Uploads**: Share images, documents, and other media.
- 🔔 **Real-Time Messaging**: Instant notifications and message delivery.
- 🎭 **Theme Support**: Light, dark, and automatic theme switching.
- 🌍 **Multi-Language**: Support for localization and multiple languages.
- 🔒 **Secure**: Built with enterprise-grade security and privacy standards.
- 💾 **Chat Persistence**: Conversations persist across app restarts and widget instances.
- 🔙 **Back Button Support**: Configurable legacy back icon with proper event handling.

## 🛠️ Requirements

- **Minimum SDK**: 23 (Android 6.0)
- **Kotlin**: 1.8+
- **AndroidX**: Required

## 🚀 Quick Start

Get your chat widget up and running in three simple steps.

### 1. Add the Dependency

First, add the JitPack repository to your `settings.gradle.kts` file:

```kotlin
// settings.gradle.kts
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Next, add the SDK dependency to your app's `build.gradle.kts` file:

```kotlin
// app/build.gradle.kts  
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:v0.0.9")
}
```

### 2. Initialize the Widget

To add the chat widget to your app, configure it with your `websiteToken` and user details. Then, create a `LimechatWidgetButton` and add it to your layout.

```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 1. Configure the widget
        val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN") // Get this from your LimeChat dashboard
            .setUser(WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            ))
            .build()
        
        // 2. Create the floating chat button
        val widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)
        
        // 3. Add the button to your layout (e.g., bottom-right corner)
        val container = findViewById<FrameLayout>(R.id.main_container)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 16.dp, 16.dp)
        }
        container.addView(widgetButton, layoutParams)
    }
}
```

### 3. Add Permissions

Finally, ensure your app has the necessary permissions by adding the following to your `AndroidManifest.xml`:

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

**That's it! Your chat widget is now ready.** 🎉

## 📖 Documentation

For more detailed information, check out our comprehensive documentation and examples.

- **[🚀 Quick Reference](QUICK_REFERENCE.md)**: Common code snippets and quick examples.
- **[📚 Full Integration Guide](CLIENT_INTEGRATION_GUIDE.md)**: A complete guide to integrating and customizing the widget.
- **[🔧 Working Example](published-sdk-demo/)**: A sample application demonstrating a complete integration.

### Advanced Integration

For more control, you can embed the widget directly as a `LimechatWidgetView` and handle events with callbacks.

```kotlin
// Full-screen widget with callbacks
class ChatActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        // Enable file uploads
        val filePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(filePicker)
        
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() { /* Widget is ready */ }
            override fun onClose() { finish() } // Automatically called when user closes/minimizes widget
            override fun onError(error: WidgetError) { /* Handle any errors */ }
            override fun onMessage(message: Map<String, Any>) {
                // Handle real-time events from the widget
                when (message["event"]) {
                    "unread-count-changed" -> updateBadge(message["count"] as Int)
                    "conversation-started" -> trackAnalytics("chat_started")
                }
            }
        })
    }
}
```

## 🆕 New Features in v0.0.9

### Chat Persistence

The SDK now automatically persists conversations across app restarts and widget instances. Each widget instance maintains its own conversation state, ensuring users can continue their conversations seamlessly.

```kotlin
// Chat persistence is automatic - no additional configuration needed
val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN")
    .setInstanceId("unique-widget-instance") // Optional: for widget isolation
    .build()
```

### Legacy Back Icon Support

Control the display of the legacy back icon in the widget with the new `showLegacyBackIcon` property.

```kotlin
val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN")
    .setShowLegacyBackIcon(true) // Enable legacy back icon
    .build()
```

**URL Parameter**: When enabled, `show_legacy_back_icon=true` is automatically added to the widget URL.

### Widget Back Event Handling

The SDK now handles the `widget-back` event, which behaves the same as the `close-widget` event.

```kotlin
widgetView.init(config, object : WidgetCallback {
    override fun onClose() { 
        // Called for both 'close-widget' and 'widget-back' events
        finish() 
    }
})
```

## 🔧 Common Issues

### Dependency Resolution Error

If you encounter a dependency resolution error, ensure you have `google()` and `mavenCentral()` in your `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

### AndroidX Compatibility

The SDK requires AndroidX. To enable it, add the following to your `gradle.properties` file:

```properties
# gradle.properties
android.useAndroidX=true
android.enableJetifier=true
```

**For more solutions, see our [Troubleshooting Guide](CLIENT_INTEGRATION_GUIDE.md#-troubleshooting).**

## 📊 SDK Status

- **Current Version**: v0.0.9
- **Build Status**: [![JitPack Build](https://jitpack.io/v/wavicle-limechat/widget-sdk-android.svg)](https://jitpack.io/#wavicle-limechat/widget-sdk-android)
- **Stability**: Production Ready

## 🤝 Support

- **📖 Documentation**: [CLIENT_INTEGRATION_GUIDE.md](CLIENT_INTEGRATION_GUIDE.md)
- **📦 Publishing Guide**: [PUBLISHING.md](PUBLISHING.md)
- **🐛 Bug Reports**: [GitHub Issues](https://github.com/wavicle-limechat/widget-sdk-android/issues)
- **💬 General Support**: Contact the LimeChat support team.
- **📧 Email**: support@limechat.ai

## 📄 License

This project is licensed under the MIT License.
