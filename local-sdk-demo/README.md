# Local SDK Demo

This is an example Android application that demonstrates how to use the **local** `widget-sdk` module instead of the published JitPack dependency.

## Key Differences from Published SDK Demo

| Aspect | Published SDK Demo | Local SDK Demo |
|--------|-------------------|----------------|
| **Dependency** | `implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.9-alpha")` | `implementation(project(":widget-sdk"))` |
| **Source** | JitPack published artifact | Local project module |
| **Purpose** | Proves published SDK works independently | Proves local SDK works for development |
| **Use Case** | Production integration testing | Development and testing |

## Project Structure

```
local-sdk-demo/
├── app/
│   ├── build.gradle.kts          # Uses local widget-sdk module
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/example/localsdkdemo/
│   │   │       ├── MainActivity.kt      # Main demo activity
│   │   │       └── WidgetActivity.kt    # Full-screen widget
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    # Main UI layout
│   │   │   └── values/
│   │   │       ├── strings.xml          # App strings
│   │   │       ├── colors.xml           # Theme colors
│   │   │       └── themes.xml           # App theme
│   │   └── AndroidManifest.xml          # App manifest
│   └── build.gradle.kts                 # Module-level config
├── build.gradle.kts                     # Module-level plugins
└── README.md                            # This file
```

## How to Use

### 1. Build and Run

```bash
# From the main project directory
./gradlew :local-sdk-demo:assembleDebug
./gradlew :local-sdk-demo:installDebug
```

### 2. Verify Local SDK Usage

- Check logs for "LOCAL SDK DEMO" messages
- Verify dependency in `app/build.gradle.kts` shows `:widget-sdk`
- Widget button appears in bottom-right corner
- All functionality works with local development

### 3. Key Features Demonstrated

- **Widget Button**: Floating action button with badge support
- **Full Widget**: Full-screen chat widget integration
- **Configuration**: Custom user attributes and settings
- **File Upload**: File picker integration for attachments
- **Callbacks**: Event handling for widget lifecycle

## Configuration

The demo uses a live LimeChat token: `MEFFACy4xaovJayhLjSt836h`

This allows testing with real backend services while using the local SDK.

## Development Workflow

1. **Make changes** to the `widget-sdk` module
2. **Test changes** using this local-sdk-demo app
3. **Verify functionality** works as expected
4. **Publish to JitPack** when ready for production

## Benefits of Local SDK Demo

- ✅ **Immediate testing** of SDK changes
- ✅ **No publishing delays** for development iterations
- ✅ **Full source access** for debugging
- ✅ **Version control** integration
- ✅ **CI/CD pipeline** testing

## Comparison with Published SDK Demo

The `published-sdk-demo` proves the SDK works independently after publishing, while this `local-sdk-demo` proves the SDK works during local development.

Both demos are essential for a complete development workflow.

## Building the Project

Since this is a module within the main project, you can build it using:

```bash
# Build the entire project
./gradlew build

# Build just the local-sdk-demo
./gradlew :local-sdk-demo:build

# Build and install the local-sdk-demo
./gradlew :local-sdk-demo:installDebug
```

## Troubleshooting

If you encounter build issues:

1. **Clean and rebuild**: `./gradlew clean build`
2. **Check module inclusion**: Ensure `:local-sdk-demo` is in `settings.gradle.kts`
3. **Verify dependencies**: Check that `:widget-sdk` module exists and builds
4. **Sync project**: Refresh Gradle project in Android Studio
