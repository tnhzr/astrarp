package dev.tnhzr.astrarp.module.names;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Args;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class RpNameCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;
    private final NamesModule module;

    public RpNameCommand(AstraRP plugin, NamesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "common.players_only");
            return true;
        }
        if (!sender.hasPermission("astrarp.name.set")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "names.usage_self");
            return true;
        }
        String sub = args[0];
        if (!sub.equalsIgnoreCase("set")) {
            plugin.messages().send(sender, "names.usage_self");
            return true;
        }

        boolean admin = sender.hasPermission("astrarp.name.admin");
        if (!admin && module.isOnCooldown(player.getUniqueId())) {
            plugin.messages().send(sender, "names.cooldown",
                    Map.of("seconds", String.valueOf(module.cooldownRemaining(player.getUniqueId()))));
            return true;
        }

        String name = Args.parseQuoted(args, 1);
        if (name == null || name.isBlank()) {
            plugin.messages().send(sender, "names.invalid");
            return true;
        }
        if (!module.validate(name)) {
            plugin.messages().send(sender, "names.invalid");
            return true;
        }

        module.set(player.getUniqueId(), name, admin);
        plugin.messages().send(sender, "names.set_self", Map.of("name", name));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("set");
        return List.of();
    }
}
