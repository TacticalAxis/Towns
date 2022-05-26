package net.tak7.towns.commands;

import net.tak7.towns.PlayerTowns;
import net.tak7.towns.objects.CC;
import net.tak7.towns.objects.Town;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerTownCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length >= 1) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    PlayerTowns.mainConfig.reloadConfiguration();
                    sender.sendMessage(ChatColor.GREEN + "PlayerTowns successfully reloaded!");
                } else {
                    sendHelp(sender);
                }
            } else {
                String name = args[1];
                Town town = Town.getTownFromName(name);
                if (town != null) {
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("delete")) {
                            sender.sendMessage(ChatColor.GREEN + "Successfully removed town: " + ChatColor.YELLOW + town.getTownName());
                            PlayerTowns.towns.remove(town);
                        } else {
                            sendHelp(sender);
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("set")) {
                            try {
                                double money = Double.parseDouble(args[2]);
                                if (money < 0) {
                                    sender.sendMessage(CC.getMessage("use-real-number"));
                                    return true;
                                } else {
                                    town.setTownMoney(money);
                                    sender.sendMessage(CC.getMessage("set-town-money")
                                            .replace("%town%", town.getTownName())
                                            .replace("%amount%", String.valueOf(money))
                                            .replace("%currency%", PlayerTowns.CURRENCY_NAME));
                                }
                            } catch (Exception e) {
                                sender.sendMessage(CC.getMessage("use-real-number"));
                            }
                        } else {
                            sendHelp(sender);
                        }
                    }
                } else {
                    sender.sendMessage(CC.getMessage("town-not-exists"));
                }
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§2§l§m=============§r§e Towns Help §e §2§l§m=============");
        sender.sendMessage("§a/playertowns: §7Base command");
        sender.sendMessage("§a   set <town> <amount>: §7Set a town's money");
        sender.sendMessage("§a   delete <town>: §7Delete a town with the name");
        sender.sendMessage("§a   reload: §7Reload Plugin Config");
        sender.sendMessage("§2§l§m====================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> argList = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("cmd.towns.admin")) {
            argList.add("set");
            argList.add("delete");
            argList.add("reload");
            return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && sender.hasPermission("cmd.towns.admin")) {
            for (Town t : PlayerTowns.towns) {

                argList.add(t.getTownName());
            }

            return argList.stream().filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 3 && sender.hasPermission("cmd.towns.admin")) {
            if (args[0].equalsIgnoreCase("set")) {
                argList.add("<amount>");
            }

            return argList.stream().filter(a -> a.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }
        return argList;
    }
}