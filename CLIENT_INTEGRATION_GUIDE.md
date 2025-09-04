# LimeChat Widget Android SDK - Client Integration Guide

**A comprehensive guide for integrating the LimeChat widget into your Android application.**

| Version | Repository                               | Platform      |
| :------ | :--------------------------------------- | :------------ |
| 0.0.6   | `com.github.wavicle-limechat:widget-sdk-android` | Android API 23+ |

---

## 📋 Table of Contents

1. [Getting Started](#-getting-started)
    - [Requirements](#requirements)
    - [Step 1: Add Repository](#step-1-add-repository)
    - [Step 2: Add Dependency](#step-2-add-dependency)
    - [Step 3: Add Permissions](#step-3-add-permissions)
2. [Basic Integration](#-basic-integration)
    - [Displaying the Widget Button](#displaying-the-widget-button)
3. [Advanced Configuration](#-advanced-configuration)
    - [WidgetConfig Options](#widgetconfig-options)
    - [Custom Attributes](#custom-attributes)
4. [Customizing the Widget Button](#-customizing-the-widget-button)
    - [Using a Custom Button](#using-a-custom-button)
    - [Positioning the Button](#positioning-the-button)
    - [Displaying Unread Messages](#displaying-unread-messages)
5. [Full-Screen Widget](#-full-screen-widget)
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
12. [Comparison with React Native SDK](#-comparison-with-react-native-sdk)

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
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.6")

    // The SDK requires the following AndroidX libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.11.0")
}
```

> **Note on Dependency Versions:** You may find other documentation referencing different dependency names or versions. For the latest stable release, please use the dependency specified above.

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

1.  **Configure the Widget**: Create a `WidgetConfig` object with your `websiteToken` and user details.
2.  **Create the Button**: Instantiate `LimechatWidgetButton`.
3.  **Add to Layout**: Add the button to your activity's layout.

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

> **Tip**: Create a simple extension function to convert `dp` to pixels: `fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()`

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

1.  **Create an Activity**: Create a new `FragmentActivity` to host the `LimechatWidgetView`.
2.  **Add the View**: Add the `LimechatWidgetView` to the activity's layout.
3.  **Initialize**: Initialize the view with your `WidgetConfig`.
4.  **Handle Back Press**: Override `onBackPressed` to allow the widget to handle navigation.

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

## 📡 Handling Events

You can listen for events from the widget using the `WidgetCallback`.

### Implementing WidgetCallback

```kotlin
widgetView.init(config, object : WidgetCallback {
    override fun onLoaded() {
        // Widget has finished loading
    }

    override fun onClose() {
        // User has requested to close the widget
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
- **ProGuard Issues**: If the widget crashes in release builds, add the ProGuard rules from the `widget-sdk/proguard-rules.pro` file to your app's ProGuard configuration.

---

## 📚 API Reference

For detailed information on the SDK's classes and methods, please refer to the source code and the `LimechatWidgetButton`, `LimechatWidgetView`, and `WidgetConfig` classes.

---

## 🆚 Comparison with React Native SDK

This section provides a brief comparison between the native Android SDK and the React Native SDK for developers familiar with both platforms.

### Key Differences

| Feature           | React Native SDK                | Android Native SDK                  |
| ----------------- | ------------------------------- | ----------------------------------- |
| **Integration**   | JSX Component (`<LimeChatWidget>`) | View (`LimechatWidgetView`) or Activity |
| **Custom Button** | `customButton` prop with JSX    | Launch a dedicated `Activity`       |
| **Unread Count**  | Built-in with styling props     | Manual implementation required      |
| **Configuration** | Props-based                     | `WidgetConfig` object               |

### Example Comparison

**React Native:**

```jsx
<LimeChatWidget
  websiteToken="YOUR_TOKEN"
  user={user}
  customButton={<MyCustomButton />}
/>
```

**Android Native:**

```kotlin
// Configuration
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    user = user
)

// Launching the widget activity from a custom button
myCustomButton.setOnClickListener {
    val intent = Intent(this, ChatActivity::class.java)
    // Add config to intent if needed
    startActivity(intent)
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
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.6")
    
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
implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.6")
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

For more examples, check out our [sample applications](./published-sdk-demo/) in this repository.inutes!**

For more examples, check out our [sample applications](./published-sdk-demo/) in this repository.