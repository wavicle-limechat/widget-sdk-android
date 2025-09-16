# Published SDK Demo

This demonstrates the LimeChat Widget Android SDK working with the published JitPack dependency.

## ✅ What This Proves

1. **SDK Published Successfully**: Downloads from JitPack and works
2. **Independent Operation**: No local project dependencies
3. **Complete Functionality**: Widget, badges, callbacks all working
4. **Production Ready**: External developers can use the SDK

## 🎯 How to Verify Published vs Local

### Published SDK (This Demo)

- **Dependency**: `com.github.wavicle-limechat:widget-sdk-android:0.0.8`
- **Logs**: `🚀 PUBLISHED SDK DEMO - Using JitPack dependency`
- **UI**: Green banner shows "Using PUBLISHED SDK from JitPack"
- **Structure**: Standalone project, no `/widget-sdk` folder

### Local SDK (sample-app, example-project)

- **Dependency**: `implementation(project(":widget-sdk"))`
- **Logs**: Standard widget logging
- **UI**: No "PUBLISHED SDK" indicators
- **Structure**: Part of main project with `/widget-sdk` source

## 🚀 Usage

1. **Wait for JitPack Build**: v0.0.8 must complete on JitPack
2. **Open in Android Studio**: This folder only
3. **Gradle Sync**: Downloads SDK from JitPack automatically
4. **Run**:
   - Green status shows "Using PUBLISHED SDK"
   - Widget appears bottom-right
   - All functions work independently

## ✅ Configuration

**Pre-configured with working LimeChat token:**

```kotlin
private const val WEBSITE_TOKEN = "MEFFACy4xaovJayhLjSt836h"
private const val BASE_URL = "https://app.limechat.ai"
```

**Widget URL:** `https://app.limechat.ai/widget?website_token=MEFFACy4xaovJayhLjSt836h`

## ⚡ Simple Architecture

```
published-sdk-demo/          # Standalone project
├── app/
│   ├── build.gradle.kts     # JitPack dependency ONLY
│   └── src/main/            # Uses published SDK classes
├── gradle.properties        # AndroidX configured
└── settings.gradle.kts      # JitPack repository
```

**No `/widget-sdk` folder = No local dependencies = Published SDK Proof** ✅

## 🎯 Key Features

- **AndroidX Compatible**: Configured with `android.useAndroidX=true`
- **JitPack Integration**: Uses published dependency from JitPack
- **Clean Dependencies**: Minimal, clear JitPack-only dependency
- **Clear Distinction**: UI and logs clearly indicate published SDK usage
- **Production Ready**: Demonstrates SDK works independently

This demonstrates the SDK is published and working correctly! 🚀
