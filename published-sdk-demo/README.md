# Published SDK Demo 🚀

This is a standalone Android application that demonstrates using **ONLY** the published LimeChat Widget SDK from JitPack.

## Key Features

✅ **Published SDK Only** - Uses `com.github.wavicle-limechat:widget-sdk-android:0.0.1`  
✅ **No Local Dependencies** - Completely independent from the SDK source code  
✅ **JitPack Integration** - Direct dependency resolution from JitPack  
✅ **Widget Button** - Shows default widget icon fetched from API  
✅ **Badge Updates** - Demonstrates unread count functionality  

## Dependencies

```kotlin
dependencies {
    // ONLY the published SDK - no local project references
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.1")
    
    // Standard Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.google.android.material:material:1.11.0")
}
```

## How to Run

1. **Ensure SDK is published**: The v0.0.1 tag must be pushed to GitHub for JitPack to build it
2. **Open in Android Studio**: Open this `published-sdk-demo` folder as an Android project
3. **Sync dependencies**: Let Gradle download the published SDK from JitPack
4. **Run the app**: The widget button will appear in the bottom-right corner

## What This Proves

This demo app proves that:

- ✅ The SDK is properly published to JitPack
- ✅ External apps can use the SDK without any local source dependencies
- ✅ The SDK works independently with all its features
- ✅ Widget icon fetching from API works
- ✅ Badge updates work correctly
- ✅ The SDK is production-ready for distribution

## Usage Example

```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

// Create configuration
val config = WidgetConfig(
    websiteToken = "YOUR_TOKEN",
    locale = "en",
    colorScheme = WidgetConfig.ColorScheme.LIGHT
)

// Create and initialize widget button
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config)

// Add to your layout
yourLayout.addView(widgetButton)
```

## Configuration

Update `MainActivity.kt` with your actual website token:

```kotlin
private const val WEBSITE_TOKEN = "your_actual_website_token_here"
```

## Build Requirements

- Android Studio Arctic Fox or newer
- Gradle 8.0+
- Min SDK: 23 (Android 6.0)
- Target SDK: 35 (Android 15)

This app demonstrates that the LimeChat Widget Android SDK is successfully published and ready for production use! 🎉