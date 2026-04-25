package dev.tnhzr.astrarp.placeholder;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.names.NamesModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import dev.tnhzr.astrarp.util.Text;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AstraPlaceholders extends PlaceholderExpansion {

    private final AstraRP plugin;

    public AstraPlaceholders(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "astrarp"; }
    @Override public @NotNull String getAuthor() { return "tnhzr"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        StatusModule status = plugin.status();
        NamesModule names = plugin.names();
        switch (params.toLowerCase()) {
            // Default %astrarp_status% returns raw MiniMessage so chat plugins
            // that re-parse it with MiniMessage (FlectonePulse, default Paper
            // chat) keep colours. Use _legacy / _plain for other consumers.
            case "status" -> {
                if (status == null) return "";
                return status.iconRaw(status.get(player.getUniqueId()));
            }
            case "status_legacy" -> {
                if (status == null) return "";
                return LegacyComponentSerializer.legacySection()
                        .serialize(Text.parse(status.iconRaw(status.get(player.getUniqueId()))));
            }
            case "status_plain" -> {
                if (status == null) return "";
                return Text.plain(Text.parse(status.iconRaw(status.get(player.getUniqueId()))));
            }
            case "status_raw" -> {
                if (status == null) return "";
                return status.rawString(status.get(player.getUniqueId()));
            }
            case "rpname" -> {
                if (names == null) {
                    return player.getName() == null ? "" : player.getName();
                }
                NamesModule.RpName entry = names.get(player.getUniqueId()).orElse(null);
                if (entry != null) return entry.name();
                return player.getName() == null ? "" : player.getName();
            }
            case "rpname_raw" -> {
                if (names == null) return "";
                NamesModule.RpName entry = names.get(player.getUniqueId()).orElse(null);
                return entry == null ? "" : entry.name();
            }
            default -> {
                return null;
            }
        }
    }
}
