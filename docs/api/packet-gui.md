# PacketGui API Reference

Complete API reference for PacketGui - extends AuroraGui with anti-dupe protection.

## Constructor

```java
public PacketGui(String name)
```

## Validation Methods

### validationLevel(ValidationLevel level)
Set validation level: BASIC, PACKET, or ADVANCED.

### config(PacketGuiConfig config)
Set custom configuration.

### onViolation(BiConsumer<Player, ExploitType> handler)
Set violation handler.

## Inherited from AuroraGui

All AuroraGui methods are available. See [AuroraGui API](aurora-gui.md).

## ExploitType Enum

- CLICK_DELAY (Severity: 2)
- CLICK_SPAM (Severity: 3)
- INVALID_SLOT (Severity: 3)
- CURSOR_DUPLICATION (Severity: 4)
- CURSOR_SWAP (Severity: 3)
- CLOSE_DESYNC (Severity: 3)
- NBT_INJECTION (Severity: 5)
- SHIFT_CLICK_LOOP (Severity: 4)
- TRANSACTION_MISMATCH (Severity: 4)
- NO_CLOSE_PACKET (Severity: 5)
- STALE_SESSION (Severity: 4)

## PacketGuiConfig

### Preset Configs
- `PacketGuiConfig.lenient()`
- `PacketGuiConfig.normal()`
- `PacketGuiConfig.strict()`

### Methods
- `validationLevel(ValidationLevel)`
- `minClickDelayMs(long)`
- `maxClicksPerSecond(int)`
- `sessionTimeoutMs(long)`
- `autoRollbackOnViolation(boolean)`
- `logViolations(boolean)`
- `kickOnViolation(boolean)`

See [Packet GUIs Guide](../features/packet-guis.md) for detailed usage.
