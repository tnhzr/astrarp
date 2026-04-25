package dev.tnhzr.astrarp.util;

import dev.tnhzr.astrarp.AstraRP;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Banner {

    private Banner() {}

    public static void print(AstraRP plugin) {
        try (InputStream in = plugin.getResource("banner.txt")) {
            if (in == null) return;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String enabledModules = String.join(", ", enabledModuleNames(plugin));
                if (enabledModules.isEmpty()) enabledModules = "—";
                String integrations = String.join(", ", activeIntegrations(plugin));
                if (integrations.isEmpty()) integrations = "—";

                Map<String, String> placeholders = Map.of(
                        "version", plugin.getDescription().getVersion(),
                        "api", "1.21.8",
                        "author", String.join(", ", plugin.getDescription().getAuthors()),
                        "modules", enabledModules,
                        "integrations", integrations
                );

                String line;
                List<Component> lines = new ArrayList<>();
                lines.add(Component.empty());
                while ((line = reader.readLine()) != null) {
                    lines.add(Text.parse(line, placeholders));
                }
                lines.add(Component.empty());
                for (Component c : lines) {
                    Bukkit.getServer().getConsoleSender().sendMessage(c);
                }
            }
        } catch (Exception ignored) {
            // Banner is cosmetic; never let it break startup.
        }
    }

    private static List<String> enabledModuleNames(AstraRP plugin) {
        List<String> names = new ArrayList<>();
        if (plugin.modules().isEnabled("status")) names.add("status");
        if (plugin.modules().isEnabled("names")) names.add("names");
        if (plugin.modules().isEnabled("keepinventory")) names.add("keepinv");
        if (plugin.modules().isEnabled("frames")) names.add("frames");
        if (plugin.modules().isEnabled("gm")) names.add("gm");
        return names;
    }

    private static List<String> activeIntegrations(AstraRP plugin) {
        List<String> out = new ArrayList<>();
        if (plugin.integrations().hasPlaceholderAPI()) out.add("PlaceholderAPI");
        if (plugin.integrations().hasLuckPerms()) out.add("LuckPerms");
        if (plugin.integrations().hasFlectonePulse()) out.add("FlectonePulse");
        if (plugin.integrations().hasTAB()) out.add("TAB");
        return out;
    }
}
