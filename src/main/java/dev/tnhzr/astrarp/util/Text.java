package dev.tnhzr.astrarp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;
import java.util.regex.Pattern;

public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Matches both the {@code &}-prefixed and the {@code §}-prefixed legacy
     * color codes so that they can be translated into MiniMessage tags before
     * rendering. The MiniMessage serializer escapes raw {@code <} characters,
     * so converting legacy codes ahead of deserialization is the safe path.
     */
    private static final Pattern LEGACY_PATTERN =
            Pattern.compile("(?i)[&\u00A7]([0-9A-FK-OR])");

    private Text() {}

    public static Component parse(String raw) {
        if (raw == null) return Component.empty();
        return MM.deserialize(legacyToMiniMessage(raw));
    }

    public static Component parse(String raw, Map<String, String> placeholders) {
        String v = raw == null ? "" : raw;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String value = e.getValue() == null ? "" : e.getValue();
                v = v.replace("{" + e.getKey() + "}", value);
            }
        }
        return parse(v);
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String mmSerialize(Component component) {
        return MM.serialize(component);
    }

    private static String legacyToMiniMessage(String input) {
        java.util.regex.Matcher m = LEGACY_PATTERN.matcher(input);
        if (!m.find()) return input;
        StringBuffer sb = new StringBuffer(input.length());
        m.reset();
        while (m.find()) {
            String tag = legacyTagFor(Character.toLowerCase(m.group(1).charAt(0)));
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(tag));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String legacyTagFor(char code) {
        return switch (code) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> "";
        };
    }
}
