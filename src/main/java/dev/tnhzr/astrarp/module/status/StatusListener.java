package dev.tnhzr.astrarp.module.status;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class StatusListener implements Listener {

    private final AstraRP plugin;
    private final StatusModule module;

    public StatusListener(AstraRP plugin, StatusModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        // Default first-join state is NRP — players never see a "no status" state.
        if (!module.has(uuid)) {
            module.set(uuid, StatusModule.RpStatus.NRP);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> module.applyDisplay(event.getPlayer()), 5L);
    }
}
