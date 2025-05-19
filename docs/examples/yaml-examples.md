# YAML Configuration Examples

Complete examples for configuring GUIs using YAML files.

## Basic Menu

```yaml
main-menu:
  title: "&6&lMain Menu"
  rows: 3
  items:
    10:
      material: GRASS_BLOCK
      name: "&aSpawn"
      lore:
        - "&7Click to teleport to spawn"
      command: "spawn"

    13:
      material: COMPASS
      name: "&bNavigate"
      lore:
        - "&7Open navigation menu"
      action: OPEN_GUI
      target-gui: "navigation"

    16:
      material: NETHER_STAR
      name: "&6Premium"
      lore:
        - "&7VIP exclusive content"
      permission: "server.vip"
```

## Shop with Categories

```yaml
shop-main:
  title: "&6&lShop"
  rows: 3
  border:
    type: FULL
    material: GRAY_STAINED_GLASS_PANE
    name: "&7"
  items:
    11:
      material: DIAMOND_SWORD
      name: "&cWeapons"
      lore:
        - "&7Browse weapon shop"
      action: OPEN_GUI
      target-gui: "shop-weapons"

    13:
      material: DIAMOND_CHESTPLATE
      name: "&9Armor"
      lore:
        - "&7Browse armor shop"
      action: OPEN_GUI
      target-gui: "shop-armor"

    15:
      material: GRASS_BLOCK
      name: "&aBlocks"
      lore:
        - "&7Browse block shop"
      action: OPEN_GUI
      target-gui: "shop-blocks"
```

## Weapon Shop

```yaml
shop-weapons:
  title: "&cWeapon Shop"
  rows: 6
  type: PACKET
  validation-level: PACKET
  border:
    type: FULL
    material: GRAY_STAINED_GLASS_PANE
    name: "&7"
  items:
    10:
      material: DIAMOND_SWORD
      name: "&cDiamond Sword"
      lore:
        - "&7Sharpness V"
        - "&7Unbreaking III"
        - ""
        - "&7Price: &6$500"
        - "&aLeft-click to purchase"
      enchantments:
        DAMAGE_ALL: 5
        DURABILITY: 3
      permission: "shop.weapons"
      click-type: LEFT
      command: "shop buy diamond_sword"

    11:
      material: DIAMOND_AXE
      name: "&cDiamond Axe"
      lore:
        - "&7Sharpness IV"
        - "&7Efficiency V"
        - ""
        - "&7Price: &6$450"
      enchantments:
        DAMAGE_ALL: 4
        DIG_SPEED: 5
      command: "shop buy diamond_axe"

    # Navigation
    45:
      material: ARROW
      name: "&cBack"
      action: OPEN_GUI
      target-gui: "shop-main"

    49:
      material: GOLD_INGOT
      name: "&6Balance: ${balance}"
      refresh-on-click: true
```

## Paginated Shop

```yaml
large-shop:
  title: "&6Large Shop - Page {page}"
  rows: 6
  paginated: true
  border:
    type: FULL
    material: GRAY_STAINED_GLASS_PANE
  items:
    # Items will auto-fill available slots
    # Navigation added automatically

    48:
      material: ARROW
      name: "&ePrevious Page"
      action: PREVIOUS_PAGE

    50:
      material: PAPER
      name: "&6Page {page}/{total_pages}"

    53:
      material: ARROW
      name: "&eNext Page"
      action: NEXT_PAGE
```

## VIP Menu

```yaml
vip-menu:
  title: "&6&lVIP Menu"
  rows: 4
  permission: "server.vip"
  border:
    type: CORNERS
    material: GOLD_BLOCK
    name: "&6"
  items:
    11:
      material: DIAMOND
      name: "&bDaily Reward"
      lore:
        - "&7Click to claim your"
        - "&7daily VIP reward!"
      command: "vip daily"
      cooldown: 86400000  # 24 hours

    13:
      material: NETHER_STAR
      name: "&5VIP Perks"
      lore:
        - "&7View your VIP benefits"
      action: OPEN_GUI
      target-gui: "vip-perks"

    15:
      material: CHEST
      name: "&6VIP Shop"
      lore:
        - "&7Exclusive VIP items"
      action: OPEN_GUI
      target-gui: "vip-shop"
```

## Confirmation Dialog

