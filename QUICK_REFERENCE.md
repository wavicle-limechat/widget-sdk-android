# LimeChat Android SDK - Quick Reference

## 🚀 5-Minute Setup

### 1. Add Dependency
```kotlin
implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.4")
```

### 2. Add JitPack Repository
```kotlin
// settings.gradle.kts
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

### 3. Basic Integration
```kotlin
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN",
    user = WidgetConfig.User(name = "John", email = "john@example.com")
)

val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config)
yourContainer.addView(widgetButton)
```

## 📱 Common Use Cases

### Widget Button (Floating)
```kotlin
// Bottom-right floating button
val widgetButton = LimechatWidgetButton(this).apply {
    layoutParams = FrameLayout.LayoutParams(
        WRAP_CONTENT, WRAP_CONTENT
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.END
        setMargins(0, 0, 16.dp, 16.dp)
    }
}
widgetButton.init(config)
container.addView(widgetButton)
```

### Full-Screen Widget
```kotlin
class ChatActivity : FragmentActivity() {
    private lateinit var widgetView: LimechatWidgetView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        widgetView = LimechatWidgetView(this)
        setContentView(widgetView)
        
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() { /* Widget ready */ }
            override fun onClose() { finish() }
            override fun onError(error: WidgetError) { /* Handle error */ }
            override fun onMessage(message: Map<String, Any>) { /* Handle events */ }
        })
    }
}
```

### File Upload Support
```kotlin
// In your activity's onCreate
val widgetFilePicker = WidgetFilePicker(this)
widgetView.attachFilePicker(widgetFilePicker)
```

## 🎨 Customization Examples

### Custom Button
```kotlin
val customButton = MaterialButton(this).apply {
    text = "💬 Chat Support"
    setBackgroundColor(Color.BLUE)
}
widgetButton.init(config, customButton)
```

### Theme Integration
```kotlin
val config = WidgetConfig(
    websiteToken = "TOKEN",
    colorScheme = WidgetConfig.ColorScheme.AUTO // Follows system theme
)
```

### User Context
```kotlin
val config = WidgetConfig(
    websiteToken = "TOKEN",
    user = WidgetConfig.User(
        name = "John Doe",
        email = "john@company.com",
        phoneNumber = "+1234567890"
    ),
    customAttributes = mapOf(
        "user_id" to "12345",
        "plan" to "premium",
        "app_version" to BuildConfig.VERSION_NAME
    )
)
```

## 📡 Event Handling

### Unread Count Updates
```kotlin
override fun onMessage(message: Map<String, Any>) {
    when (message["event"]) {
        "unread-count-changed" -> {
            val count = message["count"] as? Int ?: 0
            widgetButton.updateUnreadCount(count)
        }
    }
}
```

### Error Handling
```kotlin
override fun onError(error: WidgetError) {
    when (error.code) {
        WidgetError.ErrorCode.NETWORK_ERROR -> showRetryDialog()
        WidgetError.ErrorCode.CONFIG_ERROR -> showConfigError()
        else -> showGenericError(error.message)
    }
}
```

## 🔧 Common Issues & Fixes

### Dependency Resolution
```kotlin
// If "Could not resolve dependency" error:
repositories {
    google()
    mavenCentral() 
    maven { url = uri("https://jitpack.io") }
}
```

### AndroidX Issues
```properties
# Add to gradle.properties
android.useAndroidX=true
android.enableJetifier=true
```

### ProGuard Rules
```proguard
# Add to proguard-rules.pro
-keep class ai.limechat.widget.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
```

### File Picker Not Working
```kotlin
// Must create in onCreate, not later
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val widgetFilePicker = WidgetFilePicker(this) // ← Create here
    widgetView.attachFilePicker(widgetFilePicker)
}
```

## 📋 Checklist

### Before Integration
- [ ] JitPack repository added
- [ ] Internet permission in AndroidManifest.xml
- [ ] AndroidX enabled in gradle.properties
- [ ] Your LimeChat website token ready

### Testing Checklist  
- [ ] Widget button appears and clickable
- [ ] Widget opens and loads correctly
- [ ] File upload works (if needed)
- [ ] Proper cleanup in onDestroy()
- [ ] Works in release build with ProGuard

### Go Live Checklist
- [ ] Replace test token with production token
- [ ] Test on multiple devices/screen sizes
- [ ] Error handling implemented
- [ ] Analytics integration (optional)
- [ ] Performance testing completed

## 🔗 Useful Links

- **Full Integration Guide**: [CLIENT_INTEGRATION_GUIDE.md](./CLIENT_INTEGRATION_GUIDE.md)
- **Working Example**: [published-sdk-demo/](./published-sdk-demo/)
- **JitPack Status**: https://jitpack.io/#wavicle-limechat/widget-sdk-android
- **GitHub Repository**: https://github.com/wavicle-limechat/widget-sdk-android

**Need help? Check the [troubleshooting section](./CLIENT_INTEGRATION_GUIDE.md#-troubleshooting) in the full guide!**