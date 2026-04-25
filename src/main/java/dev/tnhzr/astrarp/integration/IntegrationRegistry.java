package dev.tnhzr.astrarp.integration;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.Bukkit;

public final class IntegrationRegistry {

    private final AstraRP plugin;
    private boolean luckperms;
    private boolean placeholderApi;
    private boolean flectonePulse;
    private boolean tab;

    private LuckPermsBridge luckPermsBridge;

    public IntegrationRegistry(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void discover() {
        luckperms = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
        placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        flectonePulse = Bukkit.getPluginManager().getPlugin("FlectonePulse") != null;
        tab = Bukkit.getPluginManager().getPlugin("TAB") != null;

        if (luckperms) {
            try {
                luckPermsBridge = new LuckPermsBridge(plugin);
                plugin.getLogger().info("LuckPerms bridge ready.");
            } catch (Throwable t) {
                luckperms = false;
                plugin.getLogger().warning("LuckPerms bridge failed: " + t.getMessage());
            }
        }
        if (placeholderApi) plugin.getLogger().info("Detected PlaceholderAPI.");
        if (flectonePulse) plugin.getLogger().info("Detected FlectonePulse.");
        if (tab) plugin.getLogger().info("Detected TAB.");
    }

    public boolean hasLuckPerms() { return luckperms; }
    public boolean hasPlaceholderAPI() { return placeholderApi; }
    public boolean hasFlectonePulse() { return flectonePulse; }
    public boolean hasTAB() { return tab; }

    public LuckPermsBridge luckPerms() { return luckPermsBridge; }
}
