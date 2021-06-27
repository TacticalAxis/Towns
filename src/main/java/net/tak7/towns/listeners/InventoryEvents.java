package net.tak7.towns.listeners;

import net.tak7.towns.objects.Town;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryEvents implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (Town.inventoryIsShop(event.getView().getTopInventory())) {
                event.setCancelled(true);

                if (event.getClick() != ClickType.LEFT) {
                    return;
                }

                if (Town.getShop(event.getView()) != null && event.getView().getTopInventory().getViewers().size() == 1) {
                    ItemStack item = event.getCurrentItem();
                    Town from = Town.getShop(event.getView());
                    Town to = Town.getTownFromPlayer((Player) event.getWhoClicked());
                    Player player = (Player) event.getWhoClicked();

                    if (from != null && to != null && item != null) {
                        if (item.getType() != Material.AIR) {
                            if (from != to) {
                                from.buyItem(item, to, player);
                            } else {
                                from.removeItem(item);
                                player.getInventory().addItem(new ItemStack(item.getType(), item.getAmount()));
                            }
                        }
                    }
                } else {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Someone else is using that shop right now!");
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        if (Town.inventoryIsShop(event.getInventory())) {
            if (Town.isShop(event.getView())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryInteractEvent event) {
        if (Town.inventoryIsShop(event.getInventory())) {
            if (Town.isShop(event.getView())) {
                event.setCancelled(true);
            }
        }
    }
}