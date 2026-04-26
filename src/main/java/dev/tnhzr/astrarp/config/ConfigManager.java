package dev.tnhzr.astrarp.config;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {

    private final AstraRP plugin;

    private YamlConfiguration config;
    private YamlConfiguration modules;
    private YamlConfiguration statusCfg;
    private YamlConfiguration namesCfg;
    private YamlConfiguration keepInvCfg;
    private YamlConfiguration framesCfg;
    private YamlConfiguration gmCfg;

    public ConfigManager(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        File dir = plugin.getDataFolder();
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("Cannot create data folder " + dir);
        }
        File modDir = new File(dir, "modules");
        if (!modDir.exists() && !modDir.mkdirs()) {
            plugin.getLogger().warning("Cannot create modules folder " + modDir);
        }

        config = saveAndLoad("config.yml");
        modules = saveAndLoad("modules.yml");

        statusCfg = saveAndLoad("modules/status.yml");
        namesCfg = saveAndLoad("modules/names.yml");
        keepInvCfg = saveAndLoad("modules/keepinventory.yml");
        framesCfg = saveAndLoad("modules/frames.yml");
        gmCfg = saveAndLoad("modules/gm.yml");

        migrateLegacyDefaults();
    }

    /**
     * Rewrite legacy default values that we used to ship and that users almost
     * certainly never customised. {@link #saveAndLoad} only fills in *missing*
     * keys, so once a file is on disk with the old default we never replace it
     * automatically — which is why bracket-formatted RPC names kept showing up
     * after the v1.0.5 default change. Anything matched here is replaced with
     * the current jar default.
     */
    private void migrateLegacyDefaults() {
        // v1.0.0 default: "<gold>[ <reset>{name}<gold> ]</gold> {style}{text}"
        String currentRpcFormat = gmCfg.getString("rpc.format");
        if (currentRpcFormat != null && currentRpcFormat.contains("<gold>[ <reset>")
                && currentRpcFormat.contains("<gold> ]</gold>")) {
            String newFormat = "{name} {style}{text}";
            gmCfg.set("rpc.format", newFormat);
            try {
                gmCfg.save(new File(plugin.getDataFolder(), "modules/gm.yml"));
                plugin.getLogger().info("Migrated legacy rpc.format default \u2014 brackets removed.");
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to save migrated gm.yml: " + ex.getMessage());
            }
        }

        // v1.0.6 default ChatHeads suffix: " <#1a1a1a><i>({player})</i></#1a1a1a>".
        // The italic suffix produced rendering glitches on servers running custom-
        // font resource packs (CraftEngine / ItemsAdder), so v1.0.7+ ships with
        // chatheads.enabled: false by default. Any user that hadn't customised
        // either key gets flipped automatically; users with a custom format are
        // left alone.
        String currentSuffix = config.getString("chatheads.suffix_format");
        boolean enabled = config.getBoolean("chatheads.enabled", false);
        if (enabled && currentSuffix != null
                && currentSuffix.equals(" <#1a1a1a><i>({player})</i></#1a1a1a>")) {
            config.set("chatheads.enabled", false);
            config.set("chatheads.suffix_format", " <#1a1a1a>({player})</#1a1a1a>");
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
                plugin.getLogger().info("Migrated legacy ChatHeads suffix default \u2014 disabled, italic stripped.");
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to save migrated config.yml: " + ex.getMessage());
            }
        }
    }

    private YamlConfiguration saveAndLoad(String resource) {
        File target = new File(plugin.getDataFolder(), resource);
        if (!target.exists()) {
            try (InputStream in = plugin.getResource(resource)) {
                if (in != null) {
                    File parent = target.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                plugin.getLogger().warning("Cannot copy default " + resource + ": " + ex.getMessage());
            }
        }

        YamlConfiguration cfg = new YamlConfiguration();
        if (target.exists()) {
            try {
                cfg.load(target);
            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to load " + resource + ": " + ex.getMessage());
            }
        } else {
            try (InputStream in = plugin.getResource(resource)) {
                if (in != null) {
                    cfg.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {}
        }
        // Merge defaults from jar resource so newer keys appear automatically.
        try (InputStream in = plugin.getResource(resource)) {
            if (in != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8));
                cfg.setDefaults(defaults);
                cfg.options().copyDefaults(true);
            }
        } catch (Exception ignored) {}
        try {
            cfg.save(target);
        } catch (IOException ignored) {}
        return cfg;
    }

    public YamlConfiguration root() { return config; }
    public YamlConfiguration modules() { return modules; }
    public YamlConfiguration status() { return statusCfg; }
    public YamlConfiguration names() { return namesCfg; }
    public YamlConfiguration keepInv() { return keepInvCfg; }
    public YamlConfiguration frames() { return framesCfg; }
    public YamlConfiguration gm() { return gmCfg; }
}
