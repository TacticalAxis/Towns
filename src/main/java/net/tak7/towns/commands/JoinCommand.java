package net.tak7.towns.commands;

import net.tak7.towns.PlayerTowns;
import net.tak7.towns.objects.Invite;
import net.tak7.towns.objects.Rank;
import net.tak7.towns.objects.Town;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"ConstantConditions", "NullableProblems"})
public class JoinCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                String townName = args[0];
                Player player = (Player) sender;
                Town town = Town.getTownFromName(townName);

                if (town != null) {

                }


                if (town != null) {
                    if (town.playerInvited(player)) {
                        Town leave = Town.getTownFromPlayer(player);
                        if (leave != null) {
                            town.removePlayer(player.getUniqueId());
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    PlayerTowns.mainConfig.cfg().getString("message-town-left").replace("%town%", town.getTownName())));
                        }
                        town.getMembers().put(player.getUniqueId(), Rank.CIVILIAN);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                PlayerTowns.mainConfig.cfg().getString("message-town-joined").replace("%town%", town.getTownName())));
                        Invite remove;
//                        for (Invite i : Player)
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "That town does not exist!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid input. Do /join <town name>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> argList = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("cmd.towns.player")) {
            for (Invite i : PlayerTowns.invitations) {
                Player player = (Player) sender;
                if (i.getPlayer() == player) {
                    argList.add(i.getTown().getTownName());
                }
            }
            return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        return null;
    }
}
