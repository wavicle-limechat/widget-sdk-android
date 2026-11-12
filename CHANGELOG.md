# Changelog

All notable changes to the LimeChat Android SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0-beta.1]

> **Upgrade note: unverified contact update.** User details passed to the SDK reach the widget through `rnUser` with no verification. On every customer message the server updates the contact, and can merge into an existing contact with the same email or phone. If you use identity validation, pass `identifier` and the identifier hash (`identifierHash`) so the contact is verified.

### Changed
- `open(message)` now continues the stored conversation. In 0.0.9 every `open` reloaded without the stored token and started a new conversation.
- `onLoaded` fires once per page, when the widget is ready (its first `set-cw-conversation`), after `onConversationTokenChange`. It used to fire on every page finish, twice per load. A failed page gives `onError` and no `onLoaded`.
- `open` sends the message now if the widget is ready, with no reload. If not ready it holds the message, with no url parameter, and sends it at ready. An empty string or empty map does nothing. A held message stays held after a failed load. A message object with no `content` and other keys, such as `submitted_values`, gets `content: "Hi"`, like the web SDK's `setOpenMessage`. This also applies to `initialMessageData`.
- `set-user` is sent once per page, at ready, not on every page finish. Back press no longer resends an open message.
- `sendMessage` now posts `chatwoot-widget:` prefixed json, the prefix the widget reads. It was ignored before. Apostrophes and nested maps now arrive intact.
- Removed the `LimechatNative` web message listener and the injected `window.postMessage` wrappers. The widget calls `window.LimechatAndroid.postMessage` directly.
- `show_legacy_back_icon` is no longer added to the widget URL. `setShowLegacyBackIcon` no longer changes the url. `close-widget` always calls `onClose` when a callback is set, as in v0.0.9. Only with no callback and the legacy back icon on does it finish the host activity.
- The widget url is logged without its query string, so `website_token` and `custom_attributes` no longer reach logcat.
- `WidgetConfig.User` is now sent to the widget through `window.chatwootWebChannel.rnUser` on every page load. Hosts that already pass a user will update contacts with every message.
- A held `open` message is now sent when the widget posts `set-cw-conversation`, not on page finish. The page finish can come before the widget listens, which lost the message.
- Under external token management, the live conversation token is kept and used for reload and open, and is not overwritten by the config token.
- The `WidgetConfig.User` 5 arg constructor is back for java hosts.

### Added
- `platform=android` in the widget url, right after `website_token`.
- `setMessageAttributes` on `LimechatWidgetView` and `LimechatWidgetButton`. Needs a widget build containing `c8126fb7cd`, older builds ignore it. Values that are not json (a `Date`, an unknown object) return `false`.
- `LimechatWidgetView.reload()`.
- `LimechatWidgetView.reset()` and the static `LimechatWidgetView.reset(context, instanceId)`. `reset()` clears this widget's conversation, like the web SDK's `reset()`: it drops the stored or live conversation token and the user (and a token or user you passed in, until you pass new ones), drops held message attributes and a held open message, and reloads the page without a conversation if it is showing. It does not close the widget. Call it on logout. The static `reset(context, instanceId)` only clears stored state. if a view is showing, call its `reset()` instead, or the view keeps its live token and stores it again.
- `WidgetConfig.User.identifier`. When both `identifier` and `identifierHash` are set, a `set-user` message is sent. Nothing changes until a host sets both.
- `WidgetConfig.Builder.setHideFilePicker`, which adds `hide_file_picker=true` to the widget URL.

### Fixed
- A dead render process (API 26+) no longer kills the host app. The SDK replaces the WebView and reloads once. A second death before ready reports `Widget web process died again before ready` through `onError`.
- `onLoaded` still fires when the token callback throws.
- External token management accepts a token with no callback, or a callback with no token. `build()` used to throw.
- A main frame http error (status 400 or above) now reaches `onError` as a `WEBVIEW_ERROR` with `statusCode` in `context`.
- `widget-back` closes the widget again, as in 0.0.9.
- `UrlBuilder.buildWidgetUrl(config, cwConversation)` is callable from Java again.
- Embedded iframes can navigate. Only top-frame navigations are checked against the widget host.
- `init()` with a different user than the last one on the same instance (user A to user B, or user A to no user) drops the stored conversation token. The identity is `identifier`, `email` and phone. The last identity is kept in memory only, so a switch across an app restart is not caught: call `reset()` on logout.
- `init()` or `reload()` followed by `open(...)` no longer loads the page twice.
- If the page loads but the widget never becomes ready, `open` reloads after 10 seconds instead of holding forever.
- A conversation token posted by the old page after `reset()` or `reload()` is ignored. It used to be saved and could bring the old conversation back.
- The consumer ProGuard rule no longer keeps every class that has a `@JavascriptInterface` method; only those members are kept.

### Deprecated
- `WidgetConfig.colorScheme`, `WidgetConfig.ColorScheme` and `WidgetConfig.Builder.setColorScheme`. Ignored by the widget. Will be removed in the next major release. The url keeps `color_scheme` as before.
- `WidgetError.ErrorCode`: `CONFIG_ERROR`, `NETWORK_ERROR`, `FILE_PICKER_ERROR`, `INITIALIZATION_ERROR`, `UNKNOWN_ERROR` will be removed in the next major release.
- `WidgetConfigResponse.ConfigData`: every property except `widgetIconMobile` and `widgetIconDesktop` will be removed in the next major release.
- `LimechatWidgetButton.SDK_VERSION` and `getSDKVersion()` will be removed in the next major release.
- `UrlBuilder.buildWidgetUrl` parameters `openMessage` and `openPayload` will be removed in the next major release. The SDK no longer uses them.
- Unused by the SDK and kept for compatibility:
  - `WidgetConfig.User.hasIdentifyingInfo()` and `getPrimaryIdentifier()`, `WidgetConfig.TokenManagement`, `WidgetConfig.tokenManagement`, `WidgetConfig.isExternalTokenManagementValid`, `WidgetConfig.simple` and `WidgetConfig.withInstanceId` will be removed in the next major release.
  - `WidgetException` subclasses `NetworkError`, `JavaScriptError`, `WebViewError`, `FilePickerError`, `PersistenceError` and `UnknownError` will be removed in the next major release. They were never thrown. `ConfigurationError` stays.
  - `core.WidgetManager`, `core.WidgetLogger`, `core.WidgetResult`, `button.WidgetButtonRenderer`, `utils.WidgetUrlBuilder`, `webview.WidgetWebViewManager`, `models.WidgetMessage`, `models.WidgetState`, `exceptions.WidgetException` and `Result.handleWidgetError` will be removed in the next major release.

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

- **v0.2.0-beta.1** (unreleased): see the [0.2.0-beta.1] section above
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
