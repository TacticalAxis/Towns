package net.tak7.towns.commands;

import net.tak7.api.PlayerUtils;
import net.tak7.towns.objects.CC;
import net.tak7.towns.objects.Money;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        // /bal and /bal set <playerName> <amount>
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(CC.getMessage("check-balance").replace("%money%", String.valueOf(Money.getMoney(((Player) sender).getUniqueId()))));
            } else {
                sender.sendMessage(ChatColor.RED + "You need to be a player to use this command, or do /bal <player name>");
            }
        } else if (args.length == 1) {
            if (!sender.hasPermission("cmd.towns.admin")) {
                sender.sendMessage(CC.getMessage("no-permission"));
                return true;
            }

            String playerName = args[0];
            String uuid = PlayerUtils.getUUIDFromName(playerName, false);
            if (uuid == null) {
                sender.sendMessage(CC.getMessage("player-no-exist"));
                return true;
            }

            sender.sendMessage(CC.getMessage("check-balance-other")
                    .replace("%money%", String.valueOf(Money.getMoney(((Player) sender).getUniqueId())))
                    .replace("%player%", playerName));
        } else if (args.length == 3) {
            if (!sender.hasPermission("cmd.towns.admin")) {
                sender.sendMessage(CC.getMessage("no-permission"));
                return true;
            }

            String uuid = PlayerUtils.getUUIDFromName(args[0], false);
            if (uuid == null) {
                sender.sendMessage(CC.getMessage("player-no-exist"));
                return true;
            }

            if (!args[1].equalsIgnoreCase("set")) {
                sendHelp(sender);
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
                sender.sendMessage(CC.getMessage("use-real-number"));
                return true;
            }

            Money.setMoney(UUID.fromString(uuid), amount);
            sender.sendMessage(CC.getMessage("set-player-balance").replace("%player%", args[0]).replace("%money%", String.valueOf(amount)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> argList = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("cmd.towns.admin")) {
            argList = PlayerUtils.getAllPlayers();
            return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && sender.hasPermission("cmd.towns.admin")) {
            argList.add("set");
            return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 3 && sender.hasPermission("cmd.towns.admin")) {
            if (args[1].equalsIgnoreCase("set")) {
                argList.add("<amount>");
            }
            return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        return null;
    }

    private void sendHelp(CommandSender sender) {
        if (sender.hasPermission("cmd.towns.admin")) {
            sender.sendMessage("§2§l§m=============§r§e Towns Help §e §2§l§m=============");
            sender.sendMessage("§a/balance: §7Base command");
            sender.sendMessage("§a   <player>: §7See player balance");
            sender.sendMessage("§a   <player> set <amount>: §7Set player balance");
            sender.sendMessage("§2§l§m====================================");
        }
    }
}