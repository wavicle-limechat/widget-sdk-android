# Changelog

All notable changes to the LimeChat Android SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

> **Upgrade note: unverified contact update.** User details passed to the SDK reach the widget through `rnUser` with no verification. On the customer's first message the server updates the contact, and can merge into an existing contact with the same email or phone. If you use identity validation, pass `identifier` and the identifier hash (`identifierHash`) so the contact is verified.

### Changed
- `onLoaded` fires once per page, when the widget is ready (its first `set-cw-conversation`), after `onConversationTokenChange`. It used to fire on every page finish, twice per load. A failed page gives `onError` and no `onLoaded`.
- `open` sends the message now if the widget is ready, with no reload. If not ready it holds the message, with no url parameter, and sends it at ready. An empty string or empty map does nothing. A held message stays held after a failed load.
- `set-user` is sent once per page, at ready, not on every page finish. Back press no longer resends an open message.
- `sendMessage` now posts `chatwoot-widget:` prefixed json, the prefix the widget reads. It was ignored before. Apostrophes and nested maps now arrive intact.
- Removed the `LimechatNative` web message listener and the injected `window.postMessage` wrappers. The widget calls `window.LimechatAndroid.postMessage` directly.
- `show_legacy_back_icon` is no longer added to the widget URL. `setShowLegacyBackIcon` keeps its native close behaviour.
- The widget url is logged without its query string, so `website_token`, `custom_attributes` and `lc_open_*` no longer reach logcat.
- `%` in `lc_open_payload` is escaped so the payload is not dropped.
- `WidgetConfig.User` is now sent to the widget through `window.chatwootWebChannel.rnUser` on every page load. Hosts that already pass a user will update contacts on the first message.
- When a user is set, `open` messages are sent after `rnUser`, and only to the page that was loaded for them. A held message is dropped on re-init.
- A held `open` message is now sent when the widget posts `set-cw-conversation`, not on page finish. The page finish can come before the widget listens, which lost the message.
- Under external token management, the live conversation token is kept and used for reload and open, and is not overwritten by the config token.
- `%` in `lc_open_message` is escaped so the text is not dropped.
- The `WidgetConfig.User` 5 arg constructor is back for java hosts.

### Added
- `platform=android` in the widget url, right after `website_token`.
- `setMessageAttributes` on `LimechatWidgetView` and `LimechatWidgetButton`. Needs a widget build containing `c8126fb7cd`, older builds ignore it. Values that are not json (a `Date`, an unknown object) return `false`.
- `LimechatWidgetView.reload()`.
- `WidgetConfig.User.identifier`. When both `identifier` and `identifierHash` are set, a `set-user` message is sent. Nothing changes until a host sets both.
- `WidgetConfig.Builder.setHideFilePicker`, which adds `hide_file_picker=true` to the widget URL.

### Fixed
- A dead render process (API 26+) no longer kills the host app. The SDK replaces the WebView and reloads once. A second death before ready reports `Widget web process died again before ready` through `onError`.
- `onLoaded` still fires when the token callback throws.
- External token management accepts a token with no callback, or a callback with no token. `build()` used to throw.
- A main frame http error (status 400 or above) now reaches `onError` as a `WEBVIEW_ERROR` with `statusCode` in `context`.

### Deprecated
Removed at the next major.
- `WidgetError.ErrorCode`: `CONFIG_ERROR`, `NETWORK_ERROR`, `FILE_PICKER_ERROR`, `INITIALIZATION_ERROR`, `UNKNOWN_ERROR`.
- `WidgetConfigResponse.ConfigData`: every property except `widgetIconMobile` and `widgetIconDesktop`.
- `LimechatWidgetButton.SDK_VERSION` and `getSDKVersion()`.
- `UrlBuilder.buildWidgetUrl` parameters `openMessage` and `openPayload`. The SDK no longer uses them.

### Removed
- `WidgetException` subclasses `NetworkError`, `JavaScriptError`, `WebViewError`, `FilePickerError`, `PersistenceError` and `UnknownError`. They were never thrown. `ConfigurationError` stays.

## [v0.0.9] - 2024-12-19

### Added
- **Chat Persistence**: Automatic conversation persistence across app restarts and widget instances
  - Each widget instance maintains its own conversation state
  - Conversations persist even when the app is completely closed and reopened
  - Support for multiple widget instances with separate conversation histories
- **Instance ID Support**: New `setInstanceId()` method for widget isolation
  - Enables proper conversation persistence and isolation between different widget instances
  - Optional parameter that helps maintain separate conversations for different widget instances
