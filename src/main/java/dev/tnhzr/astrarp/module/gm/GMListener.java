package dev.tnhzr.astrarp.module.gm;

import dev.tnhzr.astrarp.AstraRP;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public final class GMListener implements Listener {

    private final AstraRP plugin;
    private final GMModule module;

    public GMListener(AstraRP plugin, GMModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RpcGuiHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.setCancelled(true);

        if (RpcGui.LIST_TITLE.equals(holder.tag())) {
            handleListClick(player, event);
        } else if (RpcGui.EDIT_TITLE.equals(holder.tag())) {
            handleEditClick(player, event);
        }
    }

    private void handleListClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        var name = event.getCurrentItem().getItemMeta() == null
                ? null
                : event.getCurrentItem().getItemMeta().displayName();
        if (name == null) return;
        String displayPlain = PlainTextComponentSerializer.plainText().serialize(name);

        RpcCharacter match = null;
        for (RpcCharacter ch : module.repository().all()) {
            String chPlain = PlainTextComponentSerializer.plainText().serialize(
                    dev.tnhzr.astrarp.util.Text.parse(ch.displayName()));
            if (chPlain.equals(displayPlain)) {
                match = ch;
                break;
            }
        }
        if (match == null) return;

        ClickType ct = event.getClick();
        if (ct == ClickType.RIGHT) {
            plugin.messages().send(player, "gm.rpc_delete_hint");
            return;
        }
        if (ct == ClickType.SHIFT_RIGHT) {
            module.repository().delete(match.id());
            plugin.messages().send(player, "gm.rpc_deleted",
                    java.util.Map.of("id", match.id()));
            module.gui().openList(player);
            return;
        }
        // LEFT click — open editor
        RpcCharacter draft = new RpcCharacter(match.id(), match.displayName(), match.style(),
                match.icon(), match.radius());
        module.sessions().setDraft(player.getUniqueId(), draft);
        module.gui().openEditor(player, draft, false);
    }

    private void handleEditClick(Player player, InventoryClickEvent event) {
        RpcCharacter draft = module.sessions().draft(player.getUniqueId());
        if (draft == null) return;
        int slot = event.getRawSlot();

        switch (slot) {
            case 10 -> startChat(player, draft, RpcEditSessions.Field.ID, true);
            case 11 -> startChat(player, draft, RpcEditSessions.Field.DISPLAY_NAME, false);
            case 12 -> startChat(player, draft, RpcEditSessions.Field.STYLE, false);
            case 13 -> startChat(player, draft, RpcEditSessions.Field.ICON, false);
            case 14 -> startChat(player, draft, RpcEditSessions.Field.RADIUS, false);
            case 18 -> {
                module.sessions().clear(player.getUniqueId());
                player.closeInventory();
                plugin.messages().send(player, "gm.rpc_cancelled");
            }
            case 22 -> {
                if (draft.id() == null || draft.id().isBlank()) {
                    plugin.messages().send(player, "gm.rpc_id_required");
                    return;
                }
                module.repository().save(draft);
                module.sessions().clear(player.getUniqueId());
                player.closeInventory();
                plugin.messages().send(player, "gm.rpc_saved", java.util.Map.of("id", draft.id()));
            }
            case 26 -> {
                module.repository().delete(draft.id());
                module.sessions().clear(player.getUniqueId());
                player.closeInventory();
                plugin.messages().send(player, "gm.rpc_deleted", java.util.Map.of("id", draft.id()));
            }
            default -> {}
        }
    }

    private void startChat(Player player, RpcCharacter draft, RpcEditSessions.Field field, boolean idChange) {
        module.sessions().setAwaiting(player.getUniqueId(),
                new RpcEditSessions.Pending(draft.id(), idChange, field));
        // Paper deadlocks the inventory packet sequence if we close from inside
        // an InventoryClickEvent handler synchronously — defer one tick.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            plugin.messages().send(player, "gm.rpc_enter_value",
                    java.util.Map.of("field", field.name().toLowerCase()));
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        RpcEditSessions.Pending pending = module.sessions().awaiting(uuid);
        if (pending == null) return;

        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        event.setCancelled(true);
        // Empty the recipient set so chat plugins running at MONITOR/HIGHEST cannot
        // resurrect the message (FlectonePulse re-broadcasts cancelled events).
        try { event.viewers().clear(); } catch (Throwable ignored) {}

        plugin.getServer().getScheduler().runTask(plugin, () -> applyValue(event.getPlayer(), pending, text));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onLegacyChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        RpcEditSessions.Pending pending = module.sessions().awaiting(uuid);
        if (pending == null) return;

        String text = event.getMessage();
        event.setCancelled(true);
        try { event.getRecipients().clear(); } catch (Throwable ignored) {}

        plugin.getServer().getScheduler().runTask(plugin, () -> applyValue(event.getPlayer(), pending, text));
    }

    private void applyValue(Player player, RpcEditSessions.Pending pending, String text) {
        RpcCharacter draft = module.sessions().draft(player.getUniqueId());
        if (draft == null) {
            module.sessions().clear(player.getUniqueId());
            return;
        }
        if ("отмена".equalsIgnoreCase(text.trim()) || "cancel".equalsIgnoreCase(text.trim())) {
            module.sessions().setAwaiting(player.getUniqueId(), null);
            module.gui().openEditor(player, draft, pending.isNew);
            return;
        }
        switch (pending.field) {
            case ID -> {
                String newId = text.trim().toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
                if (newId.isEmpty()) {
                    plugin.messages().send(player, "gm.rpc_id_required");
                    return;
                }
                RpcCharacter renamed = new RpcCharacter(newId, draft.displayName(), draft.style(),
                        draft.icon(), draft.radius());
                module.sessions().setDraft(player.getUniqueId(), renamed);
                module.sessions().setAwaiting(player.getUniqueId(), null);
                module.gui().openEditor(player, renamed, pending.isNew);
            }
            case DISPLAY_NAME -> {
                draft.setDisplayName(text);
                module.sessions().setAwaiting(player.getUniqueId(), null);
                module.gui().openEditor(player, draft, pending.isNew);
            }
            case STYLE -> {
                draft.setStyle(text);
                module.sessions().setAwaiting(player.getUniqueId(), null);
                module.gui().openEditor(player, draft, pending.isNew);
            }
            case ICON -> {
                draft.setIcon(text);
                module.sessions().setAwaiting(player.getUniqueId(), null);
                module.gui().openEditor(player, draft, pending.isNew);
            }
            case RADIUS -> {
                try {
                    draft.setRadius(Integer.parseInt(text.trim()));
                } catch (NumberFormatException ex) {
                    plugin.messages().send(player, "gm.rpc_invalid_number");
                    return;
                }
                module.sessions().setAwaiting(player.getUniqueId(), null);
                module.gui().openEditor(player, draft, pending.isNew);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Drafts now live for the duration of the player's editing flow; they are
        // cleared explicitly by Save / Cancel / Delete or on disable. Auto-clearing
        // on close raced with the chat-then-reopen flow and broke buttons.
    }
}
