# AuroraGui API Reference

Complete API reference for the AuroraGui class.

## Constructor

```java
public AuroraGui(String name)
```

## Configuration Methods

### title(String title)
Sets the GUI title with color code support.

### rows(int rows)
Sets number of rows (1-6).

## Item Methods

### addItem(int slot, ItemStack item)
### addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler)
### addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler, ClickCondition condition)

### fill(ItemStack item)
### fill(int startSlot, int endSlot, ItemStack item)

## Border Methods

### setBorder(BorderType type, ItemStack item)

## Pagination Methods

### addPaginatedItems(List<ItemStack> items, Consumer<InventoryClickEvent> handler)
### nextPage()
### prevPage()
### setPage(int page)
### getCurrentPage()
### getTotalPages()

## Animation Methods

### addAnimation(int slot, Animation animation)
### stopAnimation(int slot)
### stopAllAnimations()

## Registration

### register(GuiManager manager)
### open(Player player)
### close(Player player)

See [Basic GUIs Guide](../features/basic-guis.md) for detailed usage.
