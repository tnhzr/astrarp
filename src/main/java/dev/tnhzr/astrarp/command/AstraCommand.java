package dev.tnhzr.astrarp.command;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AstraCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;

    public AstraCommand(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.messages().send(sender, "core.about");
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("astrarp.admin.reload")) {
                plugin.messages().send(sender, "common.no_permission");
                return true;
            }
            plugin.reloadAll();
            plugin.messages().send(sender, "core.reloaded");
            return true;
        }
        plugin.messages().send(sender, "core.usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload");
        return List.of();
    }
}
