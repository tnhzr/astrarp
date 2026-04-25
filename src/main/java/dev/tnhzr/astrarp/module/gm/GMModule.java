package dev.tnhzr.astrarp.module.gm;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.AstraModule;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public final class GMModule implements AstraModule {

    private final AstraRP plugin;
    private RpcRepository repository;
    private RpcGui gui;
    private RpcEditSessions sessions;
    private GMListener listener;

    public GMModule(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override public String id() { return "gm"; }
    @Override public String defaultConfigName() { return "modules/gm.yml"; }

    @Override
    public void onEnable() {
        repository = new RpcRepository(plugin);
        repository.loadAll();
        sessions = new RpcEditSessions();
        gui = new RpcGui(plugin, repository);

        listener = new GMListener(plugin, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        FeelCommand feel = new FeelCommand(plugin);
        if (plugin.getCommand("feel") != null) {
            plugin.getCommand("feel").setExecutor(feel);
            plugin.getCommand("feel").setTabCompleter(feel);
        }

        RpcCommand rpc = new RpcCommand(plugin, this);
        if (plugin.getCommand("rpc") != null) {
            plugin.getCommand("rpc").setExecutor(rpc);
            plugin.getCommand("rpc").setTabCompleter(rpc);
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) HandlerList.unregisterAll(listener);
        if (sessions != null) sessions = null;
    }

    public RpcRepository repository() { return repository; }
    public RpcGui gui() { return gui; }
    public RpcEditSessions sessions() { return sessions; }
}
