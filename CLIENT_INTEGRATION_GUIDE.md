# LimeChat Android SDK Integration Guide

**A comprehensive guide for integrating the LimeChat widget into your Android application.**

| Version | Repository | Platform |  |
| --- | --- | --- | --- |
| 0.0.8 | `com.github.wavicle-limechat:widget-sdk-android` | Android API 23+ |  |

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
        val config = WidgetConfig(
            websiteToken = "YOUR_WEBSITE_TOKEN", // Replace with your token from the LimeChat dashboard
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com"
            )
        )

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

Here are some of the available options:

```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN",    // Required: Your LimeChat website token
    locale = "en",                          // Optional: Language code (e.g., "en", "es", "fr")
    colorScheme = WidgetConfig.ColorScheme.AUTO, // Optional: LIGHT, DARK, or AUTO
    user = WidgetConfig.User(               // Optional: User information
        name = "John Doe",
        email = "john@company.com",
        phoneNumber = "+1234567890",
        identifierHash = "USER_HASH_123"    // Optional: For secure mode
    ),
    customAttributes = mapOf(               // Optional: Custom metadata
        "user_type" to "premium",
        "plan" to "enterprise"
    ),
    baseUrl = "https://app.limechat.ai"     // Optional: For custom domains
)
```

### Custom Attributes

You can send any custom data to the widget, which can be useful for tracking or segmentation.

```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    customAttributes = mapOf(
        "user_id" to "12345",
        "subscription_tier" to "premium",
        "app_version" to BuildConfig.VERSION_NAME
    )
)
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
        val config = WidgetConfig(
            websiteToken = intent.getStringExtra("WEBSITE_TOKEN") ?: "YOUR_TOKEN"
        )

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

val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    user = user
)
```

### Updating User Details

If the user logs in or out, you can re-initialize the widget with the new information.

```kotlin
fun onUserLoggedIn(user: AppUser) {
    val chatUser = WidgetConfig.User(name = user.name, email = user.email)
    val newConfig = WidgetConfig(websiteToken = "YOUR_TOKEN", user = chatUser)
    widgetButton.init(newConfig) // Re-initializes with the new user
}
```

### Secure Mode

For added security, you can use a server-generated hash to verify the user's identity.

```kotlin
// This hash should be generated on your backend
val userHash = "SERVER_GENERATED_HASH"

val user = WidgetConfig.User(
    email = "jane@example.com",
    identifierHash = userHash
)
```

---

## 💾 Chat Persistence

To keep conversations sticky across sessions, pass the last saved `cw_conversation` token when you initialise the widget and capture updates when the widget issues a new token.

```kotlin
val storedToken = loadToken()

val initOptions = LimechatWidgetView.InitOptions(
    conversationOptions = LimechatWidgetView.ConversationOptions(
        token = storedToken,
        onTokenChange = LimechatWidgetView.ConversationTokenListener { token ->
            saveToken(token)
        }
    ),
    conversationInstanceId = "customer_support_widget"
)

widgetView.init(config, widgetCallback, initOptions)
```

- If you skip both fields, the SDK caches the generated token internally so the current widget instance keeps its state.
- Use `conversationInstanceId` to ensure separate entry points (for example, an embedded widget and a floating button) don’t share the same conversation history.
- Access the latest value at any time with `widgetView.getConversationToken()` or replace it later with `widgetView.setConversationOptions(...)`.


## 📡 Handling Events

You can listen for events from the widget using the `WidgetCallback`.

### Implementing WidgetCallback

```kotlin
widgetView.init(config, object : WidgetCallback {
    override fun onLoaded() {
        // Widget has finished loading
    }

    override fun onClose() {
        // User has requested to close the widget (e.g., clicked minimize button)
        // The SDK automatically handles minimize/close detection
        finish() // Example: close the activity
    }

    override fun onError(error: WidgetError) {
        // An error occurred
        Log.e("LimeChat", "Widget error: ${error.message}")
    }

    override fun onMessage(message: Map<String, Any>) {
        // A message was received from the widget
        handleWidgetMessage(message)
    }
})
```

### Available Events

Here are some of the events you can receive in `onMessage`:

- `unread-count-changed`: The number of unread messages has changed.
- `conversation-started`: A new conversation has been started.
- `agent-joined`: An agent has joined the chat.
- `message-received`: A new message has been received.

```kotlin
private fun handleWidgetMessage(message: Map<String, Any>) {
    when (message["event"] as? String) {
        "unread-count-changed" -> {
            val count = message["count"] as? Int ?: 0
            // Update a notification badge
        }
        "conversation-started" -> {
            // Track an analytics event
        }
    }
}
```

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

- **Dependency Resolution Error**: Ensure JitPack is in your `settings.gradle.kts` and the dependency name is correct.
- **AndroidX Compatibility**: Make sure your project has `android.useAndroidX=true` and `android.enableJetifier=true` in `gradle.properties`.
- **Widget Not Loading**: Double-check your `websiteToken` and internet permissions. Use the `onError` callback to log any errors.
- **Widget Not Closing**: Ensure you've implemented the `onClose()` callback in your `WidgetCallback`. The SDK automatically handles minimize/close button presses.
- **ProGuard Issues**: If the widget crashes in release builds, add the ProGuard rules from the `widget-sdk/proguard-rules.pro` file to your app's ProGuard configuration.

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