```yaml
confirm-purchase:
  title: "&cConfirm Purchase?"
  rows: 3
  items:
    11:
      material: GREEN_WOOL
      name: "&a&lCONFIRM"
      lore:
        - "&7Click to confirm purchase"
        - ""
        - "&cThis action cannot be undone!"
      command: "shop confirm"

    13:
      material: PAPER
      name: "&eItem: {item_name}"
      lore:
        - "&7Price: &6${price}"
        - "&7Balance: &6${balance}"

    15:
      material: RED_WOOL
      name: "&c&lCANCEL"
      action: CLOSE
```

## Admin Panel

```yaml
admin-panel:
  title: "&4&lAdmin Panel"
  rows: 5
  permission: "server.admin"
  border:
    type: FULL
    material: RED_STAINED_GLASS_PANE
    name: "&c"
  items:
    10:
      material: DIAMOND_SWORD
      name: "&cPlayer Management"
      lore:
        - "&7Manage players"
      action: OPEN_GUI
      target-gui: "admin-players"

    12:
      material: COMMAND_BLOCK
      name: "&6Server Management"
      lore:
        - "&7Server controls"
      action: OPEN_GUI
      target-gui: "admin-server"

    14:
      material: CHEST
      name: "&eInventory Inspector"
      command: "admin inspect"

    16:
      material: BOOK
      name: "&bLogs"
      action: OPEN_GUI
      target-gui: "admin-logs"

    22:
      material: BARRIER
      name: "&cShutdown"
      lore:
        - "&7Stop the server"
        - ""
        - "&4&lWARNING: This will stop the server!"
      command: "admin shutdown confirm"
      click-type: SHIFT
```

## Kit Selector

```yaml
kit-selector:
  title: "&6Select Your Kit"
  rows: 3
  items:
    11:
      material: DIAMOND_SWORD
      name: "&c&lWarrior Kit"
      lore:
        - "&7Strong melee combat"
        - "&7High health"
        - ""
        - "&aClick to select"
      enchantments:
        DAMAGE_ALL: 5
      command: "kit select warrior"

    13:
      material: BOW
      name: "&a&lArcher Kit"
      lore:
        - "&7Ranged combat"
        - "&7Speed boost"
        - ""
        - "&aClick to select"
      enchantments:
        ARROW_DAMAGE: 5
      command: "kit select archer"

    15:
      material: SHIELD
      name: "&9&lTank Kit"
      lore:
        - "&7Heavy armor"
        - "&7High defense"
        - ""
        - "&aClick to select"
      command: "kit select tank"
```

## Custom Model Items

```yaml
custom-shop:
  title: "&6Custom Item Shop"
  rows: 3
  items:
    11:
      material: DIAMOND
      custom-model-data: 1000001
      name: "&cRuby"
      lore:
        - "&7A precious red gem"
        - "&7Price: &6$500"
      command: "shop buy ruby"

    13:
      material: GOLD_NUGGET
      custom-model-data: 1000010
      name: "&6Gold Coin"
      lore:
        - "&7Currency item"
        - "&7Price: &6$50"
      command: "shop buy gold_coin"

    15:
      material: STICK
      custom-model-data: 1000020
      name: "&5Magic Wand"
      lore:
        - "&7Powerful magical artifact"
        - "&7Price: &6$1000"
      command: "shop buy magic_wand"
```

## Multi-Language Support

```yaml
menu-{lang}:
  # English version
  en:
    title: "&6Main Menu"
    items:
      13:
        name: "&bSettings"
        lore:
          - "&7Configure your preferences"

  # Spanish version
  es:
    title: "&6Menú Principal"
    items:
      13:
        name: "&bConfiguraci\u00f3n"
        lore:
          - "&7Configura tus preferencias"

  # French version
  fr:
    title: "&6Menu Principal"
    items:
      13:
        name: "&bParam\u00e8tres"
        lore:
          - "&7Configurez vos pr\u00e9f\u00e9rences"
```

## Dynamic Placeholders

```yaml
player-stats:
  title: "&6Stats: {player}"
  rows: 3
  items:
    11:
      material: EXPERIENCE_BOTTLE
      name: "&eLevel: {level}"
      lore:
        - "&7XP: {xp}/{xp_to_next}"

    13:
      material: PLAYER_HEAD
      owner: "{player}"
      name: "&e{player}"
      lore:
        - "&7Health: &c{health}/{max_health}"
        - "&7Hunger: &6{food}/20"
        - "&7World: &a{world}"

    15:
      material: GOLD_INGOT
      name: "&6Balance"
      lore:
        - "&7Money: &6${balance}"
```

## See Also

- [Config System Guide](../guides/config-system.md)
- [Code Examples](code-examples.md)
- [Complete Projects](complete-projects.md)
