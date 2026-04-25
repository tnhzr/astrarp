package dev.tnhzr.astrarp.module.names;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Args;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RpANameCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;
    private final NamesModule module;

    public RpANameCommand(AstraRP plugin, NamesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("astrarp.name.admin")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }
        if (args.length < 3) {
            plugin.messages().send(sender, "names.usage_admin");
            return true;
        }
        String targetName = args[0];
        if (!"set".equalsIgnoreCase(args[1])) {
            plugin.messages().send(sender, "names.usage_admin");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getUniqueId() == null) {
            plugin.messages().send(sender, "common.player_not_found", Map.of("player", targetName));
            return true;
        }

        String name = Args.parseQuoted(args, 2);
        if (name == null || name.isBlank() || !module.validate(name)) {
            plugin.messages().send(sender, "names.invalid");
            return true;
        }

        module.set(target.getUniqueId(), name, true);
        plugin.messages().send(sender, "names.set_admin",
                Map.of("player", target.getName() == null ? targetName : target.getName(), "name", name));
        if (target.isOnline() && target.getPlayer() != null) {
            plugin.messages().send(target.getPlayer(), "names.set_self", Map.of("name", name));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
            return out;
        }
        if (args.length == 2) return List.of("set");
        return List.of();
    }
}
