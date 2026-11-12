# LimeChat Android SDK Integration Guide

**A comprehensive guide for integrating the LimeChat widget into your Android application.**

| Version | Repository | Platform |  |
| --- | --- | --- | --- |
| v0.2.0-beta.1 | `com.github.wavicle-limechat:widget-sdk-android` | Android API 23+ |  |

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
6. [Chat Persistence](#-chat-persistence)
    - [How It Works](#how-it-works)
    - [Configuration](#configuration)
    - [Instance ID Usage](#instance-id-usage)
    - [External Token Management](#external-token-management)
    - [Logout](#logout)
7. [User Management](#-user-management)
    - [Passing User Information](#passing-user-information)
    - [Updating User Details](#updating-user-details)
    - [Secure Mode](#secure-mode)
8. [Handling Events](#-handling-events)
    - [Implementing WidgetCallback](#implementing-widgetcallback)
    - [Widget Events](#widget-events)
    - [Web Process Death](#web-process-death)
9. [File Uploads](#-file-uploads)
    - [Enabling File Uploads](#enabling-file-uploads)
    - [Permissions for File Uploads](#permissions-for-file-uploads)
10. [Custom Message Widget Buttons](#-custom-message-widget-buttons)
11. [Message Attributes](#-message-attributes)
12. [Troubleshooting](#-troubleshooting)
13. [API Reference](#-api-reference)

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
    implementation("com.github.wavicle-limechat:widget-sdk-android:v0.2.0-beta.1")

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
import android.content.Intent
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

        // The button only reports taps. Open the chat yourself, here a ChatActivity (see Full-Screen Widget below)
        widgetButton.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // 3. Add the button to your layout (e.g., in the bottom-right corner)
        val container = findViewById<FrameLayout>(R.id.main_container)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, dp(16), dp(16)) // dp() is the helper below
        }
        container.addView(widgetButton, layoutParams)
    }
}
```

> Tip: Create a simple helper to convert dp to pixels: `fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()`

---

## ⚙️ Advanced Configuration

Customize the widget's behavior and appearance through the `WidgetConfig` object.

### WidgetConfig Options

Here are some of the available options using the builder pattern:

```kotlin
val config = WidgetConfig.builder("YOUR_WEBSITE_TOKEN")    // Required: Your LimeChat website token
    .setLocale("en")                                       // Optional: Language code (e.g., "en", "es", "fr")
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
- **`setShowLegacyBackIcon(Boolean)`**: Controls the native close behaviour. Closing the widget always calls `onClose()` when a callback is set. Only when no callback is set and this is enabled, and the view sits in an Activity, does closing finish that Activity.

#### Added after v0.0.9:

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

You can use your own `View` as the chat button. The SDK does not open the widget when it is tapped. Set a click listener on the `LimechatWidgetButton` and open the chat yourself, by starting your `ChatActivity` or showing your `LimechatWidgetView`. Call `setWidgetView` only if you want `widgetButton.open` and `widgetButton.setMessageAttributes` to reach a view.

```kotlin
// In your layout XML or created programmatically
val myCustomButton: Button = findViewById(R.id.my_chat_button)

// Initialize the widget with your custom button
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config, myCustomButton)
widgetButton.setOnClickListener {
    startActivity(Intent(this, ChatActivity::class.java))
}
```

### Positioning the Button

If you are not using a custom button, you can position the default floating button anywhere on the screen. The bottom-right is a common choice.

```kotlin
widgetButton.layoutParams = FrameLayout.LayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
).apply {
    gravity = Gravity.BOTTOM or Gravity.START // Example: bottom-left
    setMargins(dp(16), 0, 0, dp(16))
}
```

### Displaying Unread Messages

You can show a badge on the widget button to indicate unread messages. The count is set by the host app. The widget does not update it.

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
// Either value can be omitted. With no token, the widget starts from a stored token for this instance if there is one.
// With no callback, the SDK stores the live token itself. With a callback, the SDK stores nothing.
var conversationToken: String = storedToken // from your own storage

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

### Logout

```kotlin
fun onLogout() {
    widgetView.reset()
}
```

`reset()` clears this widget's conversation, like the web SDK's `reset()`: it drops the stored or live conversation token and the user (and a token or user you passed in, until you pass new ones), drops held message attributes and a held open message, and reloads the page without a conversation if it is showing. It does not close the widget. Call it on logout.

- `reset()` does nothing before `init`.
- With no view on screen, call `LimechatWidgetView.reset(context)` or `LimechatWidgetView.reset(context, instanceId)`. It clears the stored token for that instance (`default` for a null or blank id) and does not reload a view that is showing. The static `reset(context, instanceId)` only clears stored state. if a view is showing, call its `reset()` instead, or the view keeps its live token and stores it again.

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

`WidgetConfig.User` also takes `customAttributes`. User custom attributes go with every message the customer types, not onto the contact.

```kotlin
val user = WidgetConfig.User(
    email = "jane@example.com",
    customAttributes = mapOf("plan" to "premium")
)
```

User details reach the widget without verification. On every customer message the server updates the contact, and can merge into an existing contact with the same email or phone. If you use identity validation, pass `identifier` and `identifierHash` (see Secure Mode) so the contact is verified.

### Updating User Details

If the user logs out, call `reset()` (see Logout). If the user logs in, or their details change, re-initialize the widget with the new information.

The SDK also notices a user switch. The identity is `identifier`, `email` and `phoneNumber`. At `init`, if the identity differs from the last one seen for the same instance (user A to user B, or user A to no user), the stored conversation token is dropped and the new user starts a fresh conversation. No user to user A, and the same identity with a new name, keep the conversation. A token you pass with `setExternalTokenManagement` still wins. The last identity is kept in memory only, so a switch across an app restart is not caught. Call `reset()` on logout.

```kotlin
fun onUserLoggedIn(user: AppUser) {
    val chatUser = WidgetConfig.User(name = user.name, email = user.email)
    val newConfig = WidgetConfig.builder("YOUR_TOKEN").setUser(chatUser).build()
    widgetView.init(newConfig, callback) // Re-initializes with the new user
}
```

`widgetView.init` clears held message attributes and a held `open` message, then reloads the widget. `LimechatWidgetButton.init` does not do this. The button keeps the config for its icon only, and the view keeps the old user and token.

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
        // The widget can receive messages. Called once per page load.
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

`onLoaded` fires when the widget is ready to receive messages, once per page load, after `onConversationTokenChange`. A page that fails to load calls `onError` and never calls `onLoaded`. This includes a main page HTTP error (status 400 or above), which reaches `onError` as a `WEBVIEW_ERROR` with `statusCode` in `context`. A page that returns 200 but never starts the widget gives neither.

`onError` also reports failures of subresources, such as a script or image on the page (`WidgetWebViewClient`). `onLoaded` can still follow, so an `onError` alone does not mean the page failed.

### Widget Events

The widget sends only `set-cw-conversation` and `close-widget` today. The SDK handles both itself, so `onMessage` has no events yet.

When the widget becomes ready, the SDK sends in this order: the user fields, the verified user, held message attributes, then a held `open` message. Then `onConversationTokenChange` and `onLoaded` fire.

### Web Process Death

On API 26 and above the system can kill the WebView's render process. The SDK handles this for its own WebView, so the widget does not crash the host app. A WebView of your own needs its own `onRenderProcessGone`.

1. The SDK replaces the dead WebView with a new one and clears the ready state, so `open` and `setMessageAttributes` hold.
2. The first time, it reloads the page once with the live conversation token.
3. If the process dies again before the widget is ready, it does not reload. It calls `onError` with `WEBVIEW_ERROR` and the message `Widget web process died again before ready`. The page is blank then, so close the screen from `onError`.
4. The first time any page gets ready, this resets. A manual `reload()`, an `open` or a second `init` does not reset it.

A held `open` message and unflushed message attributes stay held and go out at the next ready, attributes first. Attributes already sent are lost with the page. After step 3, a later `open(message)` or `reload()` still loads the page. The SDK does not retry on its own. On API 23 to 25 the system does not report this.

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

### Opening a Loaded Widget with a Message

`open` works on a view that is already initialised.

```kotlin
widgetView.open("I need help with my order.")
widgetView.open(mapOf("content" to "I need help with my order."))
```

- If the widget is ready, the message is sent now. A ready widget is never reloaded.
- If it is not ready, the message is held until it is. A later `open` replaces a held one.
- If the page fails to load, the message stays held until a later load succeeds, or until you call `init` again, which drops it.
- A null, empty string or empty map does nothing.
- A message object with no `content` and other keys, such as `submitted_values`, gets `content: "Hi"`, like the web SDK's `setOpenMessage`. This also applies to `initialMessageData` at init. A lone `content: ""` is sent as is.
- Call `open` and `setMessageAttributes` on the main thread. Before ready they only hold a value, so a background call seems to work. After ready they call `evaluateJavascript`, which throws off the main thread.
- `sendMessage` before the widget is ready is lost. Only `open` and `setMessageAttributes` wait.

`reload()` loads the widget page again. A held message and unflushed message attributes are kept and sent at the next ready.

### Example: Contextual Help Buttons

```kotlin
// In your MainActivity
val helpButton = findViewById<Button>(R.id.help_button)
helpButton.setOnClickListener {
    openWidgetWithMessage("I need help with my order.")
}
```

---

## 🏷️ Message Attributes

```kotlin
val ok = widgetView.setMessageAttributes(mapOf(
    "order_id" to "A-1042",
    "screen" to "checkout"
))
```

- They are attached as `custom_attributes` to the customer's next message only. Repeat calls merge, later keys win. They are message attributes, not contact attributes.
- You can call this at any time. Until the widget is ready they are held, and they are sent before an `open` message, so the first message carries them.
- It returns `false` for anything other than a non-empty map, or for a value that cannot be serialised. Nothing is sent then.
- The widget's auto-start `Hi` counts as the next message. With no welcome text, no pre-chat form and no conversation, the widget sends `Hi` on its own. If you did not call `open`, the attributes can land on `Hi` and not on what the customer types.
- Values must be json values: strings, numbers, booleans, `null`, and maps or lists of those. Anything else, such as a `Date`, makes the call return `false` and nothing is sent.
- After the customer's message is sent, the widget clears them. If that message fails, the widget keeps them for the next one. Attributes already sent to the widget are lost on a page reload. Attributes still held are kept. They also survive the widget being closed, because the view stays alive. Calling `init` a second time clears them. Calls made before the first `init` are kept.
- File attachments and email-only submits don't consume held attributes.
- Explicit `custom_attributes` on the message payload win over held ones for the same key.
- `LimechatWidgetButton.setMessageAttributes` forwards to the view set with `setWidgetView`. With no view it returns `false`.
- This needs a widget build containing `c8126fb7cd` (pr #120, 2026-09-25). Older builds ignore the event silently.

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

### LimechatWidgetView

| Member | Description |
| --- | --- |
| `init(config, callback = null)` | Load the widget. |
| `init(config, callback, initialMessage: String?)` | Load the widget and hold a first message. |
| `init(config, callback, initialMessageData: Map<String, Any>?)` | Same, with a message object. |
| `open(message: String? = null)` | Send a message, now if ready, else when ready. Null or empty does nothing. |
| `open(messageData: Map<String, Any>)` | Same, with a message object. Empty does nothing. With no `content` and other keys, such as `submitted_values`, it gets `content: "Hi"`, like the web SDK's `setOpenMessage`. |
| `setMessageAttributes(attributes: Map<String, Any?>?): Boolean` | See Message Attributes. |
| `sendMessage(event: String, data: Map<String, Any> = emptyMap())` | Post an event to the widget. Lost if the widget is not ready. |
| `reload()` | Load the widget page again. Does nothing before `init`. |
| `reset()` | Clear this widget's conversation and reload it. Does not close the widget. Call it on logout. Does nothing before `init`. |
| `LimechatWidgetView.reset(context: Context, instanceId: String? = null)` | Static. Clear the stored token for an instance (`default` for null or blank). Does not reload a view that is showing. The static `reset(context, instanceId)` only clears stored state. if a view is showing, call its `reset()` instead, or the view keeps its live token and stores it again. |
| `onBackPressed(): Boolean` | Go back in the web view. Returns `false` if it cannot. |
| `attachFilePicker(filePicker: WidgetFilePicker)` | Enable file uploads. |
| `destroy()` | Release the web view. |

Calling `init` again clears a held open message and unflushed message attributes.

### LimechatWidgetButton

| Member | Description |
| --- | --- |
| `init(config, customButton: View? = null)` | Show the default icon, or wrap your own view. |
| `setOnClickListener(listener)` | Click handling for the button. |
| `updateUnreadCount(count: Int)` | Set the badge. 0 or less hides it, above 99 shows `99+`. |
| `setWidgetView(widgetView: LimechatWidgetView)` | Needed by `open` and `setMessageAttributes`. |
| `open(message: String? = null)`, `open(messageData: Map<String, Any>)` | Forward to the widget view. Logs a warning with no view. |
| `setMessageAttributes(attributes: Map<String, Any?>?): Boolean` | Forwards to the widget view. `false` with no view. |
| `destroy()` | Cancel the button's background work. |

### WidgetConfig.Builder

`WidgetConfig.builder(websiteToken)` returns the builder. `build()` throws `WidgetException.ConfigurationError` for a blank token, locale or instance ID, or an invalid base URL.

| Method | Description |
| --- | --- |
| `setLocale(locale: String)` | Language code. Default `en`. |
| `setUser(user: WidgetConfig.User?)` | User details. |
| `setCustomAttributes(attributes: Map<String, Any>?)` | Custom metadata sent with the widget url. |
| `setBaseUrl(baseUrl: String)` | Custom domain. |
| `setExternalTokenManagement(token: String?, onTokenChange: ((String) -> Unit)?)` | You store the conversation token. Either value can be omitted. |
| `setInstanceId(instanceId: String?)` | Isolates stored conversations. No `:` or `/`. |
| `setShowLegacyBackIcon(showLegacyBackIcon: Boolean)` | Legacy close behaviour. |
| `setHideFilePicker(hideFilePicker: Boolean)` | Hide the attachment button. |

### WidgetConfig.User

`name`, `email`, `phoneNumber`, `identifier`, `identifierHash`, `customAttributes: Map<String, Any>?`. All are optional. `identifier` and `identifierHash` work only together.

### WidgetCallback

| Method | Description |
| --- | --- |
| `onLoaded()` | The widget is ready. Once per page load. |
| `onClose()` | The user closed the widget. |
| `onError(error: WidgetError)` | A load, http or script error. |
| `onMessage(message: Map<String, Any?>)` | Widget events other than close and token. None are sent today. |
| `onConversationTokenChange(token: String)` | Optional. The conversation token changed. |

### WidgetError

`code` (`WEBVIEW_ERROR` or `JAVASCRIPT_ERROR`), `message`, `originalError: Throwable?`, `context: Map<String, Any>?`. A main page HTTP error has `statusCode` in `context`. Other `ErrorCode` values are deprecated and unused, and will be removed in the next major release.

---

## 📞 Support

### Getting Help

- **Documentation**: This integration guide
- **Issues**: Report bugs or feature requests on GitHub
- **Support**: Contact LimeChat support team

### Version Updates

Stay updated with the latest SDK versions:

- **Current Version**: v0.2.0-beta.1
- **Changelog**: Check GitHub releases
- **Migration Guides**: Available for major version updates

---

## 📄 License

LimeChat Widget Android SDK is distributed under the MIT License. See LICENSE file for details.

---

**Ready to integrate? Start with the [Basic Integration](#-basic-integration) section and have your chat widget running in minutes!**

For more examples, check out our sample applications in this repository.