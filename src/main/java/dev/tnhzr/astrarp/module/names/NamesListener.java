package dev.tnhzr.astrarp.module.names;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class NamesListener implements Listener {

    private final AstraRP plugin;
    private final NamesModule module;

    public NamesListener(AstraRP plugin, NamesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> module.applyDisplay(event.getPlayer()), 5L);
    }

    /**
     * Experimental selector translation: replace quoted RP-name occurrences inside
     * commands with the underlying real player name so that vanilla and other
     * plugins can resolve them as ordinary selectors.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        boolean enabled = plugin.configs().names().getBoolean("brigadier.enabled", true);
        if (!enabled) return;
        String message = event.getMessage();
        if (!message.contains("\"")) return;

        StringBuilder out = new StringBuilder(message.length());
        int i = 0;
        while (i < message.length()) {
            char c = message.charAt(i);
            if (c == '"') {
                int end = message.indexOf('"', i + 1);
                if (end == -1) {
                    out.append(message.substring(i));
                    break;
                }
                String quoted = message.substring(i + 1, end);
                java.util.Optional<UUID> matched = module.findByRpName(quoted);
                if (matched.isPresent()) {
                    Player resolved = Bukkit.getPlayer(matched.get());
                    if (resolved != null) {
                        out.append(resolved.getName());
                        i = end + 1;
                        continue;
                    }
                }
                out.append(message, i, end + 1);
                i = end + 1;
            } else {
                out.append(c);
                i++;
            }
        }
        String replaced = out.toString();
        if (!replaced.equals(message)) {
            event.setMessage(replaced);
        }
    }
}
