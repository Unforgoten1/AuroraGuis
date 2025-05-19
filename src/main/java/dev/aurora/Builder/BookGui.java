package dev.aurora.Builder;

import dev.aurora.Compatibility.BookCompat;
import dev.aurora.Utilities.Strings.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates an interactive book GUI
 * Displays formatted text with clickable elements
 */
public class BookGui {
    private final String name;
    private String title;
    private String author;
    private final List<String> pages;
    private final Map<String, Consumer<Player>> clickActions;
    private static final Pattern CLICK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\{([^}]+)\\}");

    /**
     * Creates a new book GUI
     *
     * @param name The GUI identifier
     */
    public BookGui(String name) {
        this.name = name;
        this.title = "Book";
        this.author = "Server";
        this.pages = new ArrayList<>();
        this.clickActions = new HashMap<>();
    }

    /**
     * Sets the book title
     *
     * @param title The title
     * @return This book GUI for chaining
     */
    public BookGui title(String title) {
        this.title = ColorUtils.color(title);
        return this;
    }

    /**
     * Sets the book author
     *
     * @param author The author name
     * @return This book GUI for chaining
     */
    public BookGui author(String author) {
        this.author = ColorUtils.color(author);
        return this;
    }

    /**
     * Adds a page to the book
     *
     * @param content The page content (supports color codes)
     * @return This book GUI for chaining
     */
    public BookGui addPage(String content) {
        pages.add(ColorUtils.color(content));
        return this;
    }

    /**
     * Adds multiple pages at once
     *
     * @param pages The pages to add
     * @return This book GUI for chaining
     */
    public BookGui addPages(String... pages) {
        for (String page : pages) {
            addPage(page);
        }
        return this;
    }

    /**
     * Adds a page with clickable text
     * Format: [Display Text]{action_id}
     *
     * @param content The page content with clickable markers
     * @param actions Map of action_id to Consumer<Player>
     * @return This book GUI for chaining
     */
    public BookGui addClickablePage(String content, Map<String, Consumer<Player>> actions) {
        clickActions.putAll(actions);
        pages.add(ColorUtils.color(content));
        return this;
    }

    /**
     * Adds a clickable link on the current page
     *
     * @param text The text to display
     * @param actionId The action identifier
     * @param action The action to execute
     * @return This book GUI for chaining
     */
    public BookGui addClickableText(String text, String actionId, Consumer<Player> action) {
        clickActions.put(actionId, action);
        return this;
    }

    /**
     * Clears all pages
     *
     * @return This book GUI for chaining
     */
    public BookGui clearPages() {
        pages.clear();
        return this;
    }

    /**
     * Gets the number of pages
     *
     * @return Page count
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * Opens the book for a player
     *
     * @param player The player
     * @return true if successful
     */
    public boolean open(Player player) {
        if (pages.isEmpty()) {
            addPage("&7This book is empty.");
        }

        ItemStack book = BookCompat.createBook(title, author, pages);
        return BookCompat.openBook(player, book);
    }

    /**
     * Creates a help book
     *
     * @param title The book title
     * @return BookGui configured as help book
     */
    public static BookGui help(String title) {
        return new BookGui("help")
                .title(title)
                .author("Server Help");
    }

    /**
     * Creates a tutorial book
     *
     * @param title The book title
     * @return BookGui configured as tutorial
     */
    public static BookGui tutorial(String title) {
        return new BookGui("tutorial")
                .title(title)
                .author("Tutorial");
    }

    /**
     * Creates a story/lore book
     *
     * @param title The book title
     * @param author The story author
     * @return BookGui configured as story book
     */
    public static BookGui story(String title, String author) {
        return new BookGui("story")
                .title(title)
                .author(author);
    }

    /**
     * Creates a rules book
     *
     * @return BookGui configured for server rules
     */
    public static BookGui rules() {
        return new BookGui("rules")
                .title("&cServer Rules")
                .author("Server Staff");
    }

    /**
     * Creates an info/documentation book
     *
     * @param topic The topic name
     * @return BookGui configured for documentation
     */
    public static BookGui info(String topic) {
        return new BookGui("info")
                .title("&bInfo: " + topic)
                .author("Documentation");
    }

    /**
     * Processes clickable text in pages
     * Note: Full click handling requires JSON text components
     * This is a simplified version
     */
    private String processClickableText(String content) {
        Matcher matcher = CLICK_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String displayText = matcher.group(1);
            String actionId = matcher.group(2);

            // Replace with colored clickable-looking text
            // Full implementation would use JSON text components
            matcher.appendReplacement(result, "&n&b" + displayText + "&r");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Builder-style creation
     *
     * @param name The GUI name
     * @return New BookGui
     */
    public static BookGui create(String name) {
        return new BookGui(name);
    }

    @Override
    public String toString() {
        return "BookGui{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", pages=" + pages.size() +
                '}';
    }
}
