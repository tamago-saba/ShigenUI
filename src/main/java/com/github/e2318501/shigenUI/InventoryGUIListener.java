package com.github.e2318501.shigenUI;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public class InventoryGUIListener implements Listener {
    private final ImmutableMap<Integer, Runnable> slotActions;
    @Getter
    private final Inventory inventory;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory) && slotActions.containsKey(event.getSlot())) {
            event.setCancelled(true);
            slotActions.get(event.getSlot()).run();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.getHandlers().unregister(this);
        }
    }
}
