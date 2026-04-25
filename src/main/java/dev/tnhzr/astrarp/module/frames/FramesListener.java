package dev.tnhzr.astrarp.module.frames;

import dev.tnhzr.astrarp.AstraRP;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class FramesListener implements Listener {

    private final AstraRP plugin;
    private final FramesModule module;

    public FramesListener(AstraRP plugin, FramesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;
        Player player = event.getPlayer();
        FramesModule.AdminContext ctx = module.modeOf(player.getUniqueId());

        if (ctx.mode() != FramesModule.AdminMode.NONE) {
            event.setCancelled(true);
            handleAdminClick(player, frame, ctx);
            module.setMode(player.getUniqueId(), FramesModule.AdminMode.NONE, null);
            return;
        }

        if (!module.isInfinite(frame)) return;
        event.setCancelled(true);

        if (module.hasTaken(frame.getUniqueId(), player.getUniqueId())) {
            plugin.messages().send(player, "frames.already_taken");
            return;
        }

        module.loadStoredItem(frame, optStack -> giveCopy(player, frame.getUniqueId(), optStack));
    }

    private void giveCopy(Player player, UUID frameId, Optional<ItemStack> optStack) {
        if (optStack.isEmpty()) {
            plugin.messages().send(player, "frames.empty_storage");
            return;
        }
        ItemStack copy = optStack.get().clone();
        copy.setAmount(Math.max(1, copy.getAmount()));

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(copy);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
            plugin.messages().send(player, "frames.given_dropped");
        } else {
            plugin.messages().send(player, "frames.given");
        }
        module.markTaken(frameId, player.getUniqueId());
    }

    private void handleAdminClick(Player player, ItemFrame frame, FramesModule.AdminContext ctx) {
        switch (ctx.mode()) {
            case CREATE -> {
                ItemStack inFrame = frame.getItem();
                if (inFrame == null || inFrame.getType().isAir()) {
                    plugin.messages().send(player, "frames.create_empty");
                    return;
                }
                if (module.isInfinite(frame)) {
                    plugin.messages().send(player, "frames.already_infinite");
                    return;
                }
                module.registerFrame(frame, inFrame.clone());
                plugin.messages().send(player, "frames.created");
            }
            case DELETE -> {
                if (!module.isInfinite(frame)) {
                    plugin.messages().send(player, "frames.not_infinite");
                    return;
                }
                module.deregisterFrame(frame);
                plugin.messages().send(player, "frames.deleted");
            }
            case CHECK -> {
                if (!module.isInfinite(frame)) {
                    plugin.messages().send(player, "frames.not_infinite");
                    return;
                }
                module.listTakers(frame.getUniqueId(), takers -> {
                    if (takers.isEmpty()) {
                        plugin.messages().send(player, "frames.takers_empty");
                        return;
                    }
                    StringBuilder list = new StringBuilder();
                    int i = 0;
                    for (UUID id : takers) {
                        if (i++ > 0) list.append(", ");
                        OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                        list.append(op.getName() == null ? id.toString() : op.getName());
                    }
                    plugin.messages().send(player, "frames.takers_list",
                            Map.of("count", String.valueOf(takers.size()), "players", list.toString()));
                });
            }
            case RESET_LOCAL -> {
                if (!module.isInfinite(frame)) {
                    plugin.messages().send(player, "frames.not_infinite");
                    return;
                }
                module.resetTakes(frame.getUniqueId(), null);
                plugin.messages().send(player, "frames.reset_local_all");
            }
            case RESET_LOCAL_PLAYER -> {
                if (!module.isInfinite(frame)) {
                    plugin.messages().send(player, "frames.not_infinite");
                    return;
                }
                String name = ctx.extra();
                OfflinePlayer op = Bukkit.getOfflinePlayer(name);
                module.resetTakes(frame.getUniqueId(), op.getUniqueId());
                Map<String, String> ph = new HashMap<>();
                ph.put("player", name);
                plugin.messages().send(player, "frames.reset_local_player", ph);
            }
            default -> {}
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (module.isInfinite(frame)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (module.isInfinite(frame)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (module.isInfinite(frame)) {
            event.setCancelled(true);
        }
    }
}
