package dev.tnhzr.astrarp.command;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.module.names.NamesModule;
import dev.tnhzr.astrarp.module.status.StatusModule;
import dev.tnhzr.astrarp.util.Text;
import net.kyori.adventure.text.Component;
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
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("astrarp.admin.reload")) {
                    plugin.messages().send(sender, "common.no_permission");
                    return true;
                }
                plugin.reloadAll();
                plugin.messages().send(sender, "core.reloaded");
                return true;
            }
            case "debug" -> {
                if (!sender.hasPermission("astrarp.admin.reload")) {
                    plugin.messages().send(sender, "common.no_permission");
                    return true;
                }
                runDebug(sender, args);
                return true;
            }
            default -> {
                plugin.messages().send(sender, "core.usage");
                return true;
            }
        }
    }

    private void runDebug(CommandSender sender, String[] args) {
        OfflinePlayer target = null;
        if (args.length >= 2) {
            target = Bukkit.getOfflinePlayerIfCached(args[1]);
            if (target == null) target = Bukkit.getOfflinePlayer(args[1]);
        } else if (sender instanceof Player p) {
            target = p;
        }

        sender.sendMessage(Component.text("AstraRP debug ── integrations:"));
        sender.sendMessage(Component.text("  PlaceholderAPI : " + plugin.integrations().hasPlaceholderAPI()));
        sender.sendMessage(Component.text("  LuckPerms      : " + plugin.integrations().hasLuckPerms()));
        sender.sendMessage(Component.text("  FlectonePulse  : " + plugin.integrations().hasFlectonePulse()));
        sender.sendMessage(Component.text("  TAB            : " + plugin.integrations().hasTAB()));

        if (target == null) {
            sender.sendMessage(Component.text("Provide a player name to inspect placeholders."));
            return;
        }
        sender.sendMessage(Component.text("AstraRP debug ── values for " + target.getName() + ":"));

        StatusModule.RpStatus status = plugin.status() == null
                ? StatusModule.RpStatus.NONE
                : plugin.status().get(target.getUniqueId());
        sender.sendMessage(Component.text("  status_raw : " + (plugin.status() == null
                ? "(module disabled)"
                : plugin.status().rawString(status))));
        sender.sendMessage(Text.parse("  status     : " + (plugin.status() == null
                ? "(module disabled)"
                : plugin.status().iconRaw(status))));

        if (plugin.names() != null) {
            String rp = plugin.names().get(target.getUniqueId())
                    .map(NamesModule.RpName::name)
                    .orElse("(unset)");
            sender.sendMessage(Component.text("  rpname     : " + rp));
        } else {
            sender.sendMessage(Component.text("  rpname     : (module disabled)"));
        }

        if (plugin.integrations().hasPlaceholderAPI()) {
            try {
                Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Object resolved = papi.getMethod("setPlaceholders", OfflinePlayer.class, String.class)
                        .invoke(null, target, "%astrarp_rpname%");
                sender.sendMessage(Component.text("  PAPI parse : %astrarp_rpname% -> '" + resolved + "'"));
            } catch (Throwable t) {
                sender.sendMessage(Component.text("  PAPI parse : failed (" + t.getMessage() + ")"));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload", "debug");
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return names;
        }
        return List.of();
    }
}
