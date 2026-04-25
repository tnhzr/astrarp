package dev.tnhzr.astrarp.module.gm;

import dev.tnhzr.astrarp.AstraRP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RpcRepository {

    private final AstraRP plugin;
    private final Map<String, RpcCharacter> cache = new ConcurrentHashMap<>();

    public RpcRepository(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        cache.clear();
        try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                "SELECT id, display_name, style, icon, radius FROM rp_characters");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cache.put(rs.getString("id"),
                        new RpcCharacter(
                                rs.getString("id"),
                                rs.getString("display_name"),
                                rs.getString("style"),
                                rs.getString("icon"),
                                rs.getInt("radius")));
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("rpc load failed: " + ex.getMessage());
        }
    }

    public Optional<RpcCharacter> get(String id) {
        return Optional.ofNullable(cache.get(id.toLowerCase()));
    }

    public Collection<RpcCharacter> all() {
        return new LinkedHashMap<>(cache).values();
    }

    public void save(RpcCharacter character) {
        cache.put(character.id().toLowerCase(), character);
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "INSERT INTO rp_characters(id,display_name,style,icon,radius,created_at) " +
                            "VALUES(?,?,?,?,?,?) " +
                            "ON CONFLICT(id) DO UPDATE SET display_name=excluded.display_name, " +
                            "style=excluded.style, icon=excluded.icon, radius=excluded.radius")) {
                ps.setString(1, character.id().toLowerCase());
                ps.setString(2, character.displayName());
                ps.setString(3, character.style());
                ps.setString(4, character.icon());
                ps.setInt(5, character.radius());
                ps.setLong(6, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("rpc save failed: " + ex.getMessage());
            }
        });
    }

    public void delete(String id) {
        cache.remove(id.toLowerCase());
        plugin.database().async(() -> {
            try (PreparedStatement ps = plugin.database().connection().prepareStatement(
                    "DELETE FROM rp_characters WHERE id = ?")) {
                ps.setString(1, id.toLowerCase());
                ps.executeUpdate();
            } catch (Exception ex) {
                plugin.getLogger().warning("rpc delete failed: " + ex.getMessage());
            }
        });
    }
}
