package dev.tnhzr.astrarp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;

public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    public static Component parse(String raw) {
        if (raw == null) return Component.empty();
        // Convert legacy ampersand colors to MiniMessage equivalents while keeping minimessage tags intact.
        Component legacy = LEGACY.deserialize(raw);
        String mm = MM.serialize(legacy);
        return MM.deserialize(mm);
    }

    public static Component parse(String raw, Map<String, String> placeholders) {
        String v = raw == null ? "" : raw;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            v = v.replace("{" + e.getKey() + "}", e.getValue());
        }
        return parse(v);
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String mmSerialize(Component component) {
        return MM.serialize(component);
    }
}
