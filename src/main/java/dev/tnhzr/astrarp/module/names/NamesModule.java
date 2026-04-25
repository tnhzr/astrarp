package dev.tnhzr.astrarp.module.names;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.AstraModule;
import dev.tnhzr.astrarp.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NamesModule implements AstraModule {

    public record RpName(String name, long lastChanged) {}

    private final AstraRP plugin;
    private final Map<UUID, RpName> cache = new ConcurrentHashMap<>();
    private final Map<String, UUID> reverseCache = new ConcurrentHashMap<>();

    private RpNameCommand rpNameCommand;
    private RpANameCommand rpANameCommand;
    private NamesListener listener;

    public NamesModule(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public String id() { return "names"; }
    @Override public String defaultConfigName() { return "modules/names.yml"; }

    @Override
    public void onEnable() {
        loadAll();

        rpNameCommand = new RpNameCommand(plugin, this);
        rpANameCommand = new RpANameCommand(plugin, this);
        if (plugin.getCommand("rpname") != null) {
            plugin.getCommand("rpname").setExecutor(rpNameCommand);
            plugin.getCommand("rpname").setTabCompleter(rpNameCommand);
        }
        if (plugin.getCommand("rpaname") != null) {
            plugin.getCommand("rpaname").setExecutor(rpANameCommand);
            plugin.getCommand("rpaname").setTabCompleter(rpANameCommand);
        }

        listener = new NamesListener(plugin, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        for (Player p : Bukkit.getOnlinePlayers()) {
            applyDisplay(p);
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) HandlerList.unregisterAll(listener);
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                p.displayName(Component.text(p.getName()));
                p.playerListName(Component.text(p.getName()));
            } catch (Throwable ignored) {}
        }
        cache.clear();
        reverseCache.clear();
    }

    public Optional<RpName> get(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    public Optional<UUID> findByRpName(String rpName) {
        if (rpName == null) return Optional.empty();
        UUID id = reverseCache.get(rpName.toLowerCase());
        return Optional.ofNullable(id);
    }

    public boolean isOnCooldown(UUID uuid) {
        long cooldownSec = plugin.configs().names().getLong("cooldown_seconds", 300);
        RpName entry = cache.get(uuid);
        if (entry == null) return false;
        long diff = (System.currentTimeMillis() - entry.lastChanged()) / 1000L;
        return diff < cooldownSec;
    }

    public long cooldownRemaining(UUID uuid) {
        long cooldownSec = plugin.configs().names().getLong("cooldown_seconds", 300);
        RpName entry = cache.get(uuid);
        if (entry == null) return 0L;
        long diff = (System.currentTimeMillis() - entry.lastChanged()) / 1000L;
        return Math.max(0L, cooldownSec - diff);
    }

    public boolean validate(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        int min = plugin.configs().names().getInt("validation.min_length", 1);
        int max = plugin.configs().names().getInt("validation.max_length", 32);
        if (trimmed.length() < min || trimmed.length() > max) return false;

        // Reject ASCII control characters (newline, tab, etc.) — they corrupt
        // chat rendering and tab list. Everything else (spaces, emoji, |, &
        // codes, MM tags, etc.) is allowed by default.
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 0x20 || c == 0x7F) return false;
        }

        // Optional opt-in regex for admins who want stricter validation.
        String pattern = plugin.configs().names().getString("validation.regex", "");
        if (pattern == null || pattern.isBlank()) return true;
        try {
            return trimmed.matches(pattern);
        } catch (Exception ex) {
            return true;
        }
    }

    public void set(UUID uuid, String name, boolean ignoreCooldown) {
        long now = System.currentTimeMillis();
        RpName previous = cache.get(uuid);
        if (previous != null) reverseCache.remove(previous.name().toLowerCase());

        cache.put(uuid, new RpName(name, ignoreCooldown ? now - 1_000_000_000L : now));
        reverseCache.put(name.toLowerCase(), uuid);

        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "INSERT INTO rp_names(uuid,name,last_changed) VALUES(?,?,?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET name=excluded.name, last_changed=excluded.last_changed")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setLong(3, ignoreCooldown ? 0L : now);
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("rp_names save failed: " + ex.getMessage());
            }
        });

        Player p = Bukkit.getPlayer(uuid);
        if (p != null) applyDisplay(p);
    }

    public void clear(UUID uuid) {
        RpName previous = cache.remove(uuid);
        if (previous != null) reverseCache.remove(previous.name().toLowerCase());
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "DELETE FROM rp_names WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("rp_names delete failed: " + ex.getMessage());
            }
        });
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            try {
                p.displayName(Component.text(p.getName()));
                p.playerListName(Component.text(p.getName()));
            } catch (Throwable ignored) {}
        }
    }

    public void applyDisplay(Player player) {
        boolean updateDisplayName = plugin.configs().names().getBoolean("apply.display_name", true);
        boolean updateTabName = plugin.configs().names().getBoolean("apply.tab_list", true);
        String format = plugin.configs().names().getString("apply.format", "<gold>{rpname}</gold>");

        Optional<RpName> entry = get(player.getUniqueId());
        if (entry.isEmpty()) {
            if (updateDisplayName) player.displayName(Component.text(player.getName()));
            if (updateTabName) player.playerListName(Component.text(player.getName()));
            return;
        }

        Component rendered = Text.parse(format, Map.of("rpname", entry.get().name(), "player", player.getName()));
        if (updateDisplayName) player.displayName(rendered);
        if (updateTabName) player.playerListName(rendered);

        if (plugin.integrations().hasLuckPerms() && plugin.integrations().luckPerms() != null
                && plugin.configs().names().getBoolean("luckperms.write_meta", true)) {
            String key = plugin.configs().names().getString("luckperms.meta_key", "astrarp_rpname");
            plugin.integrations().luckPerms().setMeta(player.getUniqueId(), key, entry.get().name());
        }
    }

    public Map<UUID, RpName> snapshot() {
        return new HashMap<>(cache);
    }

    private void loadAll() {
        cache.clear();
        reverseCache.clear();
        try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                "SELECT uuid, name, last_changed FROM rp_names");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String name = rs.getString("name");
                    long ts = rs.getLong("last_changed");
                    cache.put(uuid, new RpName(name, ts));
                    reverseCache.put(name.toLowerCase(), uuid);
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("rp_names load failed: " + ex.getMessage());
        }
    }
}
