# Changelog

All notable changes to AuroraGuis will be documented in this file.

## [1.0.3] - Packet GUI Release - 2026-02-09

### 🛡️ NEW: Packet-Based GUIs with Anti-Duplication Protection

This release adds comprehensive packet-level GUI validation to prevent item duplication exploits.

### Added - Packet GUI System

#### Core Components
- **PacketGui** (`Packet/Core/PacketGui.java`) - Packet-based GUI implementation
- **IGui Interface** (`GUI/IGui.java`) - Common interface for AuroraGui and PacketGui
- **PacketGuiRegistry** (`Packet/Core/PacketGuiRegistry.java`) - Active packet GUI tracking
- **PacketEventManager** (`Packet/Core/PacketEventManager.java`) - PacketEvents lifecycle management

#### Validation System
- **11 Exploit Type Protections:**
  1. **CLICK_DELAY** - Prevents rapid clicking (< 50ms)
  2. **CLICK_SPAM** - Rate limiting (max 20 clicks/second)
  3. **INVALID_SLOT** - Out-of-bounds slot access prevention
  4. **CURSOR_DUPLICATION** - Detects cursor amount increases
  5. **CURSOR_SWAP** - Detects cursor type changes
  6. **CLOSE_DESYNC** - Validates inventory state on close
  7. **NBT_INJECTION** - Detects NBT tampering via SHA-256 hashing
  8. **SHIFT_CLICK_LOOP** - Prevents shift-click loop exploits
  9. **TRANSACTION_MISMATCH** - Post-click state verification
  10. **NO_CLOSE_PACKET** - Withhold-close-packet detection
  11. **STALE_SESSION** - Session timeout detection

#### Validation Components
- **AntiDupeValidator** (`Packet/Validation/AntiDupeValidator.java`) - Master validation coordinator
- **ServerSideInventory** (`Packet/Validation/ServerSideInventory.java`) - Server-side truth tracking
- **ClickValidator** (`Packet/Validation/ClickValidator.java`) - Timing and rate validation
- **CursorTracker** (`Packet/Validation/CursorTracker.java`) - Cursor state validation
- **TransactionValidator** (`Packet/Validation/TransactionValidator.java`) - Transaction verification
- **ItemFingerprint** (`Packet/Validation/ItemFingerprint.java`) - SHA-256 NBT hashing
- **ViolationLogger** (`Packet/Validation/ViolationLogger.java`) - Exploit attempt logging

#### Packet Handlers
- **ClickPacketHandler** (`Packet/Handler/ClickPacketHandler.java`) - WINDOW_CLICK packet interception
- **ClosePacketHandler** (`Packet/Handler/ClosePacketHandler.java`) - CLOSE_WINDOW packet interception

#### Configuration
- **ValidationLevel Enum** (`Packet/API/ValidationLevel.java`)
  - BASIC (0ms overhead) - Bukkit events only
  - PACKET (1-2ms) - Timing + cursor validation
  - ADVANCED (3-5ms) - Full protection + fingerprinting
- **PacketGuiConfig** (`Packet/API/PacketGuiConfig.java`)
  - 3 presets: lenient(), normal(), strict()
  - Fully customizable validation parameters
  - Session monitoring configuration
  - Violation handling options

### Modified - Core Integration

- **GuiManager.java** - Added packet support with `enablePacketSupport()` method
- **GuiBuilder.java** - Added `.packetMode(ValidationLevel)` method for easy packet GUI creation
- **pom.xml** - Added PacketEvents 2.6.0 dependency with proper shading

### Documentation

- **README.md** - Complete rewrite with packet GUI information
- **PACKET_GUI_GUIDE.md** - Comprehensive 650+ line packet GUI guide
- **EXAMPLES.md** - Complete rewrite with 1000+ lines of practical examples
  - Basic GUI examples
  - Advanced features (pagination, animations)
  - Packet GUI examples (secure shop, bank vault, trading)
  - Complete systems (full shop implementation)

### Performance

- **BASIC level:** 0ms overhead
- **PACKET level:** ~1-2ms overhead per click
- **ADVANCED level:** ~3-5ms overhead per click
- Memory usage: ~5-10 KB per active packet GUI
- Tested with 50+ concurrent packet GUIs

### Backward Compatibility

- ✅ 100% backward compatible
- ✅ Existing AuroraGui code works unchanged
- ✅ PacketEvents only loaded if packet support enabled
- ✅ All existing features preserved

### Dependencies

- **Added:** PacketEvents 2.6.0 (automatically shaded)
- **Repository:** CodeMC (maven-snapshots and maven-releases)

### Migration Notes

Migrating from AuroraGui to PacketGui is simple:

```java
// Before (event-based)
AuroraGui gui = new AuroraGui("shop")
    .title("Shop")
    .addItem(13, item, handler);

// After (packet-based with anti-dupe)
PacketGui gui = new PacketGui("shop")
    .title("Shop")
    .validationLevel(ValidationLevel.PACKET)  // Only addition!
    .addItem(13, item, handler);
```

---

## [2.0.0] - Major Release - 2026-02-08

### 🎉 Complete Feature Overhaul - All 13 Planned Enhancements Implemented

This major release transformed AuroraGuis into a best-in-class Minecraft GUI library with comprehensive testing, advanced features, persistence, analytics, and performance optimizations.

