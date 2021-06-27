package net.tak7.towns.commands;

import net.tak7.towns.PlayerTowns;
import net.tak7.towns.objects.Money;
import net.tak7.towns.objects.Town;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("NullableProblems")
public class SellhandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            ItemStack item = player.getInventory().getItemInMainHand();
            String itemName = item.getType().name();

            if (Town.getPrice(itemName) != -1.0d && item.getType() != Material.AIR) {
                // item can be sold
                Money.addMoney(player.getUniqueId(), Town.getPrice(itemName) * item.getAmount());
                player.sendMessage(ChatColor.GREEN + "You successfully sold " + item.getAmount() + "x" +ChatColor.YELLOW + itemName + ChatColor.GREEN + " for " + ChatColor.YELLOW + PlayerTowns.CURRENCY_NAME + (Town.getPrice(itemName) * item.getAmount()));
                item.setAmount(0);
            } else {
                player.sendMessage(ChatColor.RED + "You can't sell this item!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players are allowed to use this command");
        }
        return true;
    }
}