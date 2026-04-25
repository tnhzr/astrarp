package dev.tnhzr.astrarp.module.keepinv;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.AstraModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public final class KeepInvModule implements AstraModule, Listener {

    private final AstraRP plugin;

    public KeepInvModule(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public String id() { return "keepinventory"; }
    @Override public String defaultConfigName() { return "modules/keepinventory.yml"; }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent event) {
        boolean pvpOnly = plugin.configs().keepInv().getBoolean("pvp_only", true);
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (pvpOnly && killer == null) {
            return; // PvE — vanilla behaviour.
        }

        StatusModule status = plugin.status();
        if (status == null) return;

        StatusModule.RpStatus victimStatus = status.get(victim.getUniqueId());
        StatusModule.RpStatus killerStatus = killer != null ? status.get(killer.getUniqueId()) : StatusModule.RpStatus.NONE;

        boolean keep = decide(victimStatus, killerStatus);

        if (keep) {
            event.setKeepInventory(true);
            event.setKeepLevel(plugin.configs().keepInv().getBoolean("keep_xp", true));
            event.getDrops().clear();
            if (plugin.configs().keepInv().getBoolean("keep_xp", true)) {
                event.setDroppedExp(0);
            }
            if (plugin.configs().keepInv().getBoolean("notify_victim", true)) {
                plugin.messages().send(victim, "keepinv.kept");
            }
        } else if (plugin.configs().keepInv().getBoolean("notify_victim_loss", false)) {
            plugin.messages().send(victim, "keepinv.dropped");
        }

        if (plugin.configs().keepInv().getBoolean("debug", false)) {
            plugin.getLogger().info("[KeepInv] victim=" + victim.getName()
                    + " killer=" + (killer == null ? "null" : killer.getName())
                    + " victim_status=" + victimStatus + " killer_status=" + killerStatus
                    + " keep=" + keep);
        }
    }

    private boolean decide(StatusModule.RpStatus victim, StatusModule.RpStatus killer) {
        Map<String, Object> rules = plugin.configs().keepInv()
                .getConfigurationSection("rules") != null
                ? plugin.configs().keepInv().getConfigurationSection("rules").getValues(false)
                : Map.of();
        if (rules.isEmpty()) {
            // Default rule table per spec.
            if (victim == StatusModule.RpStatus.RP && killer == StatusModule.RpStatus.RP) return false;
            if (victim == StatusModule.RpStatus.NRP) return true;
            if (victim == StatusModule.RpStatus.RP && killer == StatusModule.RpStatus.NRP) return true;
            return true; // NONE => keep by default to avoid grief.
        }
        String key = (victim.name() + "_VS_" + killer.name()).toLowerCase();
        Object v = rules.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return true;
    }
}
