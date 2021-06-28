package net.tak7.towns.objects;

import net.tak7.api.PlayerUtils;
import net.tak7.towns.PlayerTowns;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Town {

    private final UUID townID;
    private String townName;
    private double townMoney;
    private Inventory communityChest;
    private HashMap<UUID, Rank> members;


    public Town(UUID townID, String townName, double townMoney, Inventory communityChest, HashMap<UUID, Rank> members) {
        this.townID = townID;
        this.townName = townName;
        this.townMoney = townMoney;
        this.communityChest = communityChest;
        this.members = members;
    }

    public static Town createTown(String name, Player sender) {
        List<String> alreadyMade = new ArrayList<>();
        for (Town town : PlayerTowns.towns) {
            alreadyMade.add(town.getTownID().toString());
        }
        String newUUID = UUID.randomUUID().toString();
        while (alreadyMade.contains(newUUID)) {
            newUUID = UUID.randomUUID().toString();
        }

        HashMap<UUID, Rank> members = new HashMap<>();
        members.put(sender.getUniqueId(), Rank.ADMIN);

        return new Town(UUID.fromString(newUUID), name, PlayerTowns.mainConfig.cfg().getDouble("town-start-amount"), Bukkit.createInventory(null, 54, ChatColor.GREEN + name + " [Shop]"), members);
    }

    public static Town loadTown(UUID uuid) {
        ConfigurationSection cfg = PlayerTowns.townConfig.cfg().getConfigurationSection(uuid.toString());
        if (cfg != null) {
            String townName = cfg.getString("town-name");
            double townMoney = cfg.getDouble("town-money");

            Inventory items = Bukkit.createInventory(null, 54, ChatColor.GREEN + townName + " [Shop]");
            for (String entry : cfg.getStringList("community-chest")) {
                if(getPrice(entry.split(";")[0].toUpperCase()) != -1.0d) {
                    try {
                        String itemName = entry.split(";")[0];
                        int amount = Integer.parseInt(entry.split(";")[1]);
                        double itemPrice = getPrice(itemName);
                        boolean valid = false;
                        for (Material material : Material.values()) {
                            if (itemName.equalsIgnoreCase(material.name())) {
                                valid = true;
                                break;
                            }
                        }

                        Material material = Material.getMaterial(itemName);
                        if (valid && (material != null)) {
                            for (int i = 0; i < amount; i++) {
                                ItemStack toAdd = new ItemStack(material, 1);
                                ItemMeta im = toAdd.getItemMeta();
                                if (im != null) {
                                    List<String> lore = new ArrayList<>();
                                    lore.add(ChatColor.GREEN + "Price: " + itemPrice);
                                    im.setLore(lore);
                                    toAdd.setItemMeta(im);
                                    items.addItem(toAdd);
                                }
                            }
                        } else {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load entry: " + ChatColor.GOLD + entry + ChatColor.RED + " because item " + ChatColor.GOLD + itemName + ChatColor.RED + " does not exist!");
                        }
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load entry: " + ChatColor.GOLD + entry + ChatColor.RED + " because there was an error!");
                        e.printStackTrace();
                    }
                }
            }

            HashMap<UUID, Rank> members = new HashMap<>();
            for (String entry : cfg.getStringList("members")) {
                members.put(UUID.fromString(entry.split(";")[0]), Rank.valueOf(entry.split(";")[1]));
            }

            return new Town(uuid, townName, townMoney, items, members);
        } else {
            return null;
        }
    }

    public static void saveAllTownData() {
        ConfigurationSection cfg = PlayerTowns.townConfig.cfg();
        for (String c : PlayerTowns.townConfig.cfg().getKeys(false)) {
            cfg.set(c, null);
        }
        for (Town town : PlayerTowns.towns) {
            ConfigurationSection thisTown = cfg.createSection(town.getTownID().toString());
            thisTown.set("town-name", town.getTownName());
            thisTown.set("town-money", town.getTownMoney());
            List<String> convertedItems = new ArrayList<>();
            for (ItemStack item : town.getCommunityChest().getContents()) {
                if (item != null) {
                    convertedItems.add(item.getType().name() + ";" + item.getAmount());
                }
            }
            thisTown.set("community-chest", convertedItems);

            List<String> convertedPlayers = new ArrayList<>();
            for (UUID player : town.getMembers().keySet()) {
                convertedPlayers.add(player.toString() + ";" + town.getRank(player).getName().toUpperCase());
            }
            thisTown.set("members", convertedPlayers);
        }
        PlayerTowns.townConfig.saveConfiguration();
    }

    public UUID getTownID() {
        return townID;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public double getTownMoney() {
        return townMoney;
    }

    public void setTownMoney(double townMoney) {
        this.townMoney = townMoney;
    }

    public Inventory getCommunityChest() {
        return communityChest;
    }

    public void setCommunityChest(Inventory communityChest) {
        this.communityChest = communityChest;
    }

    public HashMap<UUID, Rank> getMembers() {
        return members;
    }

    public void setMembers(HashMap<UUID, Rank> members) {
        this.members = members;
    }

    public List<Player> getOnlineMembers() {
        if (members != null) {
            List<Player> current = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (UUID uuid : members.keySet()) {
                    if (uuid.compareTo(player.getUniqueId()) == 0) {
                        current.add(player);
                    }
                }
            }
            return current;
        } else {
            return null;
        }
    }

    public void removePlayer(UUID uuid) {
        UUID remove = null;
        for (UUID entry : members.keySet()) {
            if (entry.compareTo(uuid) == 0) {
                remove = entry;
            }
        }
        if (remove != null) {
            members.remove(remove);
        }

        // delete town if no members left
        Town town = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (town.getMembers().keySet().size() == 0) {
                    PlayerTowns.towns.remove(town);
                } else {
                    boolean hasAdmin = false;
                    for (UUID mem : town.getMembers().keySet()) {
                        if (town.getMembers().get(mem) == Rank.ADMIN) {
                            hasAdmin = true;
                        }
                    }
                    if (!hasAdmin) {
                        for (UUID mem : town.getMembers().keySet()) {
                            // make absolutely sure they are admin
                            town.promotePlayer(mem);
                            town.promotePlayer(mem);
                            town.promotePlayer(mem);
                            town.sendMessage(ChatColor.GREEN + "Your town administrator left! " + PlayerUtils.getNameFromUUID(mem.toString(), false) + " was promoted to administrator!");
                            break;
                        }
                    }
                }
            }
        }.runTaskLater(PlayerTowns.getInstance(), 20L);
    }

    public Rank getRank(UUID player) {
        return getMembers().get(player);
    }

    public void addMoney(double amount) {
        try {
            double a = townMoney + amount; // just to test for invalid values
            townMoney += amount;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error adding money: " + amount + " to: " + getTownName());
            e.printStackTrace();
        }
    }

    public void removeMoney(double amount) {
        try {
            double a = townMoney - amount; // just to test for invalid values
            if (a >= 0) {
                townMoney -= amount;
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error removing money: " + amount + " from: " + getTownName());
            e.printStackTrace();
        }
    }

    public void sendMoney(Town town, double amount) {
        try {
            removeMoney(amount);
            town.addMoney(amount);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error sending " + amount + " from " + getTownName() + " to: " + town.getTownName());
        }

    }

    public void sendMessage(String message) {
        for (Player player : getOnlineMembers()) {
            player.sendMessage(ChatColor.GREEN + townName + " >> " + ChatColor.YELLOW + message);
        }
    }

    public void openShop(Player player) {
        player.openInventory(getCommunityChest());
    }

    public static boolean isShop(InventoryView inventory) {
        return inventory.getTitle().split(" ")[inventory.getTitle().split(" ").length - 1].equals("[Shop]");
    }

    public static Town getShop(InventoryView inventory) {
        for (Town town : PlayerTowns.towns) {
            if (town.getTownName().equals(inventory.getTitle().split(" ")[0].replace(ChatColor.GREEN + "", ""))) {
                return town;
            }
        }
        return null;
    }

    public static Town getTownFromPlayer(Player player) {
        for(Town town : PlayerTowns.towns) {
            for (UUID uuid : town.getMembers().keySet()) {
                if (uuid.compareTo(player.getUniqueId()) == 0) {
                    return town;
                }
            }
        }
        return null;
    }

    public static Town getTownFromName(String name) {
        for(Town town : PlayerTowns.towns) {
            if (town.getTownName().trim().equals(name)) {
                return town;
            }
        }
        return null;
    }

    public static double getPrice(String itemName) {
        List<String> sellable = PlayerTowns.mainConfig.cfg().getStringList("sellable-items");
        for (String name : sellable) {
            if (name.split(";")[0].equalsIgnoreCase(itemName.trim())) {
                return Double.parseDouble(name.split(";")[1]);
            }
        }
        return -1.0d;
    }

    public static boolean inventoryIsShop(Inventory inventory) {
        for (Town town : PlayerTowns.towns) {
            if (town.getCommunityChest() == inventory) {
                return true;
            }
        }
        return false;
    }

    public static List<Town> getOrderedTowns() {
        List<Town> toReturn = new ArrayList<>(PlayerTowns.towns);
        toReturn.sort(Comparator.comparing(Town::getTownMoney).reversed());
        return toReturn;
    }

    public void promotePlayer(UUID uuid) {
        Rank current = members.get(uuid);
        if (members.containsKey(uuid)) {
            members.replace(uuid, Rank.getNext(current));
        }
    }

    public boolean playerInvited(Player player) {
        for (Invite i : PlayerTowns.invitations) {
            if (player == i.getPlayer()) {
                return true;
            }
        }
        return false;
    }
}