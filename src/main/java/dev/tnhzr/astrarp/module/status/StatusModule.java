package dev.tnhzr.astrarp.module.status;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.AstraModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StatusModule implements AstraModule {

    public enum RpStatus {
        /**
         * Legacy "off" state. Kept in the enum so old database rows still
         * deserialize, but new commands never set it: every player ends up
         * either {@link #RP} or {@link #NRP}.
         */
        @Deprecated
        NONE,
        RP,
        NRP
    }

    private final AstraRP plugin;
    private final Map<UUID, RpStatus> cache = new ConcurrentHashMap<>();

    private RpCommand rpCommand;
    private NrpCommand nrpCommand;
    private StatusListener listener;

    public StatusModule(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public String id() { return "status"; }
    @Override public String defaultConfigName() { return "modules/status.yml"; }

    @Override
    public void onEnable() {
        loadAll();

        rpCommand = new RpCommand(plugin, this);
        nrpCommand = new NrpCommand(plugin, this);
        if (plugin.getCommand("rp") != null) {
            plugin.getCommand("rp").setExecutor(rpCommand);
            plugin.getCommand("rp").setTabCompleter(rpCommand);
        }
        if (plugin.getCommand("nrp") != null) {
            plugin.getCommand("nrp").setExecutor(nrpCommand);
            plugin.getCommand("nrp").setTabCompleter(nrpCommand);
        }

        listener = new StatusListener(plugin, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        // Refresh display for already-online players (e.g. after /astrarp reload).
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyDisplay(p);
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) HandlerList.unregisterAll(listener);
        cache.clear();
    }

    public RpStatus get(UUID uuid) {
        // Default state is NRP; we no longer expose NONE to commands.
        // Legacy NONE rows from older versions are migrated lazily on first read.
        RpStatus current = cache.get(uuid);
        if (current == null || current == RpStatus.NONE) {
            return RpStatus.NRP;
        }
        return current;
    }

    public boolean has(UUID uuid) {
        RpStatus current = cache.get(uuid);
        return current != null && current != RpStatus.NONE;
    }

    public void set(UUID uuid, RpStatus status) {
        cache.put(uuid, status);
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "INSERT INTO rp_status(uuid,status) VALUES(?,?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET status=excluded.status")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, status.name());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("rp_status save failed: " + ex.getMessage());
            }
        });
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) applyDisplay(p);
    }

    /**
     * Toggle RP: if already RP, switch to NRP; otherwise switch to RP. Never
     * sets NONE — there is no "off" state any more.
     */
    public RpStatus toggleRp(UUID uuid) {
        RpStatus current = get(uuid);
        RpStatus next = current == RpStatus.RP ? RpStatus.NRP : RpStatus.RP;
        set(uuid, next);
        return next;
    }

    /**
     * Toggle NRP: if already NRP, switch to RP; otherwise switch to NRP.
     * Never sets NONE.
     */
    public RpStatus toggleNrp(UUID uuid) {
        RpStatus current = get(uuid);
        RpStatus next = current == RpStatus.NRP ? RpStatus.RP : RpStatus.NRP;
        set(uuid, next);
        return next;
    }

    public String iconRaw(RpStatus status) {
        return switch (status) {
            case RP -> plugin.configs().status().getString("icons.rp", "<green>[RP]</green>");
            case NRP -> plugin.configs().status().getString("icons.nrp", "<red>[NRP]</red>");
            case NONE -> plugin.configs().status().getString("icons.none", "");
        };
    }

    public String rawString(RpStatus status) {
        return switch (status) {
            case RP -> plugin.configs().status().getString("raw.rp", "RP");
            case NRP -> plugin.configs().status().getString("raw.nrp", "NRP");
            case NONE -> plugin.configs().status().getString("raw.none", "");
        };
    }

    public void applyDisplay(Player player) {
        boolean luckperms = plugin.configs().status().getBoolean("luckperms.write_meta", true);
        if (luckperms && plugin.integrations().hasLuckPerms() && plugin.integrations().luckPerms() != null) {
            String key = plugin.configs().status().getString("luckperms.meta_key", "astrarp_status");
            RpStatus status = get(player.getUniqueId());
            String val = rawString(status);
            if (val == null || val.isEmpty()) {
                plugin.integrations().luckPerms().clearMeta(player.getUniqueId(), key);
            } else {
                plugin.integrations().luckPerms().setMeta(player.getUniqueId(), key, val);
            }
        }
    }

    public Map<UUID, RpStatus> snapshot() {
        return new HashMap<>(cache);
    }

    private void loadAll() {
        cache.clear();
        try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                "SELECT uuid, status FROM rp_status");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    RpStatus st = RpStatus.valueOf(rs.getString("status"));
                    cache.put(uuid, st);
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("rp_status load failed: " + ex.getMessage());
        }
    }
}
