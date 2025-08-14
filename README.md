# Limechat Android Widget SDK

A robust Android library for integrating the Limechat widget into Android applications. The library provides a hardened WebView-based implementation with secure messaging, file uploads, and comprehensive callback support.

## Features

- **Secure WebView Integration**: Hardened WebView with SafeBrowse, blocked mixed content, and restricted file access
- **Modern Messaging**: Uses `WebMessageListener` with `JavascriptInterface` fallback for reliable communication
- **File Upload Support**: Handle file uploads without requiring storage permissions using `ActivityResultContracts`
- **Customizable Configuration**: Support for user data, locale, color schemes, and custom attributes
- **Back Navigation**: Built-in WebView back stack handling
- **Kotlin & Java Compatible**: Full compatibility with both Kotlin and Java projects
- **ProGuard Ready**: Includes consumer ProGuard rules for release builds

## Installation

### JitPack (Recommended)

Add JitPack repository to your project's `settings.gradle` or root `build.gradle`:

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

Add the dependency to your app's `build.gradle`:

```kotlin
dependencies {
    implementation("com.github.limechat:widget-sdk:1.0.0")
}
```

### Local AAR

1. Download the AAR from releases
2. Place it in your `app/libs` folder
3. Add to `build.gradle`:

```kotlin
dependencies {
    implementation(files("libs/widget-sdk-1.0.0.aar"))
}
```

## Requirements

- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Dependencies**: `androidx.webkit`

## Quick Start

### 1. Add Widget to Layout

```xml
<ai.limechat.widget.LimechatWidgetView
    android:id="@+id/widgetView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 2. Configure and Initialize

#### Kotlin

```kotlin
class MainActivity : FragmentActivity() {
    
    private lateinit var widgetView: LimechatWidgetView
    private lateinit var widgetFilePicker: WidgetFilePicker
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        widgetView = findViewById(R.id.widgetView)
        
        // IMPORTANT: Initialize file picker in onCreate to properly register ActivityResultLauncher
        widgetFilePicker = WidgetFilePicker(this)
        widgetView.attachFilePicker(widgetFilePicker)
        
        val config = WidgetConfig(
            websiteToken = "your-website-token",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT,
            user = WidgetConfig.User(
                name = "John Doe",
                email = "john@example.com",
                phoneNumber = "+1234567890"
            ),
            customAttributes = mapOf(
                "department" to "support",
                "priority" to "high"
            )
        )
        
        widgetView.init(config, object : WidgetCallback {
            override fun onLoaded() {
                Log.d("Widget", "Widget loaded successfully")
            }
            
            override fun onClose() {
                Log.d("Widget", "Widget closed")
            }
            
            override fun onError(error: WidgetError) {
                Log.e("Widget", "Widget error: ${error.message}")
            }
            
            override fun onMessage(message: Map<String, Any>) {
                Log.d("Widget", "Message received: $message")
            }
        })
    }
    
    override fun onBackPressed() {
        if (!widgetView.onBackPressed()) {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        widgetView.destroy()
    }
}
```

#### Java

```java
public class MainActivity extends FragmentActivity {
    
    private LimechatWidgetView widgetView;
    private WidgetFilePicker widgetFilePicker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        widgetView = findViewById(R.id.widgetView);
        
        // IMPORTANT: Initialize file picker in onCreate to properly register ActivityResultLauncher
        widgetFilePicker = new WidgetFilePicker(this);
        widgetView.attachFilePicker(widgetFilePicker);
        
        WidgetConfig.User user = new WidgetConfig.User(
            "John Doe", 
            "john@example.com", 
            "+1234567890", 
            null
        );
        
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("department", "support");
        customAttributes.put("priority", "high");
        
        WidgetConfig config = new WidgetConfig(
            "your-website-token",
            "en",
            WidgetConfig.ColorScheme.LIGHT,
            user,
            customAttributes,
            "https://app.limechat.ai"
        );
        
