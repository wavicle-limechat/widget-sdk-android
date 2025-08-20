# LimeChat Widget Android SDK

**Official Android SDK for integrating LimeChat chat widget into your Android applications.**

[![JitPack](https://jitpack.io/v/wavicle-limechat/widget-sdk-android.svg)](https://jitpack.io/#wavicle-limechat/widget-sdk-android)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## ✨ Features

- 🎯 **Easy Integration** - Add chat to your app in 5 minutes
- 🎨 **Customizable Appearance** - Match your brand design
- 📱 **Widget Button** - Floating button with unread count badge
- 🖥️ **Full-Screen Chat** - Immersive conversation experience  
- 📎 **File Upload Support** - Share images, documents, and media
- 🔔 **Real-Time Messaging** - Instant notifications and responses
- 🎭 **Theme Support** - Light, dark, and auto themes
- 🌍 **Multi-Language** - Localization support
- 🔒 **Secure** - Enterprise-grade security and privacy

## 🚀 Quick Start

### 1. Add Dependency

```kotlin
// settings.gradle.kts
repositories {
    maven { url = uri("https://jitpack.io") }
}

// app/build.gradle.kts  
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.4")
}
```

### 2. Initialize Widget

```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Configure the widget
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // Get from LimeChat dashboard
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            )
        )
        
        // Create floating chat button
        val widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)
        
        // Add to your layout (bottom-right corner)
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

### 3. Add Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

**That's it! Your chat widget is ready.** 🎉

## 📖 Documentation

### Quick References
- **[🚀 Quick Reference](QUICK_REFERENCE.md)** - Common code snippets and examples
- **[📚 Full Integration Guide](CLIENT_INTEGRATION_GUIDE.md)** - Comprehensive documentation
- **[🔧 Working Example](published-sdk-demo/)** - Complete sample application

### Advanced Integration

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
            override fun onLoaded() { /* Widget ready */ }
            override fun onClose() { finish() }
            override fun onError(error: WidgetError) { /* Handle errors */ }
            override fun onMessage(message: Map<String, Any>) {
                // Handle real-time events
                when (message["event"]) {
                    "unread-count-changed" -> updateBadge(message["count"] as Int)
                    "conversation-started" -> trackAnalytics("chat_started")
                }
            }
        })
    }
}
```

## 🛠️ Requirements

- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)  
- **Kotlin**: 1.8+
- **AndroidX**: Required

## 🔧 Common Issues

### Dependency Resolution Error
```kotlin
// Add to settings.gradle.kts
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

### AndroidX Compatibility
```properties
# Add to gradle.properties
android.useAndroidX=true
android.enableJetifier=true
```

**For more solutions, see [Troubleshooting Guide](CLIENT_INTEGRATION_GUIDE.md#-troubleshooting)**

## 📊 SDK Status

- **Current Version**: 0.0.4
- **Build Status**: [![JitPack Build](https://jitpack.io/v/wavicle-limechat/widget-sdk-android.svg)](https://jitpack.io/#wavicle-limechat/widget-sdk-android)
- **Stability**: Production Ready
- **Last Updated**: January 2025

## 🤝 Support

- **📖 Documentation**: [CLIENT_INTEGRATION_GUIDE.md](CLIENT_INTEGRATION_GUIDE.md)
- **🐛 Issues**: [GitHub Issues](https://github.com/wavicle-limechat/widget-sdk-android/issues)
- **💬 Support**: Contact LimeChat support team
- **📧 Email**: support@limechat.ai

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

---

**Ready to add chat to your app?** Start with the [Quick Reference](QUICK_REFERENCE.md) for instant integration! 🚀