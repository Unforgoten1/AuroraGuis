# Complete Project Examples

Full, production-ready implementations you can use as templates.

## 1. Multi-Category Shop System

A complete shop system with categories, pagination, and security.

### Features
- Multiple shop categories (Blocks, Tools, Weapons, Armor)
- Paginated item listings
- Anti-dupe protection with PacketGui
- Economy integration (Vault)
- Purchase confirmation for expensive items
- Transaction logging
- Admin shop editor

### Implementation
See the complete tutorial: [Shop Tutorial](../guides/shop-tutorial.md)

### GitHub Repository
```
https://github.com/YourOrg/AuroraShop
```

---

## 2. Player Bank Vault

Secure personal storage with maximum security.

### Features
- Per-player storage (up to 54 items)
- Maximum security (ADVANCED validation)
- Persistent storage (MySQL/SQLite)
- Violation logging and alerts
- Admin inspection tools
- Automatic backups

### Core Code

```java
public class BankVault {
    private final JavaPlugin plugin;
    private final GuiManager manager;
    private final Database database;

    public void openVault(Player player) {
        // Load async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<ItemStack> items = database.loadVault(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                PacketGui vault = new PacketGui("vault-" + player.getUniqueId())
                    .title("&6Vault: " + player.getName())
                    .rows(6)
                    .validationLevel(ValidationLevel.ADVANCED)
                    .config(PacketGuiConfig.strict()
                        .sessionTimeoutMs(180000)
                        .violationKickThreshold(3))
                    .onViolation((p, type) -> handleViolation(p, type))
                    .register(manager);

                // Add items
                for (int i = 0; i < items.size(); i++) {
                    vault.addItem(i, items.get(i), e -> handleItemMove(e));
                }

                vault.addListener(new GuiListener() {
                    @Override
                    public void onClose(Player p, AuroraGui gui) {
                        saveVault(p, gui);
                    }
                });

                vault.open(player);
            });
        });
    }
}
```

---

## 3. Server Selector

Cross-server navigation GUI with BungeeCord/Velocity support.

### Features
- Multi-server navigation
- Real-time player counts
- Server status indicators
- VIP-only servers
- Animated server icons

### Core Code

```java
public class ServerSelector {
    private final GuiManager manager;

    public void open(Player player) {
        AuroraGui selector = new AuroraGui("server-selector")
            .title("&6&lServer Selector")
            .rows(3)
            .setBorder(BorderType.FULL, borderItem);

        // Add servers
        addServer(selector, 11, "Lobby", "lobby", Material.EMERALD_BLOCK);
        addServer(selector, 12, "Survival", "survival", Material.GRASS_BLOCK);
        addServer(selector, 13, "Creative", "creative", Material.COMMAND_BLOCK);
        addServer(selector, 14, "SkyBlock", "skyblock", Material.END_STONE);
        addServer(selector, 15, "MiniGames", "minigames", Material.DIAMOND_SWORD);

        // Add animations
        selector.addAnimation(4, new PulsingGlow(starItem, 20));

        selector.register(manager);
        selector.open(player);
    }

    private void addServer(AuroraGui gui, int slot, String name,
                          String serverName, Material icon) {
        int playerCount = getPlayerCount(serverName);

        ItemStack item = new ItemBuilder(icon)
            .name("&e&l" + name)
            .lore(
                "&7Players: &f" + playerCount,
                "",
                "&aClick to connect!"
            )
            .glow()
            .build();

        gui.addItem(slot, item, event -> {
            Player p = (Player) event.getWhoClicked();
            connectToServer(p, serverName);
        });
    }
}
```

---

## 4. Quest GUI

Complete quest tracking and management system.

### Features
- Active quest tracking
- Quest categories (Daily, Weekly, Achievements)
- Progress tracking
- Reward claiming
- Quest descriptions with objectives

### Core Code

