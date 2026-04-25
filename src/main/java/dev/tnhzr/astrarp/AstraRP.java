package dev.tnhzr.astrarp;

import dev.tnhzr.astrarp.command.AstraCommand;
import dev.tnhzr.astrarp.config.ConfigManager;
import dev.tnhzr.astrarp.config.MessageManager;
import dev.tnhzr.astrarp.integration.IntegrationRegistry;
import dev.tnhzr.astrarp.module.ModuleManager;
import dev.tnhzr.astrarp.module.frames.FramesModule;
import dev.tnhzr.astrarp.module.gm.GMModule;
import dev.tnhzr.astrarp.module.keepinv.KeepInvModule;
import dev.tnhzr.astrarp.module.names.NamesModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import dev.tnhzr.astrarp.placeholder.AstraPlaceholders;
import dev.tnhzr.astrarp.storage.Database;
import dev.tnhzr.astrarp.util.Banner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstraRP extends JavaPlugin {

    private static AstraRP instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private Database database;
    private ModuleManager moduleManager;
    private IntegrationRegistry integrations;

    private StatusModule statusModule;
    private NamesModule namesModule;
    private KeepInvModule keepInvModule;
    private FramesModule framesModule;
    private GMModule gmModule;

    public static AstraRP get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.messageManager = new MessageManager(this);
        this.messageManager.reload();

        this.database = new Database(this);
        this.database.connect();

        this.integrations = new IntegrationRegistry(this);
        this.integrations.discover();

        this.moduleManager = new ModuleManager(this);

        this.statusModule = new StatusModule(this);
        this.namesModule = new NamesModule(this);
        this.keepInvModule = new KeepInvModule(this);
        this.framesModule = new FramesModule(this);
        this.gmModule = new GMModule(this);

        this.moduleManager.register(statusModule);
        this.moduleManager.register(namesModule);
        this.moduleManager.register(keepInvModule);
        this.moduleManager.register(framesModule);
        this.moduleManager.register(gmModule);

        this.moduleManager.enableConfigured();

        AstraCommand astraCommand = new AstraCommand(this);
        if (getCommand("astrarp") != null) {
            getCommand("astrarp").setExecutor(astraCommand);
            getCommand("astrarp").setTabCompleter(astraCommand);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                me.clip.placeholderapi.expansion.PlaceholderExpansion existing =
                        me.clip.placeholderapi.PlaceholderAPIPlugin.getInstance()
                                .getLocalExpansionManager()
                                .getExpansion("astrarp");
                if (existing != null) {
                    existing.unregister();
                }
                boolean ok = new AstraPlaceholders(this).register();
                if (ok) {
                    getLogger().info("Hooked PlaceholderAPI: %astrarp_*% placeholders are live.");
                } else {
                    getLogger().warning("PlaceholderAPI register() returned false. " +
                            "Run /papi reload, then /papi parse <player> %astrarp_rpname% to debug.");
                }
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        } else {
            getLogger().info("PlaceholderAPI not present \u2014 %astrarp_*% placeholders unavailable to other plugins.");
        }

        integrations.registerTabPlaceholders();

        if (configManager.root().getBoolean("banner.enabled", true)) {
            Banner.print(this);
        } else {
            getLogger().info("AstraRP v" + getDescription().getVersion() + " enabled.");
        }
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        if (database != null) {
            database.close();
        }
        instance = null;
    }

    public void reloadAll() {
        configManager.loadAll();
        messageManager.reload();
        moduleManager.disableAll();
        moduleManager.enableConfigured();
    }

    public ConfigManager configs() { return configManager; }
    public MessageManager messages() { return messageManager; }
    public Database database() { return database; }
    public ModuleManager modules() { return moduleManager; }
    public IntegrationRegistry integrations() { return integrations; }

    public StatusModule status() { return statusModule; }
    public NamesModule names() { return namesModule; }
    public KeepInvModule keepInv() { return keepInvModule; }
    public FramesModule frames() { return framesModule; }
    public GMModule gm() { return gmModule; }
}