### Added - Testing Infrastructure
- **Unit Testing Framework** with JUnit 5, Mockito, and MockBukkit
  - `TestBase` class for all tests with pre-configured mocks
  - 70%+ test coverage for core classes
  - Maven Surefire plugin for CI/CD integration
  - `TESTING.md` comprehensive testing guide
  - 11+ test files covering all major features

### Added - Advanced Input Methods
- **AnvilInputGui** (`Builder/AnvilInputGui.java`)
  - Text input via anvil rename mechanic
  - Placeholder text support
  - Input validators
  - Completion callbacks
- **SignInputGui** (`Builder/SignInputGui.java`)
  - Multi-line text input
  - Per-line validators
  - Async sign editing (no visual glitches)

### Added - Advanced Navigation
- **GuiNavigator** (`Manager/GuiNavigator.java`)
  - History-based navigation (back/forward)
  - Breadcrumb trail
  - Automatic back button management
- **NavigationBuilder** (`Builder/NavigationBuilder.java`)
  - Fluent navigation chain creation

### Added - Conditional Rendering
- **ClickCondition** enhancements (`Struct/Condition/ClickCondition.java`)
  - Permission checks
  - Item requirements
  - Custom predicates
  - Combinators (and, or, negate)
- **ConditionalItem** (`Struct/Condition/ConditionalItem.java`)
  - Show/hide items based on conditions
  - Dynamic item updates

### Added - Click Cooldowns
- **ClickCooldown** (`Struct/Cooldown/ClickCooldown.java`)
  - Per-player cooldowns
  - Per-slot cooldowns
  - Global cooldowns
  - Remaining time queries
- **CooldownManager** (`Manager/CooldownManager.java`)
  - Centralized cooldown management

### Added - Action System
- **IGuiAction** interface (`Struct/Action/IGuiAction.java`)
- **CommandAction** (`Struct/Action/CommandAction.java`)
- **MessageAction** (`Struct/Action/MessageAction.java`)
- **CloseAction** (`Struct/Action/CloseAction.java`)
- **CompositeAction** (`Struct/Action/CompositeAction.java`)

### Added - Theme System
- **GuiTheme** (`Struct/Theme/GuiTheme.java`)
  - Reusable visual styles
  - Color schemes
  - Border patterns
- **ThemeRegistry** (`Struct/Theme/ThemeRegistry.java`)
  - Built-in themes: modern, classic, neon
- **ThemeBuilder** (`Struct/Theme/ThemeBuilder.java`)

### Added - Item Sorting
- **GuiSorter** (`Struct/Sort/GuiSorter.java`)
  - Sort by: material, amount, name, custom
  - Ascending/descending
  - Drag-to-sort support

### Added - Drag and Drop
- **DragZone** (`Struct/DragZone.java`)
  - Define allowed drag zones
  - Slot-to-slot validation
- **DragValidator** (`Struct/DragValidator.java`)

### Added - GUI Locking
- **GuiLock** (`Struct/GuiLock.java`)
  - Prevent modifications
  - Admin override
  - Automatic unlock on close

### Added - Multi-Viewer Support
- **ViewerManager** (`Struct/ViewerManager.java`)
  - Track all viewers of a GUI
  - Synchronized updates
  - Per-viewer permissions

### Added - Persistence
- **GuiState** (`Struct/Persistence/GuiState.java`)
  - Save GUI contents
  - Restore on reload
- **GuiSerializer** (`Serialization/GuiSerializer.java`)
  - JSON serialization
  - Kryo binary serialization (fast)
- **PersistenceManager** (`Manager/PersistenceManager.java`)

### Added - Configuration
- **GuiConfig** (`Config/GuiConfig.java`)
  - Per-GUI settings
  - Global defaults
- **ConfigLoader** (`Config/ConfigLoader.java`)
  - YAML configuration

### Added - Metrics & Analytics
- **GuiMetrics** (`Metrics/GuiMetrics.java`)
  - Track opens, clicks, closes
  - Average session time
  - Popular items
- **MetricsCollector** (`Metrics/MetricsCollector.java`)
- **MetricsExporter** (`Metrics/MetricsExporter.java`)

### Added - Async Operations
- **AsyncGuiLoader** (`Async/AsyncGuiLoader.java`)
  - Non-blocking GUI creation
  - Database queries
  - API calls
- **AsyncItemLoader** (`Async/AsyncItemLoader.java`)

### Added - Integration APIs
- **PlaceholderAPI Support** (`Integration/PlaceholderAPIHook.java`)
  - Auto-detect PlaceholderAPI
  - Parse placeholders in titles/lore
- **Vault Economy Support** (`Integration/VaultEconomyHook.java`)
- **WorldGuard Support** (`Integration/WorldGuardHook.java`)

### Added - Debug Utilities
- **GuiDebugger** (`Debug/GuiDebugger.java`)
  - Verbose logging
  - Performance metrics
  - Event tracing
- **GuiInspector** (`Debug/GuiInspector.java`)
  - Runtime GUI inspection

### Performance Improvements
- Centralized animation scheduler (O(1) overhead)
- Lazy inventory creation
- Concurrent collections for thread safety
- Efficient slot updates

### Documentation
- `TESTING.md` - Testing guide
- `EXAMPLES.md` - Comprehensive examples
- `COMPATIBILITY.md` - Version compatibility
- `SUGGESTIONS.md` - Future improvements

---

## [1.0.0] - Initial Release - 2025-12-15

### Core Features
- Basic GUI creation with rows and titles
- Item click handling
- Border decoration
- Simple animations
- Pagination support
- Template system
- ItemBuilder utility
- Color formatting
