# Changelog

All notable changes to the LimeChat Android SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
