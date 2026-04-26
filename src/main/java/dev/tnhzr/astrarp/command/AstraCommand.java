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
            case "help", "?" -> {
                sendHelp(sender);
                return true;
            }
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
            case "chatheads-aliases", "chatheads" -> {
                if (!sender.hasPermission("astrarp.admin.reload")) {
                    plugin.messages().send(sender, "common.no_permission");
                    return true;
                }
                printChatHeadsAliases(sender);
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
            // Resolve via the online roster first, then the cached offline list.
            // Avoid Bukkit.getOfflinePlayer(String) — it blocks the main thread on
            // a Mojang lookup when the name has never been seen on the server.
            Player online = Bukkit.getPlayerExact(args[1]);
            if (online != null) {
                target = online;
            } else {
                target = Bukkit.getOfflinePlayerIfCached(args[1]);
            }
        } else if (sender instanceof Player p) {
            target = p;
        }

        if (args.length >= 2 && target == null) {
            sender.sendMessage(Component.text("No cached player named '" + args[1] +
                    "' — log in once or pass an online nickname."));
            return;
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
                ? StatusModule.RpStatus.NRP
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

    private void sendHelp(CommandSender sender) {
        plugin.messages().send(sender, "core.help_header");
        plugin.messages().send(sender, "core.help_reload");
        plugin.messages().send(sender, "core.help_debug");
        plugin.messages().send(sender, "core.help_chatheads");
        plugin.messages().send(sender, "core.help_help");
        plugin.messages().send(sender, "core.help_subcommands");
    }

    /**
     * Prints a JSON5 {@code nameAliases} block that the server admin can paste
     * into {@code config/chat_heads.json5} on every client. This is the
     * recommended ChatHeads integration path for setups that use FlectonePulse
     * or any other plugin that strips the original username from chat — the
     * mod uses these aliases to resolve RP-names back to real player UUIDs
     * without us injecting visible suffixes that break custom-font resource
     * packs.
     */
    private void printChatHeadsAliases(CommandSender sender) {
        if (plugin.names() == null) {
            sender.sendMessage(Component.text("Names module is disabled — no aliases to print."));
            return;
        }
        java.util.Map<String, String> aliases = new java.util.LinkedHashMap<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.names().get(online.getUniqueId()).ifPresent(rp ->
                    aliases.put(rp.name(), online.getName()));
        }
        sender.sendMessage(Component.text("AstraRP \u2192 ChatHeads aliases (" + aliases.size() + "):"));
        sender.sendMessage(Component.text("  Paste into config/chat_heads.json5 on the client:"));
        sender.sendMessage(Component.text("  \"nameAliases\": {"));
        int i = 0;
        for (java.util.Map.Entry<String, String> e : aliases.entrySet()) {
            String tail = (++i == aliases.size()) ? "" : ",";
            // chat_heads.json5 stores aliases as { "<rendered text>": "<real username>" }
            sender.sendMessage(Component.text("    \"" + escapeJson(e.getKey()) + "\": \""
                    + escapeJson(e.getValue()) + "\"" + tail));
        }
        sender.sendMessage(Component.text("  }"));
        sender.sendMessage(Component.text("After pasting, restart the client (or run /chatheads reload)."));
    }

    /**
     * Escapes a string for safe insertion into a JSON / JSON5 double-quoted
     * string literal. RP-names allow arbitrary characters including
     * {@code "} and {@code \}, both of which would otherwise terminate the
     * literal early or be interpreted as escape sequences.
     */
    private static String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder out = new StringBuilder(input.length() + 2);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload", "debug", "chatheads-aliases", "help");
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return names;
        }
        return List.of();
    }
}