```java
public class QuestGUI {
    public void openQuestMenu(Player player) {
        AuroraGui menu = new AuroraGui("quest-menu")
            .title("&6&lQuests")
            .rows(4);

        // Daily quests
        menu.addItem(11, createQuestCategory("Daily Quests"),
            e -> openQuestCategory(player, QuestCategory.DAILY));

        // Weekly quests
        menu.addItem(13, createQuestCategory("Weekly Quests"),
            e -> openQuestCategory(player, QuestCategory.WEEKLY));

        // Achievements
        menu.addItem(15, createQuestCategory("Achievements"),
            e -> openQuestCategory(player, QuestCategory.ACHIEVEMENTS));

        menu.register(manager);
        menu.open(player);
    }

    public void openQuestDetails(Player player, Quest quest) {
        AuroraGui details = new AuroraGui("quest-" + quest.getId())
            .title("&6" + quest.getName())
            .rows(5);

        // Quest info
        details.addItem(13, createQuestInfo(quest), e -> {});

        // Objectives
        List<QuestObjective> objectives = quest.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            details.addItem(19 + i, createObjectiveItem(objectives.get(i)), e -> {});
        }

        // Claim button
        if (quest.isComplete(player)) {
            details.addItem(40, createClaimButton(),
                e -> claimReward(player, quest));
        }

        details.register(manager);
        details.open(player);
    }
}
```

---

## 5. Trading System

Secure player-to-player trading.

### Features
- Dual-panel trading interface
- Ready confirmation system
- Anti-dupe protection
- Trade history logging
- Admin trade monitoring

### Core Code

```java
public class TradingSystem {
    public void openTrade(Player player1, Player player2) {
        TradeSession session = new TradeSession(player1, player2);

        PacketGui tradeGui = new PacketGui("trade-" + session.getId())
            .title("&eTrade: " + player1.getName() + " <-> " + player2.getName())
            .rows(6)
            .validationLevel(ValidationLevel.ADVANCED)
            .config(PacketGuiConfig.strict());

        // Player 1 area (left side)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int slot = i * 9 + j;
                tradeGui.addItem(slot, null, e -> handleTradeSlot(session, 1, e));
            }
        }

        // Player 2 area (right side)
        for (int i = 0; i < 4; i++) {
            for (int j = 5; j < 9; j++) {
                int slot = i * 9 + j;
                tradeGui.addItem(slot, null, e -> handleTradeSlot(session, 2, e));
            }
        }

        // Divider
        ItemStack divider = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("&7")
            .build();
        for (int i = 0; i < 6; i++) {
            tradeGui.setItem(i * 9 + 4, divider);
        }

        // Accept buttons
        tradeGui.addItem(48, createAcceptButton(false),
            e -> handleAccept(session, player1));
        tradeGui.addItem(50, createAcceptButton(false),
            e -> handleAccept(session, player2));

        tradeGui.register(manager);
        tradeGui.open(player1);
        tradeGui.open(player2);
    }
}
```

---

## 6. Admin Panel

Comprehensive server management GUI.

### Features
- Player management (kick, ban, mute)
- Server controls (restart, backup)
- Plugin management
- World management
- Permission management
- Live logs viewer

### Repository
```
https://github.com/YourOrg/AuroraAdmin
```

---

## 7. Cosmetics Menu

Player cosmetics and customization.

### Features
- Particle effects
- Pet selection
- Hat wardrobe
- Gadgets
- Trail effects
- Preview system
- Purchase system

---

## 8. Auction House

Global marketplace for players.

### Features
- List items for sale
- Browse listings
- Search and filter
- Bidding system
- Transaction fees
- Expired listing collection
- Anti-dupe protection

---

## 9. Skill Tree

RPG-style skill progression.

### Features
- Visual skill tree
- Unlock requirements
- Skill descriptions
- Point allocation
- Skill reset option
- Multiple specializations

---

## 10. Minigame Lobby

Minigame selection and queue system.

### Features
- Game selection
- Queue management
- Party system
- Leaderboards
- Statistics
- Map voting

---

## Getting Started with Templates

1. **Clone the repository**
   ```bash
   git clone https://github.com/YourOrg/AuroraTemplate
   ```

2. **Configure your plugin**
   - Update plugin.yml
   - Configure config.yml
   - Set up database connection

3. **Customize GUIs**
   - Modify YAML files in `guis/`
   - Adjust messages in `messages.yml`
   - Configure permissions

4. **Test thoroughly**
   - Test all features
   - Verify security
   - Check performance

5. **Deploy**
   - Build with Maven
   - Upload to server
   - Monitor logs

## Support

Need help with implementation? Join our Discord or check the guides:

- [Shop Tutorial](../guides/shop-tutorial.md)
- [Anti-Dupe Guide](../guides/anti-dupe.md)
- [Performance Guide](../guides/performance.md)

---

**Contribute your projects!** Share your AuroraGuis implementations with the community.
