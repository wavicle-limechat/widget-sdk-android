# LimeChat Widget SDK - Example Project

This is a minimal example Android project demonstrating how to integrate the LimeChat Widget SDK using the published JitPack dependency.

## 🚀 Quick Start

1. **Clone or Download** this example project
2. **Open** in Android Studio
3. **Replace** `YOUR_WEBSITE_TOKEN` with your actual website token
4. **Build and Run** the project

## 📁 Project Structure

```
example-project/
├── app/
│   ├── build.gradle.kts          # Dependencies and configuration
│   └── src/main/
│       ├── AndroidManifest.xml   # Permissions and activities
│       ├── java/com/example/limechatdemo/
│       │   ├── MainActivity.kt    # Widget button integration
│       │   ├── WidgetActivity.kt  # Full-screen widget
│       │   └── CustomButtonActivity.kt # Custom button example
│       └── res/layout/
│           ├── activity_main.xml
│           ├── activity_widget.xml
│           └── activity_custom_button.xml
├── build.gradle.kts              # Project-level configuration
└── settings.gradle.kts           # Repository configuration
```

## ⚙️ Configuration

### 1. JitPack Repository (already configured)

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

### 2. SDK Dependency (already added)

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:1.0.0")
    // ... other dependencies
}
```

### 3. Required Permissions (already added)

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

## 🔧 Customize for Your App

### Replace Website Token

Find and replace `YOUR_WEBSITE_TOKEN` in:
- `MainActivity.kt`
- `WidgetActivity.kt` 
- `CustomButtonActivity.kt`

```kotlin
val config = WidgetConfig(
    websiteToken = "your-actual-website-token-here"
)
```

### Update User Information

```kotlin
val config = WidgetConfig(
    websiteToken = "your-token",
    user = WidgetConfig.User(
        name = "Your User Name",
        email = "user@yourapp.com",
        phoneNumber = "+1234567890"
    )
)
```

### Customize Appearance

```kotlin
val config = WidgetConfig(
    websiteToken = "your-token",
    locale = "en", // Change to your app's locale
    colorScheme = WidgetConfig.ColorScheme.AUTO // LIGHT, DARK, or AUTO
)
```

## 📱 What This Example Demonstrates

### 1. Widget Button Integration (`MainActivity`)
- Default widget icon (fetched from API)
- Unread count badge
- Click to open full-screen widget
- Proper lifecycle management

### 2. Full-Screen Widget (`WidgetActivity`)
- Complete widget interface
- File upload support
- Error handling
- Custom message handling
- Back navigation

### 3. Custom Button (`CustomButtonActivity`)
- Custom button styling
- SDK handles click events
- Unread count on custom button

## 🧪 Testing

1. **Build the project** - Should compile without errors
2. **Run on device/emulator** - Widget button should appear
3. **Click widget button** - Should open full-screen widget
4. **Test file uploads** - Should work in full-screen mode
5. **Test back navigation** - Should work properly

## 📋 Checklist

- [ ] Project builds successfully
- [ ] No compilation errors
- [ ] Widget token is configured
- [ ] Widget button appears
- [ ] Full-screen widget opens
- [ ] File uploads work
- [ ] Error handling works
- [ ] Back navigation works

## 🔄 Updating SDK Version

To update to a newer version of the SDK:

1. Check [JitPack page](https://jitpack.io/#wavicle-limechat/widget-sdk-android) for latest version
2. Update version in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.wavicle-limechat:widget-sdk-android:NEW_VERSION")
}
```

3. Sync project and test

## 📚 Learn More

- [Complete Integration Guide](../INTEGRATION_EXAMPLE.md)
- [Widget Button Guide](../WIDGET_BUTTON_GUIDE.md)
- [API Reference](../README.md)
- [JitPack Publishing](../JITPACK_PUBLISHING.md)

## ⚠️ Important Notes

1. **Replace the website token** with your actual token from LimeChat dashboard
2. **Test on real devices** for best performance evaluation
3. **Handle network connectivity** issues in production apps
4. **Follow Android best practices** for activity lifecycle management

## 🆘 Need Help?

- Check the [main documentation](../README.md)
- Review the [troubleshooting guide](../INTEGRATION_EXAMPLE.md#-troubleshooting)
- Create an issue on GitHub with your specific problem

---

**Ready to integrate LimeChat into your app! 🚀**