- **Legacy Back Icon Support**: New `setShowLegacyBackIcon()` method
  - Controls the display of the legacy back icon in the widget
  - When enabled, automatically adds `show_legacy_back_icon=true` to the widget URL
  - Default value is `false`
- **Widget Back Event Handling**: Support for `widget-back` event
  - New event that behaves identically to the `close-widget` event
  - Triggers the `onClose()` callback when the back button is clicked
  - Provides consistent behavior for both minimize/close and back button interactions
- **Builder Pattern for WidgetConfig**: Improved configuration API
  - Replaced constructor-based configuration with builder pattern
  - Better type safety and validation
  - More intuitive API for optional parameters
- **External Token Management**: Advanced conversation token handling
  - Support for external token management with `setExternalTokenManagement()`
  - Callback-based token updates for custom storage solutions
  - Maintains backward compatibility with internal token management

### Changed
- **WidgetConfig API**: Migrated from constructor to builder pattern
  - `WidgetConfig(websiteToken, ...)` → `WidgetConfig.builder(websiteToken).setXxx().build()`
  - Improved validation and error handling
  - Better support for optional parameters
- **URL Building**: Enhanced URL parameter handling
  - Automatic inclusion of `show_legacy_back_icon` parameter when enabled
  - Improved parameter validation and encoding
- **Event Handling**: Enhanced widget event processing
  - Added support for `widget-back` event alongside existing `close-widget` event
  - Improved event routing and callback handling

### Improved
- **Documentation**: Comprehensive updates to all documentation files
  - Updated README.md with new features and examples
  - Enhanced CLIENT_INTEGRATION_GUIDE.md with detailed configuration options
  - Updated QUICK_REFERENCE.md with new feature examples
  - Added comprehensive examples for all new features
- **Code Quality**: Improved internal architecture
  - Better separation of concerns
  - Enhanced error handling and validation
  - Improved type safety throughout the SDK

### Technical Details
- **Storage**: Implemented dual-layer persistence system
  - In-memory cache for active widget instances
  - SharedPreferences for cross-app-restart persistence
  - Automatic cleanup and resource management
- **Widget Isolation**: Enhanced instance management
  - Each widget instance gets unique conversation storage
  - Proper cleanup on widget destruction
  - Support for multiple concurrent widget instances

### Migration Guide
For existing users upgrading from v0.0.8:

1. **Update WidgetConfig usage**:
   ```kotlin
   // Old way (v0.0.8)
   val config = WidgetConfig(
       websiteToken = "YOUR_TOKEN",
       user = WidgetConfig.User(name = "John", email = "john@example.com")
   )
   
   // New way (v0.0.9)
   val config = WidgetConfig.builder("YOUR_TOKEN")
       .setUser(WidgetConfig.User(name = "John", email = "john@example.com"))
       .build()
   ```

2. **Optional: Add instance ID for persistence**:
   ```kotlin
   val config = WidgetConfig.builder("YOUR_TOKEN")
       .setInstanceId("unique-widget-instance") // Optional but recommended
       .build()
   ```

3. **Optional: Enable legacy back icon**:
   ```kotlin
   val config = WidgetConfig.builder("YOUR_TOKEN")
       .setShowLegacyBackIcon(true) // Optional
       .build()
   ```

### Breaking Changes
- **WidgetConfig Constructor**: The direct constructor is now internal. Use the builder pattern instead.
- **API Changes**: All configuration methods now use the builder pattern for consistency.

### Dependencies
- No new external dependencies added
- Maintains compatibility with existing Android API levels (23+)

---

## [v0.0.8] - Previous Release

### Features
- Basic widget integration
- Floating widget button
- Full-screen chat widget
- File upload support
- Theme support (light/dark/auto)
- User management
- Custom attributes
- Event handling

### API
- Constructor-based WidgetConfig
- Basic callback system
- Standard widget events

---

## Version History

- **v0.0.9** (2024-12-19): Chat persistence, back button support, builder pattern
- **v0.0.8** (Previous): Basic widget functionality
- **v0.0.7** and earlier: Initial releases and bug fixes

---

## Support

For questions about upgrading or using new features:
- 📖 **Documentation**: [CLIENT_INTEGRATION_GUIDE.md](./CLIENT_INTEGRATION_GUIDE.md)
- 🚀 **Quick Start**: [QUICK_REFERENCE.md](./QUICK_REFERENCE.md)
- 💬 **Support**: Contact the LimeChat support team
- 📧 **Email**: support@limechat.ai
