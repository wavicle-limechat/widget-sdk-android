# Published SDK vs Local SDK - Key Differences 🔍

This guide shows how to distinguish between the published JitPack SDK and the local development SDK.

## 🏗️ **Build Configuration Differences**

### Published SDK Demo (`/published-sdk-demo/`)
```kotlin
// app/build.gradle.kts
dependencies {
    // ONLY JitPack dependency - no local project references
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.1")
}
```

### Local SDK (sample-app, example-project)
```kotlin
// app/build.gradle.kts  
dependencies {
    // Local project dependency
    implementation(project(":widget-sdk"))
}
```

## 📱 **Visual UI Differences**

### Published SDK Demo
- **Green Banner**: "✅ PUBLISHED SDK v0.0.1 📦 From JitPack"
- **Toast Messages**: "🚀 Using PUBLISHED SDK v0.0.1 from JitPack"
- **Button Text**: "✅ PUBLISHED SDK v0.0.1 works!"

### Local SDK Apps
- **No Special Banner**: Standard UI without version indicators
- **Toast Messages**: Standard messages without "JitPack" reference
- **Button Text**: Standard widget button interactions

## 📊 **Log Message Differences**

### Published SDK Demo
```
D/PublishedSDKDemo: 🚀 Starting Published SDK Demo
D/PublishedSDKDemo: 📦 Using: com.github.wavicle-limechat:widget-sdk-android:0.0.1
D/PublishedSDKDemo: 🔍 This is PUBLISHED SDK - downloaded from JitPack
D/PublishedSDKDemo: ✅ Widget button test clicked - using PUBLISHED SDK from JitPack
D/PublishedSDKDemo: 🎯 Widget button clicked - PUBLISHED SDK from JitPack working!
```

### Local SDK Apps
```
D/ButtonDemoActivity: Widget initialized successfully
D/SampleActivity: Widget button clicked
```

## 🔧 **How to Verify You're Using Published SDK**

1. **Check build.gradle.kts**: Look for JitPack dependency, not `project(":widget-sdk")`
2. **Gradle sync**: Should download from JitPack (check Gradle console)
3. **Run app**: Look for green "PUBLISHED SDK" banner
4. **Check logs**: Should contain "PUBLISHED SDK" and "JitPack" references
5. **Toast messages**: Should mention "from JitPack"

## 📁 **Project Structure Differences**

### Published SDK Demo
```
published-sdk-demo/
├── app/
│   ├── build.gradle.kts      # JitPack dependency only
│   └── src/main/java/        # Uses published SDK classes
├── settings.gradle.kts       # JitPack repository configured
└── NO widget-sdk/ folder     # No local SDK source
```

### Local SDK Projects
```
limechat-android-sdk/
├── widget-sdk/               # Local SDK source code
├── sample-app/
│   ├── build.gradle.kts      # project(":widget-sdk") dependency
│   └── src/main/java/        # Uses local SDK classes
└── settings.gradle.kts       # Includes ":widget-sdk"
```

## 🚀 **Testing Steps**

### To Test Published SDK:
1. Open `/published-sdk-demo/` in Android Studio
2. Gradle sync will download from JitPack
3. Run app - should show green "PUBLISHED SDK" banner
4. Check logs for "PUBLISHED SDK from JitPack" messages

### To Test Local SDK:
1. Open `/sample-app/` or `/example-project/` in Android Studio  
2. Gradle sync uses local `:widget-sdk` project
3. Run app - standard UI without "PUBLISHED SDK" indicators
4. Check logs for standard widget messages

## ⚠️ **Common Mistakes**

❌ **Using local and published together**: Don't mix dependencies  
❌ **Wrong repository**: Published SDK needs JitPack repository  
❌ **Wrong coordinates**: Use exact `com.github.wavicle-limechat:widget-sdk-android:0.0.1`  
❌ **Cached builds**: Clear Gradle cache if seeing old behavior  

## ✅ **Success Indicators**

**You're using Published SDK when:**
- ✅ Green banner shows "PUBLISHED SDK v0.0.1"
- ✅ Toast says "from JitPack" 
- ✅ Logs contain "PUBLISHED SDK" messages
- ✅ Gradle sync downloads from JitPack
- ✅ No local `widget-sdk/` folder in project

**You're using Local SDK when:**
- ✅ Standard widget UI without version banners
- ✅ Toast messages don't mention "JitPack"
- ✅ Logs show standard widget messages
- ✅ Gradle sync uses local project
- ✅ Local `widget-sdk/` folder exists in project

This ensures clear distinction between published and local SDK usage! 🎯