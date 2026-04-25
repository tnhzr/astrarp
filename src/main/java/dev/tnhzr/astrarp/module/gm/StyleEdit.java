package dev.tnhzr.astrarp.module.gm;

import java.util.regex.Pattern;

/**
 * MiniMessage-aware string edits used by the RPC editor's colour-and-format
 * buttons. These intentionally use very simple text manipulation so the user's
 * authored MM stays readable; round-tripping through Adventure would otherwise
 * lose comments, custom resolvers, and source-format choices.
 */
public final class StyleEdit {

    /** Matches a leading MM colour tag: <name>, <#rrggbb>, <gradient:..>. */
    private static final Pattern LEADING_COLOUR = Pattern.compile(
            "^<(?:#[0-9a-fA-F]{6}|gradient:[^>]+|rainbow(?::[^>]+)?|"
                    + "black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|grey|"
                    + "dark_gray|dark_grey|blue|green|aqua|red|light_purple|yellow|white)>");

    /** Matches every MM tag (open or close). Used by stripFormatting. */
    private static final Pattern ANY_TAG = Pattern.compile("</?[A-Za-z][A-Za-z0-9_:#-]*(?::[^>]*)?>");

    private StyleEdit() {}

    /**
     * Replace (or insert) the leading colour tag with {@code <colour>}.
     * Examples:
     *   "Иван"          + "red"  -> "<red>Иван"
     *   "<gold>Иван"    + "red"  -> "<red>Иван"
     *   "<#ffda4a>Иван" + "red"  -> "<red>Иван"
     */
    public static String applyColor(String input, String colour) {
        String s = input == null ? "" : input;
        s = LEADING_COLOUR.matcher(s).replaceFirst("");
        return "<" + colour + ">" + s;
    }

    /**
     * Toggle a simple decoration tag (e.g. b, i, u, st, obf). If the tag is
     * already present anywhere in the string we remove every open and close
     * instance; otherwise we wrap the whole string with it.
     */
    public static String toggleTag(String input, String tag) {
        String s = input == null ? "" : input;
        String openShort = "<" + tag + ">";
        String closeShort = "</" + tag + ">";
        // Long forms used by MiniMessage (b/bold, i/italic, u/underlined…).
        String longName = switch (tag) {
            case "b" -> "bold";
            case "i" -> "italic";
            case "u" -> "underlined";
            case "st" -> "strikethrough";
            case "obf" -> "obfuscated";
            default -> tag;
        };
        String openLong = "<" + longName + ">";
        String closeLong = "</" + longName + ">";

        boolean has = s.contains(openShort) || s.contains(openLong);
        if (has) {
            s = s.replace(openShort, "")
                    .replace(closeShort, "")
                    .replace(openLong, "")
                    .replace(closeLong, "");
            return s;
        }
        return openShort + s;
    }

    /**
     * Remove every MM tag, leaving plain text. Useful for the "сбросить
     * оформление" buttons in the editor.
     */
    public static String stripFormatting(String input) {
        if (input == null) return "";
        return ANY_TAG.matcher(input).replaceAll("").trim();
    }

    /**
     * Returns the index in {@code palette} of the leading colour tag in
     * {@code input}, or -1 if the input has no recognised leading colour or
     * uses something exotic (gradient, rainbow, hex). Used by the colour
     * cycler in the editor to advance to the next colour.
     */
    public static int currentColorIndex(String input, String[] palette) {
        if (input == null || palette == null) return -1;
        String s = input;
        if (!s.startsWith("<")) return -1;
        int close = s.indexOf('>');
        if (close < 0) return -1;
        String tag = s.substring(1, close);
        for (int i = 0; i < palette.length; i++) {
            if (palette[i].equals(tag)) return i;
        }
        return -1;
    }

    /**
     * Advance the leading colour to the next entry in the palette, wrapping
     * around. Direction +1 = next, -1 = previous. If the input has no
     * recognised leading colour, the result starts at index 0 (next) or the
     * last index (previous).
     */
    public static String cycleColor(String input, String[] palette, int direction) {
        if (palette == null || palette.length == 0) return input;
        int current = currentColorIndex(input, palette);
        int next;
        if (current < 0) {
            next = direction >= 0 ? 0 : palette.length - 1;
        } else {
            next = ((current + direction) % palette.length + palette.length) % palette.length;
        }
        return applyColor(input, palette[next]);
    }

    /** True when {@code input} carries the given short MM tag (or its long form). */
    public static boolean containsTag(String input, String tag) {
        if (input == null) return false;
        String low = input.toLowerCase();
        String longName = switch (tag) {
            case "b" -> "bold";
            case "i" -> "italic";
            case "u" -> "underlined";
            case "st" -> "strikethrough";
            case "obf" -> "obfuscated";
            default -> tag;
        };
        return low.contains("<" + tag + ">") || low.contains("<" + longName + ">");
    }

    /** Strip the leading colour tag, leaving formatting and content intact. */
    public static String clearColor(String input) {
        if (input == null) return "";
        return LEADING_COLOUR.matcher(input).replaceFirst("");
    }
}
