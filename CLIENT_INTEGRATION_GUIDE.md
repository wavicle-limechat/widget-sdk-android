# LimeChat Android SDK Integration Guide

**A comprehensive guide for integrating the LimeChat widget into your Android application.**

| Version | Repository | Platform |  |
| --- | --- | --- | --- |
| v0.0.9 | `com.github.wavicle-limechat:widget-sdk-android` | Android API 23+ |  |

---

## 📋 Table of Contents

1. [Getting Started](#-getting-started)
    - [Requirements](#requirements)
    - [Step 1: Add Repository](#step-1-add-repository)
    - [Step 2: Add Dependency](#step-2-add-dependency)
    - [Step 3: Add Permissions](#step-3-add-permissions)
2. [Basic Integration](#-basic-integration)
    - [Displaying the Widget Button](#displaying-the-widget-button)
3. [Advanced Configuration](#️-advanced-configuration)
    - [WidgetConfig Options](#widgetconfig-options)
    - [Custom Attributes](#custom-attributes)
4. [Customizing the Widget Button](#-customizing-the-widget-button)
    - [Using a Custom Button](#using-a-custom-button)
    - [Positioning the Button](#positioning-the-button)
    - [Displaying Unread Messages](#displaying-unread-messages)
5. [Full-Screen Widget](#️-full-screen-widget)
    - [Creating a Widget Activity](#creating-a-widget-activity)
    - [Launching the Activity](#launching-the-activity)
6. [User Management](#-user-management)
    - [Passing User Information](#passing-user-information)
    - [Updating User Details](#updating-user-details)
    - [Secure Mode](#secure-mode)
7. [Handling Events](#-handling-events)
    - [Implementing WidgetCallback](#implementing-widgetcallback)
    - [Available Events](#available-events)
8. [File Uploads](#-file-uploads)
    - [Enabling File Uploads](#enabling-file-uploads)
    - [Permissions for File Uploads](#permissions-for-file-uploads)
9. [Custom Message Widget Buttons](#-custom-message-widget-buttons)
10. [Troubleshooting](#-troubleshooting)
11. [API Reference](#-api-reference)

---

## 🚀 Getting Started

This section guides you through the initial setup of the LimeChat Widget SDK.

### Requirements

- **Minimum SDK**: API 23 (Android 6.0)
- **Kotlin**: 1.8 or higher
- **AndroidX**: Your project must be migrated to AndroidX.

### Step 1: Add Repository

In your project's `settings.gradle.kts` file, add JitPack to the list of repositories:

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

### Step 2: Add Dependency

Add the LimeChat Widget SDK as a dependency in your app's `build.gradle.kts` file:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.8")

    // The SDK requires the following AndroidX libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.11.0")
}
```

### Step 3: Add Permissions

The SDK requires internet access to communicate with the LimeChat servers. Add the following permission to your `AndroidManifest.xml`:

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🎯 Basic Integration

The quickest way to get started is by adding the floating `LimechatWidgetButton` to your activity.

### Displaying the Widget Button

1. **Configure the Widget**: Create a `WidgetConfig` object with your `websiteToken` and user details.
2. **Create the Button**: Instantiate `LimechatWidgetButton`.
3. **Add to Layout**: Add the button to your activity's layout.

```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configure the widget with your token and user info
        val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN") // Replace with your token from the LimeChat dashboard
            .setUser(WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            ))
            .build()

        // 2. Create the widget button
        val widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)

        // 3. Add the button to your layout (e.g., in the bottom-right corner)
        val container = findViewById<FrameLayout>(R.id.main_container)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 16.dp, 16.dp) // Extension function for dp is recommended
        }
        container.addView(widgetButton, layoutParams)
    }
}
```

> Tip: Create a simple extension function to convert dp to pixels: `fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()`

---

## ⚙️ Advanced Configuration

Customize the widget's behavior and appearance through the `WidgetConfig` object.

### WidgetConfig Options

Here are some of the available options using the builder pattern:

```kotlin
val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN")    // Required: Your LimeChat website token
    .setLocale("en")                                       // Optional: Language code (e.g., "en", "es", "fr")
    .setColorScheme(WidgetConfig.ColorScheme.AUTO)         // Optional: LIGHT, DARK, or AUTO (no visible effect, the widget has no dark mode today)
    .setUser(WidgetConfig.User(                            // Optional: User information
        name = "John Doe",
        email = "john@company.com",
        phoneNumber = "+1234567890",
        identifier = "USER_ID_123",                        // Optional: For secure mode, needs identifierHash too
        identifierHash = "USER_HASH_123"                   // Optional: For secure mode, needs identifier too
    ))
    .setCustomAttributes(mapOf(                            // Optional: Custom metadata
        "user_type" to "premium",
        "plan" to "enterprise"
    ))
    .setBaseUrl("https://app.limechat.ai")                 // Optional: For custom domains
    .setInstanceId("unique-widget-instance")               // Optional: For widget isolation and persistence
    .setShowLegacyBackIcon(true)                           // Optional: Enable legacy back icon (default: false)
    .setHideFilePicker(true)                               // Optional: Hide the attachment button (default: false)
    .build()
```

#### New Features in v0.0.9:

- **`setInstanceId(String?)`**: Provides unique identification for widget instances, enabling proper conversation persistence and isolation between different widget instances in your app.
- **`setShowLegacyBackIcon(Boolean)`**: Controls the native close behaviour. When enabled and the view sits in an Activity, closing the widget finishes that Activity and `onClose()` is not called.
- **`setHideFilePicker(Boolean)`**: Hides the attachment button in the widget (default: false).

### Custom Attributes

You can send any custom data to the widget, which can be useful for tracking or segmentation.

```kotlin
val config = WidgetConfig.builder("YOUR_TOKEN")
    .setCustomAttributes(mapOf(
        "user_id" to "12345",
        "subscription_tier" to "premium",
        "app_version" to BuildConfig.VERSION_NAME
    ))
    .build()
```

---

## 🎈 Customizing the Widget Button

While the default button works out of the box, you can customize its appearance and behavior.

### Using a Custom Button

You can use your own `View` as the chat button. The SDK will handle opening the widget when the view is clicked.

```kotlin
// In your layout XML or created programmatically
val myCustomButton: Button = findViewById(R.id.my_chat_button)

// Initialize the widget with your custom button
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config, myCustomButton)
```

### Positioning the Button

If you are not using a custom button, you can position the default floating button anywhere on the screen. The bottom-right is a common choice.

```kotlin
widgetButton.layoutParams = FrameLayout.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
    gravity = Gravity.BOTTOM or Gravity.START // Example: bottom-left
    setMargins(16.dp, 0, 0, 16.dp)
}
```

### Displaying Unread Messages

You can show a badge on the widget button to indicate unread messages.

```kotlin
// Show a badge with 5 unread messages
widgetButton.updateUnreadCount(5)

// Hide the badge
widgetButton.updateUnreadCount(0)
```

---

## 🖥️ Full-Screen Widget

For a more immersive experience, you can open the widget in a dedicated, full-screen activity.

### Creating a Widget Activity

1. **Create an Activity**: Create a new `FragmentActivity` to host the `LimechatWidgetView`.
2. **Add the View**: Add the `LimechatWidgetView` to the activity's layout.
3. **Initialize**: Initialize the view with your `WidgetConfig`.
4. **Handle Back Press**: Override `onBackPressed` to allow the widget to handle navigation.

```kotlin
class ChatActivity : FragmentActivity() {

    private lateinit var widgetView: LimechatWidgetView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Create and set the widget view as the content
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)

        // 2. Get config from intent or create a new one
        val config = WidgetConfig.builder(
            intent.getStringExtra("WEBSITE_TOKEN") ?: "YOUR_TOKEN"
        ).build()

        // 3. Initialize the view
        widgetView.init(config)
    }

    override fun onBackPressed() {
        // 4. Allow the widget to handle back navigation (e.g., closing image previews)
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        widgetView.destroy() // Clean up resources
    }
}
```

### Launching the Activity

You can start this activity from anywhere in your app.

```kotlin
val intent = Intent(this, ChatActivity::class.java).apply {
    putExtra("WEBSITE_TOKEN", "YOUR_TOKEN")
}
startActivity(intent)
```

---

## 💾 Chat Persistence

The SDK automatically persists conversations across app restarts and widget instances. This ensures users can continue their conversations seamlessly without losing context.

### How It Works

- **Automatic Persistence**: Conversations are automatically saved and restored
- **Instance Isolation**: Each widget instance maintains its own conversation state
- **Cross-App Restart**: Conversations persist even when the app is completely closed and reopened
- **Per-Instance Storage**: Multiple widget instances in your app can have separate conversations

### Configuration

```kotlin
val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN")
    .setInstanceId("unique-widget-instance") // Optional: for widget isolation
    .build()
```

### Instance ID Usage

The `instanceId` parameter helps isolate conversations between different widget instances:

```kotlin
// Different widget instances with separate conversations
val mainWidgetConfig = WidgetConfig.builder("YOUR_TOKEN")
    .setInstanceId("main-widget")
    .build()

val supportWidgetConfig = WidgetConfig.builder("YOUR_TOKEN")
    .setInstanceId("support-widget")
    .build()

// Both widgets will have separate conversation histories
```

### External Token Management

For advanced use cases, you can manage conversation tokens externally:

```kotlin
var conversationToken: String? = null

val config = WidgetConfig.builder("YOUR_TOKEN")
    .setExternalTokenManagement(
        token = conversationToken,
        onTokenChange = { newToken ->
            conversationToken = newToken
            // Save to your preferred storage (database, preferences, etc.)
        }
    )
    .build()
```

---

## 👤 User Management

You can personalize the user experience by providing user information.

### Passing User Information

Include user details in the `WidgetConfig` object.

```kotlin
val user = WidgetConfig.User(
    name = "Jane Doe",
    email = "jane@example.com",
    phoneNumber = "+1987654321"
)

val config = WidgetConfig.builder("YOUR_TOKEN")
    .setUser(user)
    .build()
```

### Updating User Details

If the user logs in or out, you can re-initialize the widget with the new information.

```kotlin
fun onUserLoggedIn(user: AppUser) {
    val chatUser = WidgetConfig.User(name = user.name, email = user.email)
    val newConfig = WidgetConfig.builder("YOUR_TOKEN").setUser(chatUser).build()
    widgetButton.init(newConfig) // Re-initializes with the new user
}
```

### Secure Mode

For added security, you can verify the user's identity. Secure mode needs both `identifier` and `identifierHash`. The hash is `HMAC-SHA256(hmac_token, identifier)`, generated on your backend. With only one of the two, nothing is sent.

```kotlin
// This hash should be generated on your backend
val userHash = "SERVER_GENERATED_HASH"

val user = WidgetConfig.User(
    email = "jane@example.com",
    identifier = "USER_ID_123",
    identifierHash = userHash
)
```

---

## 📡 Handling Events

You can listen for events from the widget using the `WidgetCallback`.

### Implementing WidgetCallback

```kotlin
widgetView.init(config, object : WidgetCallback {
    override fun onLoaded() {
        // Widget has finished loading
    }

    override fun onClose() {
        // User has requested to close the widget (e.g., clicked minimize button or back button)
        // Called when the widget close button is pressed
        // The SDK automatically handles minimize/close detection
        finish() // Example: close the activity
    }

    override fun onError(error: WidgetError) {
        // An error occurred
        Log.e("LimeChat", "Widget error: ${error.message}")
    }

    override fun onMessage(message: Map<String, Any?>) {
        // No widget events reach this callback yet
    }
})
```

### Widget Events

The widget sends only `set-cw-conversation` and `close-widget` today. The SDK handles both itself, so `onMessage` has no events yet.

### Widget Close Events

The `onClose()` callback is triggered by the `close-widget` event, when the user clicks the minimize/close button.

---

## 📎 File Uploads

The SDK supports file uploads within the chat.

### Enabling File Uploads

To enable file uploads, you need to attach a `WidgetFilePicker` to your `LimechatWidgetView`.

```kotlin
class ChatActivity : FragmentActivity() {

    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)

        // IMPORTANT: The file picker must be created in onCreate
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)

        // ... initialize the widget
    }
}
```

### Permissions for File Uploads

Depending on the Android version and the files being accessed, you may need to declare additional permissions in your `AndroidManifest.xml`.

```xml
<!-- For accessing the camera -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- For reading from storage (on older Android versions) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## 💬 Custom Message Widget Buttons

The SDK supports opening the widget with a predefined message. This is useful for creating contextual help buttons or quick actions in your app.

### Launching with a Custom Message

To open the widget with a custom message, pass the message in the `Intent` that launches your `ChatActivity`.

```kotlin
private fun openWidgetWithMessage(message: String) {
    val intent = Intent(this, ChatActivity::class.java).apply {
        putExtra("WEBSITE_TOKEN", "YOUR_TOKEN")
        putExtra("CUSTOM_MESSAGE", message)
    }
    startActivity(intent)
}
```

Then, in your `ChatActivity`, retrieve the message from the `Intent` and pass it to the `init` method.

```kotlin
// In your ChatActivity.onCreate()
val customMessage = intent.getStringExtra("CUSTOM_MESSAGE")

widgetView.init(config, callback, customMessage)
```

### Example: Contextual Help Buttons

```kotlin
// In your MainActivity
val helpButton = findViewById<Button>(R.id.help_button)
helpButton.setOnClickListener {
    openWidgetWithMessage("I need help with my order.")
}
```

---

## 🔧 Troubleshooting

If you encounter issues, check the following common problems and solutions.

- **Dependency Resolution Error**: Ensure `google()`, `mavenCentral()` and JitPack are in your repositories and the dependency name is correct.

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

- **AndroidX Compatibility**: Make sure your project has `android.useAndroidX=true` and `android.enableJetifier=true` in `gradle.properties`.

  ```properties
  # gradle.properties
  android.useAndroidX=true
  android.enableJetifier=true
  ```

- **Widget Not Loading**: Double-check your `websiteToken` and internet permissions. Use the `onError` callback to log any errors.
- **Widget Not Closing**: Ensure you've implemented the `onClose()` callback in your `WidgetCallback`. The SDK automatically handles minimize/close button presses.
- **File Picker Not Working**: Create the `WidgetFilePicker` in `onCreate`, before the activity starts, and attach it to the view.

  ```kotlin
  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      val widgetFilePicker = WidgetFilePicker(this) // Create here, not later
      widgetView.attachFilePicker(widgetFilePicker)
  }
  ```

- **ProGuard Issues**: If the widget crashes in release builds, the SDK ships `consumer-rules.pro`, which is applied automatically, so no manual rules should be needed. Check your own ProGuard configuration.

### Checklists

Before integration:

- [ ] JitPack repository added
- [ ] Internet permission in `AndroidManifest.xml`
- [ ] AndroidX enabled in `gradle.properties`
- [ ] Your LimeChat website token ready

Testing:

- [ ] Widget button appears and is clickable
- [ ] Widget opens and loads correctly
- [ ] File upload works (if needed)
- [ ] Proper cleanup in `onDestroy()`
- [ ] Works in a release build with ProGuard

Go live:

- [ ] Replace the test token with the production token
- [ ] Test on multiple devices and screen sizes
- [ ] Error handling implemented

---

## 📚 API Reference

For detailed information on the SDK's classes and methods, please refer to the source code and the `LimechatWidgetButton`, `LimechatWidgetView`, and `WidgetConfig` classes.

---

## 📞 Support

### Getting Help

- **Documentation**: This integration guide
- **Issues**: Report bugs or feature requests on GitHub
- **Support**: Contact LimeChat support team

### Version Updates

Stay updated with the latest SDK versions:

- **Current Version**: 0.0.8
- **Changelog**: Check GitHub releases
- **Migration Guides**: Available for major version updates

---

## 📄 License

LimeChat Widget Android SDK is distributed under the MIT License. See LICENSE file for details.

---

**Ready to integrate? Start with the [Basic Integration](#-basic-integration) section and have your chat widget running in minutes!**

For more examples, check out our sample applications in this repository.