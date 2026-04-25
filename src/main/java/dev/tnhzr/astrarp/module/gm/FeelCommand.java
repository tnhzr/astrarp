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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FeelCommand implements CommandExecutor, TabCompleter {

    private final AstraRP plugin;

    public FeelCommand(AstraRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("astrarp.feel.use")) {
            plugin.messages().send(sender, "common.no_permission");
            return true;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "gm.feel_usage");
            return true;
        }
        String selector = args[0];
        String text = Args.parseQuoted(args, 1);
        if (text == null || text.isBlank()) {
            plugin.messages().send(sender, "gm.feel_usage");
            return true;
        }

        Set<Player> targets = resolve(sender, selector);
        if (targets.isEmpty()) {
            plugin.messages().send(sender, "common.player_not_found", Map.of("player", selector));
            return true;
        }

        String style = plugin.configs().gm().getString("feel.style", "<gray><i>{text}</i></gray>");
        Component msg = Text.parse(style, Map.of("text", text));
        for (Player p : targets) p.sendMessage(msg);
        plugin.messages().send(sender, "gm.feel_sent",
                Map.of("count", String.valueOf(targets.size())));
        return true;
    }

    private Set<Player> resolve(CommandSender sender, String selector) {
        Set<Player> out = new HashSet<>();
        if (selector.startsWith("@")) {
            try {
                List<Entity> entities = Bukkit.selectEntities(sender, selector);
                for (Entity e : entities) {
                    if (e instanceof Player p) out.add(p);
                }
            } catch (IllegalArgumentException ex) {
                // Fall through to name lookup.
            }
            return out;
        }
        // Try RP-name first, then real name.
        var rp = plugin.names().findByRpName(selector);
        if (rp.isPresent()) {
            Player p = Bukkit.getPlayer(rp.get());
            if (p != null) out.add(p);
            return out;
        }
        Player p = Bukkit.getPlayerExact(selector);
        if (p != null) out.add(p);
        return out;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("astrarp.feel.use")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) names.add(p.getName());
            }
            return names;
        }
        return List.of();
    }
}