        widgetView.init(config, new WidgetCallback() {
            @Override
            public void onLoaded() {
                Log.d("Widget", "Widget loaded successfully");
            }
            
            @Override
            public void onClose() {
                Log.d("Widget", "Widget closed");
            }
            
            @Override
            public void onError(WidgetError error) {
                Log.e("Widget", "Widget error: " + error.getMessage());
            }
            
            @Override
            public void onMessage(Map<String, Object> message) {
                Log.d("Widget", "Message received: " + message.toString());
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        if (!widgetView.onBackPressed()) {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        widgetView.destroy();
    }
}
```

## API Reference

### WidgetConfig

Configuration object for the widget:

```kotlin
data class WidgetConfig(
    val websiteToken: String,                    // Required: Your website token
    val locale: String = "en",                   // Optional: Widget locale
    val colorScheme: ColorScheme = ColorScheme.LIGHT, // Optional: Color theme
    val user: User? = null,                      // Optional: User information
    val customAttributes: Map<String, Any>? = null, // Optional: Custom attributes
    val baseUrl: String = "https://app.limechat.ai" // Optional: Custom base URL
)
```

#### User Information

```kotlin
data class User(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val identifierHash: String? = null
)
```

#### Color Schemes

```kotlin
enum class ColorScheme {
    LIGHT,  // Light theme
    DARK,   // Dark theme
    AUTO    // Auto theme (follows system)
}
```

### WidgetCallback

Interface for handling widget events:

```kotlin
interface WidgetCallback {
    fun onLoaded()                           // Widget finished loading
    fun onClose()                           // Widget closed
    fun onError(error: WidgetError)         // Error occurred
    fun onMessage(message: Map<String, Any>) // Message received
}
```

### LimechatWidgetView

Main widget view with the following methods:

```kotlin
class LimechatWidgetView : FrameLayout {
    
    // Initialize the widget
    fun init(config: WidgetConfig, callback: WidgetCallback? = null)
    
    // Send custom message to widget
    fun sendMessage(event: String, data: Map<String, Any> = emptyMap())
    
    // Handle back press (returns true if handled)
    fun onBackPressed(): Boolean
    
    // Attach custom file picker
    fun attachFilePicker(filePicker: WidgetFilePicker)
    
    // Clean up resources
    fun destroy()
}
```

### WidgetFilePicker

Helper class for file uploads:

```kotlin
class WidgetFilePicker(private val activity: FragmentActivity) {
    
    // Show file chooser (called automatically by WebView)
    fun showFileChooser(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams
    )
}
```

## File Upload Handling

The widget automatically handles file uploads using the modern `ActivityResultContracts` API. No additional permissions are required.

### Custom File Picker

If you need custom file picking logic:

```kotlin
val customFilePicker = WidgetFilePicker(this)
widgetView.attachFilePicker(customFilePicker)
```

## Security Features

The widget implements several security measures:

- **SafeBrowse Enabled**: Google Safe Browsing protection
- **Mixed Content Blocked**: Prevents loading insecure content
- **File Access Restricted**: Blocks local file system access
- **Custom User Agent**: Identifies widget requests
- **URL Validation**: Only loads allowed widget URLs

## Error Handling

The SDK provides comprehensive error handling with different error codes:

```kotlin
enum class ErrorCode {
    CONFIG_ERROR,        // Configuration issues
    WEBVIEW_ERROR,       // WebView loading errors
    NETWORK_ERROR,       // Network connectivity issues
    JAVASCRIPT_ERROR,    // JavaScript execution errors
    FILE_PICKER_ERROR,   // File upload errors
    UNKNOWN_ERROR        // Unclassified errors
}
```

Handle errors in your callback:

```kotlin
override fun onError(error: WidgetError) {
    when (error.code) {
        WidgetError.ErrorCode.CONFIG_ERROR -> {
            // Handle configuration errors
        }
        WidgetError.ErrorCode.NETWORK_ERROR -> {
            // Handle network errors
        }
        // ... handle other error types
    }
}
```

## Advanced Usage

### Custom Messaging

Send custom messages to the widget:

```kotlin
widgetView.sendMessage("custom-event", mapOf(
    "action" to "notify",
    "message" to "Hello from Android!",
    "timestamp" to System.currentTimeMillis()
))
```

### Handling Widget Messages

Receive and process messages from the widget:

```kotlin
override fun onMessage(message: Map<String, Any>) {
    val event = message["event"] as? String
    
    when (event) {
        "set-unread-count" -> {
            val count = message["count"] as? Int ?: 0
            updateUnreadBadge(count)
        }
        "custom-action" -> {
            val data = message["data"] as? Map<String, Any>
            handleCustomAction(data)
        }
    }
}
```

## Testing

The project includes comprehensive unit tests. Run them using:

```bash
./gradlew test
```

### Test Coverage

- ✅ Widget configuration validation
- ✅ URL building and parameter encoding
- ✅ Message parsing and processing
- ✅ Error handling scenarios
- ✅ File picker functionality

## Sample App

The repository includes a complete sample app demonstrating all features:

1. Clone the repository
2. Open in Android Studio
3. Run the `:sample-app` module
4. Configure your website token
5. Test all widget features

## ProGuard Configuration

The SDK includes consumer ProGuard rules that are automatically applied. No additional configuration is needed for release builds.

## Troubleshooting

### Common Issues

**Widget not loading:**
- Verify your website token is correct
- Check network connectivity
- Ensure the base URL is accessible

**File uploads not working:**
- Verify your activity extends `FragmentActivity`
- Check that the context passed to the widget is correct

**JavaScript errors:**
- Enable WebView debugging: `WebView.setWebContentsDebuggingEnabled(true)`
- Check browser console for errors

**Back navigation issues:**
- Ensure you're calling `widgetView.onBackPressed()` in your activity's `onBackPressed()`

### Enable Debugging

For development, enable WebView debugging:

```kotlin
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

## Migration Guide

### From React Native Widget

If migrating from the React Native widget:

| React Native | Android |
|--------------|---------|
| `websiteToken` | `websiteToken` |
| `user.name` | `user.name` |
| `user.email` | `user.email` |
| `user.phone_number` | `user.phoneNumber` |
| `user.identifier_hash` | `user.identifierHash` |
| `onWidgetLoad` | `onLoaded()` |
| `onWidgetClose` | `onClose()` |
| `onError` | `onError(error)` |

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:

- 📧 Email: support@limechat.ai  
- 📖 Documentation: https://docs.limechat.ai
- 🐛 Issues: https://github.com/limechat/widget-android/issues

---

Made with ❤️ by the Limechat team