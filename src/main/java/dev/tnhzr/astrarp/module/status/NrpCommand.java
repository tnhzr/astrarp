package dev.tnhzr.astrarp.module.status;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NrpCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;
    private final StatusModule module;

    public NrpCommand(AstraRP plugin, StatusModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "common.players_only");
                return true;
            }
            if (!sender.hasPermission("astrarp.status.use")) {
                plugin.messages().send(sender, "common.no_permission");
                return true;
            }
            StatusModule.RpStatus next = module.toggleNrp(player.getUniqueId());
            String key = next == StatusModule.RpStatus.NRP ? "status.self_nrp_on" : "status.self_nrp_off";
            plugin.messages().send(player, key);
            return true;
        }

        if (!sender.hasPermission("astrarp.status.admin")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.messages().send(sender, "common.player_not_found", Map.of("player", args[0]));
            return true;
        }

        StatusModule.RpStatus next = module.toggleNrp(target.getUniqueId());
        String key = next == StatusModule.RpStatus.NRP ? "status.admin_nrp_on" : "status.admin_nrp_off";
        plugin.messages().send(sender, key, Map.of("player", target.getName()));
        plugin.messages().send(target, key.replace("admin", "self"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("astrarp.status.admin")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    names.add(p.getName());
                }
            }
            return names;
        }
        return List.of();
    }
}
