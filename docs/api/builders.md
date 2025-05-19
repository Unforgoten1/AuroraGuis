# Builders API Reference

API reference for GuiBuilder and specialized GUI builders.

## GuiBuilder

Factory class for creating common GUI patterns.

### Static Methods

```java
// Shop builder
public static GuiBuilder shop(GuiManager manager, String title)

// Confirmation dialog
public static GuiBuilder confirmation(GuiManager manager, String message, Runnable onConfirm, Runnable onCancel)

// Selector
public static GuiBuilder selector(GuiManager manager, String title, List<ItemStack> options)
```

### Fluent Methods

```java
public GuiBuilder item(int slot, ItemStack item, Consumer<InventoryClickEvent> handler)
public GuiBuilder border(BorderType type)
public GuiBuilder rows(int rows)
public IGui build()
```

### Example

```java
IGui shop = GuiBuilder.shop(manager, "&6Shop")
    .item(10, swordItem, this::handlePurchase)
    .item(11, axeItem, this::handlePurchase)
    .border(BorderType.FULL)
    .build();
```

## VirtualGuiBuilder

Builder for VirtualGuis.

```java
VirtualGuiBuilder builder = new VirtualGuiBuilder("large-shop", 162);

VirtualGui gui = builder
    .title("&6Large Shop")
    .rows(6)
    .addItems(shopItems)
    .build();
```

## Specialized Builders

### ConfirmationGui

```java
ConfirmationGui.show(player, manager, "Delete data?",
    () -> deleteData(),
    () -> cancel()
);
```

### SelectorGui

```java
SelectorGui selector = new SelectorGui(manager, "Select Kit")
    .addOption(warriorKit, p -> giveWarriorKit(p))
    .addOption(archerKit, p -> giveArcherKit(p))
    .addOption(tankKit, p -> giveTankKit(p));

selector.open(player);
```

### SearchableGui

```java
SearchableGui shop = new SearchableGui(manager, "Shop")
    .addSearchableItems(allItems)
    .setSearchHandler((query, items) -> {
        return items.stream()
            .filter(item -> item.getItemMeta().getDisplayName().contains(query))
            .collect(Collectors.toList());
    });

shop.open(player);
```

See [Shop Tutorial](../guides/shop-tutorial.md) for complete examples.
