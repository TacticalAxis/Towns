package net.tak7.towns.listeners;

import net.tak7.towns.objects.Money;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryEvents implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (Town.inventoryIsShop(event.getView().getTopInventory())) {
                event.setCancelled(true);
                if (event.getViewers().size() > 1) {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Someone else is currently using that inventory!");
                    return;
                }

                Player player = (Player) event.getWhoClicked();
                Inventory shopInventory = event.getView().getTopInventory();
                Inventory playerInventory = event.getView().getBottomInventory();
                Town buyFrom = Town.getTownFromName(event.getView().getTitle().split(" ")[0].replace(ChatColor.GREEN + "", ""));
                Town playerTown = Town.getTownFromPlayer(player);

                if (event.getClick() != ClickType.LEFT) {
                    return;
                }

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null) {
                    if (Town.getPrice(clickedItem.getType().name()) != -1.0d) {
                        if (event.getClickedInventory() == playerInventory) {
                            // player clicked item in their inventory, they wanna sell
                            if (buyFrom == playerTown) {
                                if (itemCanBePut(clickedItem, shopInventory) > 0) {
                                    int put = itemCanBePut(clickedItem, shopInventory);
                                    ItemStack toBePut = clickedItem.clone();
                                    toBePut.setAmount(put);
                                    clickedItem.setAmount(clickedItem.getAmount() - put);
                                    ItemMeta im = toBePut.getItemMeta();
                                    if (im != null) {
                                        List<String> lore = new ArrayList<>();
                                        lore.add(ChatColor.GREEN + "Price: " + Town.getPrice(toBePut.getType().name()));
                                        im.setLore(lore);
                                        toBePut.setItemMeta(im);
                                        shopInventory.addItem(toBePut);
                                    }
                                }
                            }
                        } else if (event.getClickedInventory() == event.getView().getTopInventory()) {
                            // player clicked item in shop, they wanna buy
                            Material mat = clickedItem.getType();
                            if (playerTown != buyFrom) {
                                double price = Town.getPrice(clickedItem.getType().name());
                                if (Money.getMoney(player.getUniqueId()) < price) {
                                    player.sendMessage(ChatColor.RED + "Insufficient Funds!");
                                    return;
                                }
                                Money.subtractMoney(player.getUniqueId(), price);
                                player.sendMessage(ChatColor.GREEN + "You bought " + mat.name() + " for " + price + "!");
                            }
                            clickedItem.setAmount(clickedItem.getAmount() - 1);
                            player.getInventory().addItem(new ItemStack(mat, 1));
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot sell this item!");
                    }
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

    private int itemCanBePut(ItemStack item, Inventory toBePut) {
        int canPut = 0;
        int left = item.getAmount();
        for (ItemStack i : toBePut.getContents()) {
            if (i != null) {
                if (i.getType() == item.getType()) {
                    if (i.getAmount() + item.getAmount() <= 64) {
                        return item.getAmount();
                    }
                    if (left > 0) {
                        canPut += 64 - i.getAmount();
                        left -= 64 - i.getAmount();
                    }
                }
            }
        }
        if (left > 0) {
            if (toBePut.firstEmpty() > -1) {
                return canPut + left;
            }
        }
        return canPut;
    }
}