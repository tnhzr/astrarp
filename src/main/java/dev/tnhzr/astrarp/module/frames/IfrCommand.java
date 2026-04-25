package dev.tnhzr.astrarp.module.frames;

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

public final class IfrCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;
    private final FramesModule module;

    public IfrCommand(AstraRP plugin, FramesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("astrarp.ifr.admin")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "common.players_only");
            return true;
        }

        if (args.length == 0) {
            module.setMode(player.getUniqueId(), FramesModule.AdminMode.CREATE, null);
            plugin.messages().send(player, "frames.mode_create");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "del", "delete" -> {
                module.setMode(player.getUniqueId(), FramesModule.AdminMode.DELETE, null);
                plugin.messages().send(player, "frames.mode_delete");
            }
            case "check" -> {
                module.setMode(player.getUniqueId(), FramesModule.AdminMode.CHECK, null);
                plugin.messages().send(player, "frames.mode_check");
            }
            case "reset" -> handleReset(player, args);
            case "confirm" -> {
                if (module.consumeGlobalResetConfirm(player.getUniqueId())) {
                    module.resetAllTakes();
                    plugin.messages().send(player, "frames.reset_global_done");
                } else {
                    plugin.messages().send(player, "frames.reset_global_no_pending");
                }
            }
            default -> plugin.messages().send(player, "frames.usage");
        }
        return true;
    }

    private void handleReset(Player player, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(player, "frames.usage_reset");
            return;
        }
        String scope = args[1].toLowerCase();
        switch (scope) {
            case "global" -> {
                module.markGlobalResetPending(player.getUniqueId());
                plugin.messages().send(player, "frames.reset_global_confirm");
            }
            case "local" -> {
                if (args.length >= 3) {
                    module.setMode(player.getUniqueId(), FramesModule.AdminMode.RESET_LOCAL_PLAYER, args[2]);
                } else {
                    module.setMode(player.getUniqueId(), FramesModule.AdminMode.RESET_LOCAL, null);
                }
                plugin.messages().send(player, "frames.mode_reset_local");
            }
            default -> plugin.messages().send(player, "frames.usage_reset");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("astrarp.ifr.admin")) return List.of();
        if (args.length == 1) {
            return List.of("del", "reset", "check", "confirm");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return List.of("local", "global");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("reset") && args[1].equalsIgnoreCase("local")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) names.add(p.getName());
            }
            return names;
        }
        return List.of();
    }
}
