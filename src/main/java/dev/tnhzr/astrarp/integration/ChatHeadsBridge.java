package dev.tnhzr.astrarp.integration;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * Server-side workaround for the client-side ChatHeads mod.
 *
 * <p>ChatHeads scans rendered chat messages for the sender's username and
 * draws their head when it finds a match. With FlectonePulse + AstraRP RP
 * names the visible name is the RP-name, so the mod has nothing to scan.
 * On top of that, FlectonePulse cancels {@link AsyncChatEvent} and re-emits
 * the message as a system message, completely bypassing the Paper
 * {@code ChatRenderer} pipeline — so wrapping the renderer at MONITOR did
 * nothing.</p>
 *
 * <p>Instead we mutate the chat <em>message</em> itself at HIGH priority,
 * appending a near-invisible " (&lt;username&gt;)" suffix to whatever the
 * player typed. FlectonePulse then renders the appended message verbatim
 * via its own {@code <message>} tag, the suffix shows up in the final
 * displayed text, and ChatHeads' heuristic detector finds the username
 * substring and draws the head. The default {@code <#1a1a1a>} colour is
 * dark enough to be unobtrusive against vanilla chat backgrounds.</p>
 *
 * <p>Configurable via {@code chatheads.enabled} and
 * {@code chatheads.suffix_format} in {@code config.yml}. Place
 * {@code {player}} inside the format to pick up the username.</p>
 */
public final class ChatHeadsBridge implements Listener {

    private final AstraRP plugin;

    public ChatHeadsBridge(AstraRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.configs().root().getBoolean("chatheads.enabled", true)) return;

        String format = plugin.configs().root().getString(
                "chatheads.suffix_format",
                " <#1a1a1a><i>({player})</i></#1a1a1a>");
        if (format == null || format.isEmpty()) return;

        Player source = event.getPlayer();
        Component suffix = Text.parse(format, Map.of("player", source.getName()));
        // Append the suffix to the message component itself. Anything that
        // reads event.message() afterwards (FlectonePulse, vanilla rendering,
        // other plugins) will see the appended content, so the username
        // survives into the final rendered chat line.
        event.message(event.message().append(suffix));
    }

    public static void register(AstraRP plugin) {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
        } catch (Throwable t) {
            plugin.getLogger().warning("ChatHeads bridge unavailable: " + t.getMessage());
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new ChatHeadsBridge(plugin), plugin);
    }
}
