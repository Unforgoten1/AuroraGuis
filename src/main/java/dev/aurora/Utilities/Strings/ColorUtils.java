package dev.aurora.Utilities.Strings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ColorUtils {
    public static String toNiceString(String string) {
        string = ChatColor.stripColor(string).replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.toCharArray().length; ++i) {
            char c = string.toCharArray()[i];
            if (i > 0) {
                char prev = string.toCharArray()[i - 1];
                if (!(prev != ' ' && prev != '[' && prev != '(' || i != string.toCharArray().length - 1 && c == 'x' && Character.isDigit(string.toCharArray()[i + 1]))) {
                    c = Character.toUpperCase(c);
                }
            } else if (c != 'x' || !Character.isDigit(string.toCharArray()[i + 1])) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }
    public static String strip(String string) {
        return ChatColor.stripColor(string);
    }
    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    public static int stringToInt(String string) {
        if (string == null)
            return -1;
        string = strip(string.replaceAll("[^0-9]+", ""));
        if (string.isEmpty())
            return -1;
        return tryParseInt(string);
    }
    public static int tryParseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception ex) {
            return -1;
        }
    }
    public static String format(String string, Object... arguments) {
        if (arguments != null && arguments.length > 0)
            for (int i = 0; i < arguments.length; i++) {
                Object argument = arguments[i];
                string = string.replace(String.format("{%d}", Integer.valueOf(i)), (argument == null) ? "" : argument.toString());
            }
        return string;
    }
    public static String flipString(String string) {
        StringBuilder input = new StringBuilder();
        input.append(string);
        input.reverse();
        return input.toString();
    }
    public static String color (String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String color (String string, Object ...arguments){
        return ColorUtils.color(ColorUtils.format(string, arguments));
    }

    public static List<String> color (List < String > strings) {
        ArrayList<String> toReturn = new ArrayList<String>();
        strings.forEach(str -> toReturn.add(ColorUtils.color(str)));
        return toReturn;
    }

    public static String[] splitByLength(String string) {
        ArrayList<String> result = new ArrayList<String>();
        int splitLength = 20;
        for (int i = 0; i < string.length(); i += splitLength) {
            int endIndex = Math.min(i + splitLength, string.length());
            result.add(string.substring(i, endIndex));
        }
        return result.toArray(new String[0]);
    }

    public static String[] splitByLength(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; ++i) {
            if (i > 0 && !strings[i - 1].endsWith(" ")) {
                sb.append(" ");
            }
            sb.append(strings[i]);
        }
        String combinedString = sb.toString();
        ArrayList<String> result = new ArrayList<String>();
        int splitLength = 35;
        int startIndex = 0;
        while (startIndex < combinedString.length()) {
            int endIndex = Math.min(startIndex + splitLength, combinedString.length());
            if (endIndex < combinedString.length() && !Character.isWhitespace(combinedString.charAt(endIndex - 1))) {
                while (endIndex > startIndex && !Character.isWhitespace(combinedString.charAt(endIndex - 1))) {
                    --endIndex;
                }
            }
            result.add(combinedString.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
        return result.toArray(new String[0]);
    }

    public static void sendCenteredMessage(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()) {
            if (c == '\u00a7') {
                previousCode = true;
                continue;
            }
            if (previousCode) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                }
                isBold = false;
                continue;
            }
            DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
            ++messagePxSize;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        StringBuilder sb = new StringBuilder();
        for (int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
            sb.append(" ");
        }
        player.sendMessage(sb + message);
    }

    public static List<String> formatLore(String lore) {
        List<String> messages = new ArrayList<>();
        StringBuilder nextString = new StringBuilder(ChatColor.WHITE.toString());
        for (String string : lore.split(" ")) {
            if (string.length() <= 40)
                if (nextString.length() + string.length() > 40) {
                    messages.add(nextString.toString());
                    nextString = new StringBuilder(ChatColor.WHITE + string + " ");
                } else {
                    nextString.append(ChatColor.WHITE).append(string).append(" ");
                }
        }
        messages.add(nextString.toString());
        return messages;
    }
    public static String getCenteredMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()) {
            if (c == '\u00a7') {
                previousCode = true;
                continue;
            }
            if (previousCode) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                }
                isBold = false;
                continue;
            }
            DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
            ++messagePxSize;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        StringBuilder sb = new StringBuilder();
        for (int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
            sb.append(" ");
        }
        return sb + message;
    }

    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(',', 1),
        DOUBLE_QUOTE('\"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public int getBoldLength() {
            if (this == SPACE) {
                return this.getLength();
            }
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() != c) continue;
                return dFI;
            }
            return DEFAULT;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }
    }
}


