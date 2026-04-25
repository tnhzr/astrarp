package dev.tnhzr.astrarp.placeholder;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.names.NamesModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import dev.tnhzr.astrarp.util.Text;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
        switch (params.toLowerCase()) {
            case "status" -> {
                StatusModule.RpStatus s = plugin.status().get(player.getUniqueId());
                return Text.plain(Text.parse(plugin.status().iconRaw(s)));
            }
            case "status_raw" -> {
                StatusModule.RpStatus s = plugin.status().get(player.getUniqueId());
                return plugin.status().rawString(s);
            }
            case "rpname" -> {
                NamesModule.RpName entry = plugin.names().get(player.getUniqueId()).orElse(null);
                if (entry != null) return entry.name();
                return player.getName() == null ? "" : player.getName();
            }
            case "rpname_raw" -> {
                NamesModule.RpName entry = plugin.names().get(player.getUniqueId()).orElse(null);
                return entry == null ? "" : entry.name();
            }
            default -> {
                return null;
            }
        }
    }
}
