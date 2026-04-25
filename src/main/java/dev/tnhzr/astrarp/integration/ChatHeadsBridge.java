package dev.tnhzr.astrarp.integration;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Text;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * Server-side hook for the client-side ChatHeads mod.
 *
 * <p>ChatHeads scans rendered chat messages for the sender's username and
 * draws their head when it finds a match. With FlectonePulse / RP-name
 * setups, the visible name is replaced with the RP-name, so the mod has
 * nothing to scan and never paints the head. To work around this we wrap
 * whatever renderer FlectonePulse (or the default Paper renderer) installs
 * and append a near-invisible "(<username>)" suffix at MONITOR priority.
 * The text is dark enough to blend into the chat background but is still
 * picked up by ChatHeads' heuristic detection.</p>
 *
 * <p>Configurable via {@code chatheads.enabled} and
 * {@code chatheads.suffix_format} in {@code config.yml}.</p>
 */
public final class ChatHeadsBridge implements Listener {

    private final AstraRP plugin;

    public ChatHeadsBridge(AstraRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.configs().root().getBoolean("chatheads.enabled", true)) return;

        String format = plugin.configs().root().getString(
                "chatheads.suffix_format",
                " <#1a1a1a><i>({player})</i></#1a1a1a>");
        if (format == null || format.isEmpty()) return;

        ChatRenderer previous = event.renderer();
        Player source = event.getPlayer();
        String username = source.getName();
        Component suffix = Text.parse(format, Map.of("player", username));

        event.renderer((src, sourceDisplayName, message, viewer) -> {
            Component base;
            try {
                base = previous.render(src, sourceDisplayName, message, viewer);
            } catch (Throwable ignored) {
                base = Component.empty().append(sourceDisplayName).append(Component.text(": ")).append(message);
            }
            return base.append(suffix);
        });
    }

    public static void register(AstraRP plugin) {
        try {
            // Sanity check that Paper's chat API is available before we register.
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
        } catch (Throwable t) {
            plugin.getLogger().warning("ChatHeads bridge unavailable: " + t.getMessage());
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new ChatHeadsBridge(plugin), plugin);
    }
}
