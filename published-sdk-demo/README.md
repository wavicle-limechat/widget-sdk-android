# Published SDK Demo 🚀

This is a standalone Android application that demonstrates using **ONLY** the published LimeChat Widget SDK from JitPack.

## Key Features

✅ **Published SDK Only** - Uses `com.github.wavicle-limechat:widget-sdk-android:0.0.4`  
✅ **No Local Dependencies** - Completely independent from the SDK source code  
✅ **JitPack Integration** - Direct dependency resolution from JitPack  
✅ **Dynamic Version Display** - Shows actual SDK version from published JAR  
✅ **Widget Button** - Shows default widget icon fetched from API  
✅ **Badge Updates** - Demonstrates unread count functionality  
✅ **Clear Visual Indicators** - Green banner shows published version info

## How to Identify Published vs Local SDK

### 🟢 **Published SDK (this app)**
- **Green Banner**: Shows actual version like `✅ PUBLISHED: 0.0.4-published`
- **Toast Messages**: Include actual version from `getSDKVersion()`
- **Logs**: Show version from published JAR
- **Dependency**: `com.github.wavicle-limechat:widget-sdk-android:0.0.4`

### 🔘 **Local SDK** (sample-app, example-project)
- **Version Display**: Shows `0.0.5-local-dev` 
- **Standard UI**: No green version banner
- **Dependency**: `project(":widget-sdk")`

## Dependencies

```kotlin
dependencies {
    // ONLY the published SDK - no local project references
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.4")
    
    // Standard Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.google.android.material:material:1.11.0")
}
```

## How to Run

1. **Wait for JitPack Build**: v0.0.4 must be successfully built on JitPack
2. **Open in Android Studio**: Open this `published-sdk-demo` folder as an Android project
3. **Sync Dependencies**: Gradle will download the published SDK from JitPack
4. **Run the App**: 
   - Green banner will show actual published version
   - Toast messages will include version info
   - Logs will show published SDK version
   - Widget button appears in bottom-right corner

## What This Proves

This demo app proves that:

- ✅ The SDK is properly published to JitPack
- ✅ External apps can use the SDK without any local source dependencies
- ✅ The SDK works independently with all its features
- ✅ Version tracking works correctly (published vs local)
- ✅ Widget icon fetching from API works
- ✅ Badge updates work correctly
- ✅ The SDK is production-ready for distribution

## Testing Steps

1. **Build Status**: Check that v0.0.3 is built on JitPack
2. **Open Project**: Load this folder in Android Studio
3. **Gradle Sync**: Should download from JitPack without errors
4. **Run App**: 
   - Should show green banner with actual version
   - Toast should say "Published [version] from JitPack!"
   - Logs should show published version info
   - Widget functionality should work

## Expected Output

### UI Elements
- **Green Banner**: `✅ PUBLISHED: 0.0.4-published 📦 From JitPack`
- **Toast on Load**: `🚀 Using Published SDK: 0.0.4-published`
- **Test Button Toast**: `✅ Published 0.0.4-published works!`
- **Widget Click Toast**: `🎯 Widget clicked! Published 0.0.4-published from JitPack!`

### Logs
```
D/PublishedSDKDemo: 🚀 Starting Published SDK Demo
D/PublishedSDKDemo: 📦 Using: com.github.wavicle-limechat:widget-sdk-android:0.0.4
D/PublishedSDKDemo: 🔍 SDK Version from JitPack: 0.0.4-published
D/LimechatWidgetButton: 🚀 LimeChat Widget SDK v0.0.4-published initializing...
```

## Configuration

Update `MainActivity.kt` with your actual website token:

```kotlin
private const val WEBSITE_TOKEN = "your_actual_website_token_here"
```

## Build Requirements

- Android Studio Arctic Fox or newer
- Gradle 8.5+
- Min SDK: 23 (Android 6.0)
- Target SDK: 35 (Android 15)
- JitPack v0.0.4 must be successfully built

## JitPack Status

Check build status at: https://jitpack.io/#wavicle-limechat/widget-sdk-android

Current status:
- v0.0.4: Building/Ok ✅
- v0.0.3: Error ❌
- v0.0.2: Error ❌
- v0.0.1: Error ❌

## Troubleshooting

### "Could not find" Error
- **Cause**: JitPack build not ready
- **Solution**: Wait for build completion, check JitPack status

### Version Shows "local-dev"
- **Cause**: Using local SDK instead of published
- **Solution**: Check dependency is JitPack URL, not `project(":widget-sdk")`

### No Green Banner
- **Cause**: Wrong project or dependency
- **Solution**: Ensure using this `published-sdk-demo` folder

This app demonstrates that the LimeChat Widget Android SDK is successfully published and ready for production use! 🎉