package dev.tnhzr.astrarp.module.gm;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class RpcGuiHolder implements InventoryHolder {

    private final String tag;
    private Inventory inventory;

    public RpcGuiHolder(String tag) {
        this.tag = tag;
    }

    public String tag() { return tag; }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) inventory = Bukkit.createInventory(this, 9);
        return inventory;
    }
}
