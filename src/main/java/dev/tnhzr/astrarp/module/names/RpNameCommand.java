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
import java.util.Optional;
import java.util.UUID;

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
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            plugin.messages().send(sender, "names.help_header");
            plugin.messages().send(sender, "names.help_set");
            plugin.messages().send(sender, "names.help_find");
            plugin.messages().send(sender, "names.help_help");
            return true;
        }

        String sub = args[0].toLowerCase();
        if ("find".equals(sub)) {
            return handleFind(sender, args);
        }
        if (!"set".equals(sub)) {
            plugin.messages().send(sender, "names.usage_self");
            return true;
        }

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

    private boolean handleFind(CommandSender sender, String[] args) {
        if (!sender.hasPermission("astrarp.name.find")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "names.usage_find");
            return true;
        }
        String query = Args.parseQuoted(args, 1);
        if (query == null) query = args[1];
        if (query.isBlank()) {
            plugin.messages().send(sender, "names.usage_find");
            return true;
        }

        // Direction 1: query is RP-name -> find owner's username.
        Optional<UUID> uuidOpt = module.findByRpName(query);
        if (uuidOpt.isPresent()) {
            String username = resolveUsername(uuidOpt.get());
            plugin.messages().send(sender, "names.find_rp_to_user",
                    Map.of("rpname", query, "player", username));
            return true;
        }

        // Direction 2: query is username -> find RP-name.
        Player onlineMatch = Bukkit.getPlayerExact(query);
        UUID uuid;
        String username;
        if (onlineMatch != null) {
            uuid = onlineMatch.getUniqueId();
            username = onlineMatch.getName();
        } else {
            OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(query);
            if (cached == null) {
                plugin.messages().send(sender, "names.find_none", Map.of("query", query));
                return true;
            }
            uuid = cached.getUniqueId();
            username = cached.getName() == null ? query : cached.getName();
        }

        Optional<NamesModule.RpName> entry = module.get(uuid);
        if (entry.isEmpty()) {
            plugin.messages().send(sender, "names.find_user_no_rp",
                    Map.of("player", username));
            return true;
        }
        plugin.messages().send(sender, "names.find_user_to_rp",
                Map.of("player", username, "rpname", entry.get().name()));
        return true;
    }

    private String resolveUsername(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
        return off.getName() == null ? uuid.toString() : off.getName();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> all = new ArrayList<>(List.of("set", "find", "help"));
            List<String> out = new ArrayList<>();
            for (String s : all) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
            return out;
        }
        if (args.length == 2 && "find".equalsIgnoreCase(args[0])) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
            return out;
        }
        return List.of();
    }
}
