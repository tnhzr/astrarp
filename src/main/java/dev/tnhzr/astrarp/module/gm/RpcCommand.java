package dev.tnhzr.astrarp.module.gm;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Args;
import dev.tnhzr.astrarp.util.Text;
import net.kyori.adventure.text.Component;
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
import java.util.Optional;

public final class RpcCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;
    private final GMModule module;

    public RpcCommand(AstraRP plugin, GMModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1 && ("help".equalsIgnoreCase(args[0]) || "?".equals(args[0]))) {
            plugin.messages().send(sender, "gm.rpc_help_header");
            plugin.messages().send(sender, "gm.rpc_help_open");
            plugin.messages().send(sender, "gm.rpc_help_create");
            plugin.messages().send(sender, "gm.rpc_help_list");
            plugin.messages().send(sender, "gm.rpc_help_delete");
            plugin.messages().send(sender, "gm.rpc_help_speak");
            plugin.messages().send(sender, "gm.rpc_help_help");
            return true;
        }

        if (!sender.hasPermission("astrarp.rpc.use")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "common.players_only");
                return true;
            }
            module.gui().openList(player);
            return true;
        }

        String first = args[0].toLowerCase();
        if (first.equals("show")) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "common.players_only");
                return true;
            }
            module.gui().openList(player);
            return true;
        }
        if (first.equals("create")) {
            if (!(sender instanceof Player player)) {
                plugin.messages().send(sender, "common.players_only");
                return true;
            }
            String idHint = args.length >= 2 ? args[1].toLowerCase() : "new_npc";
            RpcCharacter draft = new RpcCharacter(idHint, idHint, "<gray><i>", null, 32);
            module.sessions().setDraft(player.getUniqueId(), draft);
            module.gui().openEditor(player, draft, true);
            return true;
        }
        if (first.equals("delete") || first.equals("remove")) {
            if (args.length < 2) {
                plugin.messages().send(sender, "gm.rpc_usage");
                return true;
            }
            module.repository().delete(args[1]);
            plugin.messages().send(sender, "gm.rpc_deleted", Map.of("id", args[1]));
            return true;
        }
        if (first.equals("list")) {
            int n = 0;
            StringBuilder sb = new StringBuilder();
            for (RpcCharacter ch : module.repository().all()) {
                if (n++ > 0) sb.append(", ");
                sb.append(ch.id());
            }
            plugin.messages().send(sender, "gm.rpc_list",
                    Map.of("count", String.valueOf(n), "ids", sb.toString()));
            return true;
        }

        // /rpc <id> "text" [radius] [player]
        Optional<RpcCharacter> opt = module.repository().get(first);
        if (opt.isEmpty()) {
            plugin.messages().send(sender, "gm.rpc_not_found", Map.of("id", first));
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "gm.rpc_usage");
            return true;
        }

        String text = Args.parseQuoted(args, 1);
        if (text == null || text.isBlank()) {
            plugin.messages().send(sender, "gm.rpc_usage");
            return true;
        }
        // Compute index after the quoted text.
        int afterTextIdx = afterQuoted(args, 1);
        Integer radius = null;
        String privatePlayer = null;
        if (args.length > afterTextIdx) {
            try {
                radius = Integer.parseInt(args[afterTextIdx]);
                afterTextIdx++;
            } catch (NumberFormatException ex) {
                privatePlayer = args[afterTextIdx];
                afterTextIdx++;
            }
        }
        if (privatePlayer == null && args.length > afterTextIdx) {
            privatePlayer = args[afterTextIdx];
        }

        broadcast(sender, opt.get(), text, radius, privatePlayer);
        return true;
    }

    private int afterQuoted(String[] args, int from) {
        if (from >= args.length) return from;
        if (!args[from].startsWith("\"")) return from + 1;
        if (args[from].length() > 1 && args[from].endsWith("\"")) return from + 1;
        for (int i = from + 1; i < args.length; i++) {
            if (args[i].endsWith("\"")) return i + 1;
        }
        return args.length;
    }

    private void broadcast(CommandSender sender, RpcCharacter character, String text,
                           Integer radiusOverride, String privatePlayer) {
        int radius = radiusOverride == null ? character.radius() : radiusOverride;
        String format = plugin.configs().gm().getString("rpc.format",
                "{name} {style}{text}");
        Component message = Text.parse(format, Map.of(
                "name", character.displayName(),
                "style", character.style() == null ? "" : character.style(),
                "text", text));

        if (privatePlayer != null) {
            Player target = Bukkit.getPlayerExact(privatePlayer);
            if (target == null) {
                plugin.messages().send(sender, "common.player_not_found",
                        Map.of("player", privatePlayer));
                return;
            }
            target.sendMessage(message);
            plugin.messages().send(sender, "gm.rpc_sent_private", Map.of("player", target.getName()));
            return;
        }

        if (sender instanceof Player p) {
            int sent = 0;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.getWorld().equals(p.getWorld())
                        && other.getLocation().distance(p.getLocation()) <= radius) {
                    other.sendMessage(message);
                    sent++;
                }
            }
            plugin.messages().send(sender, "gm.rpc_sent_local",
                    Map.of("count", String.valueOf(sent)));
        } else {
            for (Player other : Bukkit.getOnlinePlayers()) other.sendMessage(message);
            plugin.messages().send(sender, "gm.rpc_sent_global",
                    Map.of("count", String.valueOf(Bukkit.getOnlinePlayers().size())));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> all = new ArrayList<>(List.of("show", "create", "delete", "list", "help"));
            for (RpcCharacter ch : module.repository().all()) all.add(ch.id());
            List<String> out = new ArrayList<>();
            for (String s : all) if (s.toLowerCase().startsWith(args[0].toLowerCase())) out.add(s);
            return out;
        }
        return List.of();
    }
}
