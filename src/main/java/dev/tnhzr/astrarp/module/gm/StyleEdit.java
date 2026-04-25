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
}
