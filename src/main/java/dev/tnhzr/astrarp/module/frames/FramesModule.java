package dev.tnhzr.astrarp.module.frames;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.AstraModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class FramesModule implements AstraModule {

    public enum AdminMode {
        NONE, CREATE, DELETE, CHECK, RESET_LOCAL, RESET_LOCAL_PLAYER
    }

    public record AdminContext(AdminMode mode, String extra) {}

    private final AstraRP plugin;
    private final Set<UUID> infiniteFrames = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, AdminContext> adminModes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> pendingGlobalReset = new ConcurrentHashMap<>();

    private FramesListener listener;
    private IfrCommand command;

    public FramesModule(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public String id() { return "frames"; }
    @Override public String defaultConfigName() { return "modules/frames.yml"; }

    @Override
    public void onEnable() {
        loadAll();
        listener = new FramesListener(plugin, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        command = new IfrCommand(plugin, this);
        if (plugin.getCommand("infiniteitemframe") != null) {
            plugin.getCommand("infiniteitemframe").setExecutor(command);
            plugin.getCommand("infiniteitemframe").setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) HandlerList.unregisterAll(listener);
        infiniteFrames.clear();
        adminModes.clear();
        pendingGlobalReset.clear();
    }

    public boolean isInfinite(ItemFrame frame) {
        return infiniteFrames.contains(frame.getUniqueId());
    }

    public AdminContext modeOf(UUID player) {
        return adminModes.getOrDefault(player, new AdminContext(AdminMode.NONE, null));
    }

    public void setMode(UUID player, AdminMode mode, String extra) {
        if (mode == AdminMode.NONE) {
            adminModes.remove(player);
        } else {
            adminModes.put(player, new AdminContext(mode, extra));
        }
    }

    public void markGlobalResetPending(UUID player) {
        pendingGlobalReset.put(player, System.currentTimeMillis() + 30_000L);
    }

    public boolean consumeGlobalResetConfirm(UUID player) {
        Long deadline = pendingGlobalReset.remove(player);
        return deadline != null && deadline >= System.currentTimeMillis();
    }

    public void registerFrame(ItemFrame frame, ItemStack snapshot) {
        infiniteFrames.add(frame.getUniqueId());
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "INSERT OR REPLACE INTO infinite_frames(frame_uuid,world,x,y,z,item_b64,created_at) " +
                            "VALUES(?,?,?,?,?,?,?)")) {
                ps.setString(1, frame.getUniqueId().toString());
                ps.setString(2, frame.getWorld().getName());
                ps.setInt(3, frame.getLocation().getBlockX());
                ps.setInt(4, frame.getLocation().getBlockY());
                ps.setInt(5, frame.getLocation().getBlockZ());
                ps.setString(6, Base64.getEncoder().encodeToString(snapshot.serializeAsBytes()));
                ps.setLong(7, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("frames save failed: " + ex.getMessage());
            }
        });
    }

    public void deregisterFrame(ItemFrame frame) {
        infiniteFrames.remove(frame.getUniqueId());
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "DELETE FROM infinite_frames WHERE frame_uuid = ?")) {
                ps.setString(1, frame.getUniqueId().toString());
                ps.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "DELETE FROM infinite_frame_takes WHERE frame_uuid = ?")) {
                ps.setString(1, frame.getUniqueId().toString());
                ps.executeUpdate();
            } catch (Exception ignored) {}
        });
    }

    public void loadStoredItem(ItemFrame frame, Consumer<Optional<ItemStack>> callback) {
        plugin.database().asyncThen(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "SELECT item_b64 FROM infinite_frames WHERE frame_uuid = ?")) {
                ps.setString(1, frame.getUniqueId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        byte[] bytes = Base64.getDecoder().decode(rs.getString(1));
                        return Optional.of(ItemStack.deserializeBytes(bytes));
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("frames load item failed: " + ex.getMessage());
            }
            return Optional.<ItemStack>empty();
        }, callback);
    }

    public boolean hasTaken(UUID frameId, UUID player) {
        try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                "SELECT 1 FROM infinite_frame_takes WHERE frame_uuid = ? AND player_uuid = ?")) {
            ps.setString(1, frameId.toString());
            ps.setString(2, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("frame take check failed: " + ex.getMessage());
            return false;
        }
    }

    public void markTaken(UUID frameId, UUID player) {
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "INSERT OR IGNORE INTO infinite_frame_takes(frame_uuid,player_uuid,taken_at) VALUES(?,?,?)")) {
                ps.setString(1, frameId.toString());
                ps.setString(2, player.toString());
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("frame take save failed: " + ex.getMessage());
            }
        });
    }

    public void resetTakes(UUID frameId, UUID specificPlayer) {
        plugin.database().async(() -> {
            String sql = specificPlayer == null
                    ? "DELETE FROM infinite_frame_takes WHERE frame_uuid = ?"
                    : "DELETE FROM infinite_frame_takes WHERE frame_uuid = ? AND player_uuid = ?";
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(sql)) {
                ps.setString(1, frameId.toString());
                if (specificPlayer != null) ps.setString(2, specificPlayer.toString());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("frame reset failed: " + ex.getMessage());
            }
        });
    }

    public void resetAllTakes() {
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "DELETE FROM infinite_frame_takes")) {
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("frame global reset failed: " + ex.getMessage());
            }
        });
    }

    public void listTakers(UUID frameId, Consumer<Set<UUID>> callback) {
        plugin.database().asyncThen(() -> {
            Set<UUID> takers = new LinkedHashSet<>();
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "SELECT player_uuid FROM infinite_frame_takes WHERE frame_uuid = ? ORDER BY taken_at")) {
                ps.setString(1, frameId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try { takers.add(UUID.fromString(rs.getString(1))); } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("frame list takers failed: " + ex.getMessage());
            }
            return takers;
        }, callback);
    }

    private void loadAll() {
        infiniteFrames.clear();
        try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                "SELECT frame_uuid FROM infinite_frames");
             ResultSet rs = ps.executeQuery()) {
            Set<UUID> loaded = new HashSet<>();
            while (rs.next()) {
                try { loaded.add(UUID.fromString(rs.getString(1))); } catch (Exception ignored) {}
            }
            infiniteFrames.addAll(loaded);
            plugin.getLogger().info("Loaded " + loaded.size() + " infinite frames.");
        } catch (Exception ex) {
            plugin.getLogger().warning("frames load failed: " + ex.getMessage());
        }
    }
}
