package dev.tnhzr.astrarp.integration;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.names.NamesModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import dev.tnhzr.astrarp.util.Text;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

/**
 * Registers AstraRP placeholders directly with TAB so that templates such as
 * {@code tabprefix: "%astrarp_status% "} or {@code customtabname:
 * "%astrarp_rpname%"} resolve even on installations that do not run
 * PlaceholderAPI alongside TAB.
 */
public final class TabBridge {

    private final AstraRP plugin;

    public TabBridge(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();

        pm.registerPlayerPlaceholder("%astrarp_rpname%", 500, tabPlayer -> {
            UUID uuid = tabPlayer.getUniqueId();
            NamesModule names = plugin.names();
            if (names == null) return tabPlayer.getName();
            return names.get(uuid)
                    .map(NamesModule.RpName::name)
                    .orElseGet(tabPlayer::getName);
        });

        pm.registerPlayerPlaceholder("%astrarp_rpname_raw%", 500, tabPlayer -> {
            NamesModule names = plugin.names();
            if (names == null) return "";
            return names.get(tabPlayer.getUniqueId())
                    .map(NamesModule.RpName::name)
                    .orElse("");
        });

        // TAB renders placeholder strings with legacy '\u00a7' colour codes by
        // default, so MiniMessage tags would otherwise show up as literal text.
        pm.registerPlayerPlaceholder("%astrarp_status%", 500, tabPlayer -> {
            StatusModule status = plugin.status();
            if (status == null) return "";
            StatusModule.RpStatus s = status.get(tabPlayer.getUniqueId());
            return LegacyComponentSerializer.legacySection().serialize(Text.parse(status.iconRaw(s)));
        });

        pm.registerPlayerPlaceholder("%astrarp_status_raw%", 500, tabPlayer -> {
            StatusModule status = plugin.status();
            if (status == null) return "";
            return status.rawString(status.get(tabPlayer.getUniqueId()));
        });

        plugin.getLogger().info("Registered AstraRP placeholders with TAB.");
    }
}
