package net.tak7.towns.commands;

import net.tak7.api.PlayerUtils;
import net.tak7.towns.objects.Money;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        // /bal and /bal set <playerName> <amount>
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage("Your balance is: " + Money.getMoney(((Player) sender).getUniqueId()));
            }
        } else {
            if (args.length == 3) {
                if (!args[0].equalsIgnoreCase("set")) {
                    sendHelp(sender);
                    return true;
                }

                String uuid = PlayerUtils.getUUIDFromName(args[1], false);
                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED + "That player does not exist!");
                    return true;
                }

                // check money is legit
                double amount = -1.0d;
                try {
                    amount = Double.parseDouble(args[2]);
                    if (amount < 0) {
                        throw new NumberFormatException("");
                    }
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "You need to enter a real non-negative number!");
                    return true;
                }

                Money.setMoney(UUID.fromString(uuid), amount);
                sender.sendMessage("You set " + args[1] + " to " + amount); // config
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {

        return null;
    }

    private void sendHelp(CommandSender sender) {
        if (sender.hasPermission("cmd.towns.admin")) {
            sender.sendMessage("§2§l§m=============§r§e Towns Help §e §2§l§m=============");
            sender.sendMessage("§a/balance: §7Base command");
            sender.sendMessage("§a   set <player> <amount>: §7Set player balance");
            sender.sendMessage("§2§l§m====================================");
        }
    }
}