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
import android.content.Intent
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

        // The button only reports taps. Open the chat yourself, here a ChatActivity (see the integration guide)
        widgetButton.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        
        // 3. Add the button to your layout (e.g., bottom-right corner)
        val container = findViewById<FrameLayout>(R.id.main_container)
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, dp(16), dp(16))
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

**That's it! The button shows in your app, and a tap starts your `ChatActivity`.** 🎉

## 📖 Documentation

For more detailed information, check out our comprehensive documentation and examples.

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
            override fun onMessage(message: Map<String, Any?>) {
                // No widget events reach this callback yet. The unread badge is host driven: call widgetButton.updateUnreadCount(count)
            }
        })
    }
}
```

For persistence, back icon, message attributes, `open` and the full API reference, see the [integration guide](CLIENT_INTEGRATION_GUIDE.md). Common build issues are in its [troubleshooting section](CLIENT_INTEGRATION_GUIDE.md#-troubleshooting).

## 📊 SDK Status

- **Current Version**: v0.0.9
- **Build Status**: [![JitPack Build](https://jitpack.io/v/wavicle-limechat/widget-sdk-android.svg)](https://jitpack.io/#wavicle-limechat/widget-sdk-android)
- **Stability**: Production Ready

## Publishing

Run `./validate-build.sh` first. It cleans, builds the SDK in release and debug, runs the tests and lint.

Then run `./publish-to-jitpack.sh`. It asks for the new version, updates `widget-sdk/build.gradle.kts`, commits, tags `v<version>` and pushes the branch and the tag. JitPack builds the tag. Check it at https://jitpack.io/#wavicle-limechat/widget-sdk-android.

Once the build is green, run `./test-jitpack-dependency.sh <version>`. It resolves `com.github.wavicle-limechat:widget-sdk-android:<version>` in a temporary project. `./check-jitpack-setup.sh` checks that the publishing files exist.

## 🤝 Support

- **📖 Documentation**: [CLIENT_INTEGRATION_GUIDE.md](CLIENT_INTEGRATION_GUIDE.md)
- **📦 Publishing**: see the Publishing section above
- **🐛 Bug Reports**: [GitHub Issues](https://github.com/wavicle-limechat/widget-sdk-android/issues)
- **💬 General Support**: Contact the LimeChat support team.
- **📧 Email**: support@limechat.ai

## 📄 License

This project is licensed under the MIT License.
