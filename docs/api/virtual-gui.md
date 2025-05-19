# VirtualGui API Reference

Complete API reference for VirtualGui - breaks the 54-slot limit.

## Constructor

```java
public VirtualGui(String name, int virtualSize)
```

Creates a VirtualGui with more than 54 slots.

**Parameters:**
- `name` - Unique identifier
- `virtualSize` - Total virtual slots (can exceed 54)

## Virtual Slot Mapping

VirtualGui automatically creates multiple 54-slot pages and maps virtual slots to physical pages.

```
Virtual Slots: 0-107 (108 total)
Physical Pages: 2 pages of 54 slots each
```

## Navigation

### nextPage()
Go to next page.

### prevPage()
Go to previous page.

### goToPage(int page)
Go to specific page.

### getCurrentPage()
Get current page number.

### getTotalPages()
Get total number of pages.

## Item Methods

Same as AuroraGui, but slot numbers can exceed 54:

```java
virtualGui.addItem(80, item, handler);  // Maps to page 2, slot 26
```

## Auto-Navigation

VirtualGui automatically adds navigation buttons to move between pages.

## Example

```java
VirtualGui largeShop = new VirtualGui("large-shop", 162)  // 3 pages
    .title("&6Large Shop")
    .rows(6);

// Add items across all pages
for (int i = 0; i < 162; i++) {
    largeShop.addItem(i, shopItems.get(i), handler);
}

largeShop.register(manager);
largeShop.open(player);
```

See related guides:
- [Basic GUIs](../features/basic-guis.md)
- [API Reference](aurora-gui.md)
