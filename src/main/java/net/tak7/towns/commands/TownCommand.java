package net.tak7.towns.commands;

import net.tak7.api.PlayerUtils;
import net.tak7.towns.PlayerTowns;
import net.tak7.towns.objects.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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

@SuppressWarnings({"NullableProblems", "ConstantConditions"})
public class TownCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Town town = Town.getTownFromPlayer(player);
            Rank rank = null;
            if (town != null) {
                rank = town.getRank(player.getUniqueId());
            }

            if (args.length == 3) {
                // send command
                if (args[0].equalsIgnoreCase("send")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // check rank
                    if (rank.getOrder() > 1) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                        return true;
                    }

                    // check town is legit
                    String sendTownName = args[1];
                    Town sendTown = Town.getTownFromName(sendTownName);
                    if (sendTown == null) {
                        player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-town-not-exists")));
                        return true;
                    }

                    double amount = -1.0d;
                    // check money is legit
                    try {
                        amount = Double.parseDouble(args[2]);
                        if (amount < 0) {
                            throw new NumberFormatException("");
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "You need to enter a real non-negative number!");
                        return true;
                    }

                    // send the money
                    if (town.getTownMoney() - amount >= 0.0d) {
                        town.sendMoney(sendTown, amount);
                        player.sendMessage(
                                CC.tr(PlayerTowns.mainConfig.cfg().getString("message-send-town-money")
                                                .replace("%town%", town.getTownName())
                                                .replace("%amount%", String.valueOf(amount))
                                                .replace("%currency%", PlayerTowns.CURRENCY_NAME)));
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have enough money!");
                        return true;
                    }
                }
            } else if (args.length == 2) {
                // create command
                if (args[0].equalsIgnoreCase("create")) {
                    if (town != null) {
                        player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-already-in-town")));
                        return true;
                    }

                    String townName = args[1];
                    if (!(isAlphanumeric(townName) && townName.length() <= 10)) {
                        player.sendMessage(ChatColor.RED + "The name needs to be alphanumeric, and have a length less than or equal to 10 digits");
                        return true;
                    }

                    if (Town.getTownFromName(townName) != null) {
                        player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-town-exists")));
                        return true;
                    }

                    if ((Money.getMoney(player.getUniqueId()) - PlayerTowns.mainConfig.cfg().getDouble("town-start-amount")) < 0) {
                        player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-player-insufficient-funds")));
                        return true;
                    }

                    Town newTown = Town.createTown(townName, player);
                    PlayerTowns.towns.add(newTown);
                    player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-town-created").replace("%town%", newTown.getTownName())));
                    Money.subtractMoney(player.getUniqueId(), PlayerTowns.mainConfig.cfg().getDouble("town-start-amount"));

                } else if (args[0].equalsIgnoreCase("invite")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // check rank
                    if (rank.getOrder() > 1) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                        return true;
                    }

                    // check if player is legit
                    String playerName = args[1];
                    Player invited = null;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().equalsIgnoreCase(playerName)) {
                            invited = p;
                        }
                    }

                    if (invited == null) {
                        player.sendMessage(ChatColor.RED + "That player is not online!");
                        return true;
                    }

                    new Invite(invited, town);
                    invited.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has invited you to join " + ChatColor.YELLOW + town.getTownName() + ChatColor.GREEN + ". Do " + ChatColor.YELLOW + "/join " + town.getTownName() + ChatColor.GREEN + " to join it."); //config

                } else if (args[0].equalsIgnoreCase("shop")) {
                    // check if town real
                    String name = args[1];
                    Town getFrom = Town.getTownFromName(name);
                    if (getFrom == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlayerTowns.mainConfig.cfg().getString("message-town-not-exists")));
                        return true;
                    }
                    // open shop
                    getFrom.openShop(player);
                } else if (args[0].equalsIgnoreCase("promote")) {
                    String playerName = args[1];
                    String uuid = PlayerUtils.getUUIDFromName(playerName, false);

                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // check rank
                    if (rank == null) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                        return true;
                    }

                    if (rank.getOrder() > 0) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
                        return true;
                    }

                    if (uuid == null) {
                        sender.sendMessage(ChatColor.RED + "That player does not exist!");
                        return true;
                    }

                    if (UUID.fromString(uuid).compareTo(player.getUniqueId()) == 0) {
                        player.sendMessage(ChatColor.RED + "You cannot promote yourself!");
                        return true;
                    }

                    for (UUID u : town.getMembers().keySet()) {
                        if (u.compareTo(UUID.fromString(uuid)) == 0) {
                            town.promotePlayer(u);
                            sender.sendMessage(ChatColor.GREEN + "Successfully promoted " + playerName);
                            break;
                        }
                    }
                } else if (args[0].equalsIgnoreCase("deposit")) {
                    // check money is legit
                    double amount = -1.0d;
                    try {
                        amount = Double.parseDouble(args[1]);
                        if (amount < 0) {
                            throw new NumberFormatException("");
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "You need to enter a real non-negative number!");
                        return true;
                    }

                    // check player has enough to deposit
                    if (Money.getMoney(player.getUniqueId()) < amount) {
                        player.sendMessage(ChatColor.RED + "You don't have enough money to deposit!");
                        return true;
                    }

                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // execute
                    town.setTownMoney(town.getTownMoney() + amount);
                    Money.setMoney(player.getUniqueId(), Money.getMoney(player.getUniqueId()) - amount);
                    player.sendMessage(ChatColor.GREEN + "You successfully deposited " + amount + " to " + town.getTownName());
                } else if (args[0].equalsIgnoreCase("withdraw")) {
                    // check money is legit
                    double amount = -1.0d;
                    try {
                        amount = Double.parseDouble(args[1]);
                        if (amount < 0) {
                            throw new NumberFormatException("");
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "You need to enter a real non-negative number!");
                        return true;
                    }

                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // check player has enough money
                    if (town.getTownMoney() < amount) {
                        player.sendMessage(ChatColor.RED + "Your town doesn't have enough money to withdraw!");
                        return true;
                    }

                    // execute
                    town.setTownMoney(town.getTownMoney() - amount);
                    Money.setMoney(player.getUniqueId(), Money.getMoney(player.getUniqueId()) + amount);
                    player.sendMessage(ChatColor.GREEN + "You successfully withdrew " + amount + " from " + town.getTownName());
                } else {
                    sendHelp(sender);
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("leave")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // remove player from town
                    town.removePlayer(player.getUniqueId());
                    player.sendMessage(CC.tr(PlayerTowns.mainConfig.cfg().getString("message-town-left").replace("%town%", town.getTownName())));

                } else if (args[0].equalsIgnoreCase("list")) {
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Towns:");
                    int count = 0;
                    for (Town t : PlayerTowns.towns) {
                        if (count == 7) {
                            break;
                        }
                        player.sendMessage(ChatColor.YELLOW + t.getTownName());
                        count += 1;
                    }
                } else if (args[0].equalsIgnoreCase("shop")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    town.openShop(player);
                } else if (args[0].equalsIgnoreCase("info")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }

                    // show town information
                    sender.sendMessage("§2§l§m=============§r§e Town Info §e §2§l§m==============");
                    sender.sendMessage("§aTown Name: " + town.getTownName());
                    sender.sendMessage("§aTown Value: " + town.getTownMoney() + " " + PlayerTowns.CURRENCY_NAME);
                    sender.sendMessage("§aTown Role: " + town.getRank(player.getUniqueId()));
                    StringBuilder names = new StringBuilder();
                    for (UUID member : town.getMembers().keySet()) {
                        names.append(PlayerUtils.getNameFromUUID(member.toString(), false));
                        names.append(" (");
                        names.append(town.getRank(member).getName());
                        names.append("), ");
                    }
                    sender.sendMessage("§aMembers (" + town.getMembers().size() + "): " + names.substring(0, names.length() - 2));
                    sender.sendMessage("§2§l§m====================================");
                } else if (args[0].equalsIgnoreCase("disband")) {
                    // check if player is in a town
                    if (town == null) {
                        player.sendMessage(ChatColor.RED + "You need to be in a town to do this!");
                        return true;
                    }
                    if (rank.getOrder() <= 1) {
                        PlayerTowns.towns.remove(town);
                        sender.sendMessage(ChatColor.GREEN + "Town has been disbanded successfully!");
                    }
                } else {
                    sendHelp(player);
                }
            } else {
                sendHelp(sender);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command is only for players!");
        }
        return true;
    }

    private boolean isAlphanumeric(String check) {
        return StringUtils.isAlphanumeric(check);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§2§l§m=============§r§e Towns Help §e §2§l§m=============");
        sender.sendMessage("§a/town: §7Base command");
        sender.sendMessage("§a   create <name>: §7Create a town with the name");
        sender.sendMessage("§a   invite <name>: §7Invite a player with the name");
        sender.sendMessage("§a   shop [<name>]: §7Open your town inventory (or another town's to buy things!)");
        sender.sendMessage("§a   send <town> <amount>: §7Send money to another town");
        sender.sendMessage("§a   list: §7List a few towns");
        sender.sendMessage("§a   info: §7See the information of your town");
        sender.sendMessage("§a   promote <player>: §7Promote a player");
        sender.sendMessage("§a   withdraw <amount>: §7Withdraw money from your town");
        sender.sendMessage("§a   deposit <amount>: §7Deposit money to your town");
        sender.sendMessage("§a   disband: §7Disband your town");
        sender.sendMessage("§a   leave: §7Leave your town");
        sender.sendMessage("§2§l§m====================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Town town = Town.getTownFromPlayer(player);
            Rank rank = town != null ? town.getRank(player.getUniqueId()) : null;

            List<String> argList = new ArrayList<>();

            if (args.length == 1 && sender.hasPermission("cmd.towns.player")) {
                if (Money.getMoney(player.getUniqueId()) >= PlayerTowns.mainConfig.cfg().getDouble("town-start-amount")) {
                    argList.add("create");
                }

                argList.add("shop");
                argList.add("list");

                if (town != null) {
                    argList.add("withdraw");
                    argList.add("deposit");
                    argList.add("invite");
                    argList.add("leave");
                    argList.add("info");

                    if (rank.getOrder() <= 1) {
                        argList.add("send");

                    }
                    if (rank.getOrder() == 0) {
                        argList.add("promote");
                        argList.add("disband");
                    }
                }
                return argList.stream().filter(a -> a.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 2 && sender.hasPermission("cmd.towns.player")) {
                if (args[0].equalsIgnoreCase("create")) {
                    argList.add("<town name>");
                } else if (args[0].equalsIgnoreCase("join")) {
                    for (Town t : PlayerTowns.towns) {
                        argList.add(t.getTownName());
                    }
                } else if (args[0].equalsIgnoreCase("shop")) {
                    for (Town t : PlayerTowns.towns) {
                        argList.add(t.getTownName());
                    }
                }
                if (rank != null) {
                    if (rank.getOrder() <= 1) {
                        if (args[0].equalsIgnoreCase("send")) {
                            for (Town t : PlayerTowns.towns) {
                                argList.add(t.getTownName());
                            }
                        } else if (args[0].equalsIgnoreCase("withdraw")) {
                            argList.add("<amount>");
                        } else if (args[0].equalsIgnoreCase("deposit")) {
                            argList.add("<amount>");
                        } else if (args[0].equalsIgnoreCase("invite")) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p != player) {
                                    argList.add(p.getName());
                                }
                            }
                        }
                        if (rank.getOrder() < 1) {
                            if (args[0].equalsIgnoreCase("promote")) {
                                for (UUID mem : town.getMembers().keySet()) {
                                    argList.add(PlayerUtils.getNameFromUUID(mem.toString(), true));
                                }
                            }
                        }
                    }
                }

                return argList.stream().filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }

            if (args.length == 3 && sender.hasPermission("cmd.towns.player")) {
                if (args[0].equalsIgnoreCase("send") && (rank != null && rank.getOrder() <= 1)) {
                    argList.add("<amount>");
                }

                return argList.stream().filter(a -> a.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
            return argList;
        }
        return null;
    }
}