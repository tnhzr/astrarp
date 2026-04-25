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
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> module.applyDisplay(event.getPlayer()), 5L);
    }
}
