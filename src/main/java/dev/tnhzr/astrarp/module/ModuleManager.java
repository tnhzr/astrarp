package dev.tnhzr.astrarp.module;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleManager {

    private final AstraRP plugin;
    private final Map<String, AstraModule> modules = new LinkedHashMap<>();
    private final Map<String, Boolean> enabled = new LinkedHashMap<>();

    public ModuleManager(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void register(AstraModule module) {
        modules.put(module.id(), module);
    }

    public boolean isEnabled(String id) {
        return Boolean.TRUE.equals(enabled.get(id));
    }

    public void enableConfigured() {
        YamlConfiguration cfg = plugin.configs().modules();
        for (AstraModule module : modules.values()) {
            boolean on = cfg.getBoolean("modules." + module.id() + ".enabled", true);
            if (!on) {
                plugin.getLogger().info("Module '" + module.id() + "' disabled in modules.yml");
                enabled.put(module.id(), false);
                continue;
            }
            try {
                module.onEnable();
                enabled.put(module.id(), true);
                plugin.getLogger().info("Module '" + module.id() + "' enabled.");
            } catch (Throwable t) {
                enabled.put(module.id(), false);
                plugin.getLogger().severe("Failed to enable module '" + module.id() + "': " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    public void disableAll() {
        for (Map.Entry<String, AstraModule> e : modules.entrySet()) {
            if (Boolean.TRUE.equals(enabled.get(e.getKey()))) {
                try {
                    e.getValue().onDisable();
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error disabling module '" + e.getKey() + "': " + t.getMessage());
                }
            }
        }
        enabled.clear();
    }
}
