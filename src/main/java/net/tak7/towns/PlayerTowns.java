package net.tak7.towns;

import net.tak7.api.CustomConfiguration;
import net.tak7.api.FastBoard;
import net.tak7.towns.commands.*;
import net.tak7.towns.listeners.InventoryEvents;
import net.tak7.towns.listeners.ServerEvents;
import net.tak7.towns.objects.CC;
import net.tak7.towns.objects.Invite;
import net.tak7.towns.objects.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class PlayerTowns extends JavaPlugin {

    public static String CURRENCY_NAME;
    public static List<Town> towns;
    public static HashMap<UUID, Double> money;
    public static List<Invite> invitations;
    public static ArrayList<FastBoard> boards;
    public static CustomConfiguration mainConfig;
    public static CustomConfiguration townConfig;
    public static CustomConfiguration playerConfig;
    private static PlayerTowns instance;

    public static PlayerTowns getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        mainConfig = new CustomConfiguration("config.yml", this);
        townConfig = new CustomConfiguration("towns.yml", this);
        playerConfig = new CustomConfiguration("players.yml", this);

        CURRENCY_NAME = mainConfig.cfg().getString("currency-name");

        towns = new ArrayList<>();
        boards = new ArrayList<>();
        money = new HashMap<>();
        invitations = new ArrayList<>();

        registerEvents();

        writeMaterials();

        registerCommands();

        loadMoney();

        loadTowns();

        startRunnables();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        Town.saveAllTownData();

        for (String key : playerConfig.cfg().getKeys(false)) {
            playerConfig.cfg().set(key, null);
        }

        for (UUID u : money.keySet()) {
            playerConfig.cfg().set(u.toString(), money.get(u));
        }

        playerConfig.saveConfiguration();

        instance = null;
    }

    private void loadMoney() {
        for (String entry : playerConfig.cfg().getKeys(false)) {
            money.put(UUID.fromString(entry), Double.valueOf(playerConfig.cfg().getString(entry)));
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new InventoryEvents(), this);
        getServer().getPluginManager().registerEvents(new ServerEvents(), this);
    }

    private void writeMaterials() {
        CustomConfiguration materials = new CustomConfiguration("materials.yml", this);
        List<String> mList = new ArrayList<>();
        for (Material m : Material.values()) {
            mList.add(m.name());
        }
        materials.cfg().set("materials", mList);
        materials.saveConfiguration();
    }

    private void registerCommands() {
        PluginCommand cmd = getCommand("town");
        if (cmd != null) {
            cmd.setExecutor(new TownCommand());
        }

        PluginCommand cmd2 = getCommand("sellhand");
        if (cmd2 != null) {
            cmd2.setExecutor(new SellhandCommand());
        }

        PluginCommand cmd3 = getCommand("playertowns");
        if (cmd3 != null) {
            cmd3.setExecutor(new PlayerTownCommand());
        }

        PluginCommand cmd4 = getCommand("join");
        if (cmd4 != null) {
            cmd4.setExecutor(new JoinCommand());
        }

        PluginCommand cmd5 = getCommand("balance");
        if (cmd5 != null) {
            cmd5.setExecutor(new BalanceCommand());
        }
    }

    private void startRunnables() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (FastBoard board : boards) {
                    updateBoard(board);
                }
            }
        }.runTaskTimer(this, 1L, 10L);

        new BukkitRunnable() {
            @Override
            public void run() {
                Town.saveAllTownData();
            }
        }.runTaskTimer(this, 20L, 400L);
    }

    private void loadTowns() {
        for (String s : townConfig.cfg().getKeys(false)) {
            UUID townID = UUID.fromString(s);
            Town town = Town.loadTown(townID);
            towns.add(town);
        }
    }

    private void updateBoard(FastBoard board) {
        List<String> newLines = new ArrayList<>();
        String colour = mainConfig.cfg().getString("scoreboard-entry-colour");
        if (colour == null) {
            colour = ChatColor.GREEN + "";
        } else {
            colour = CC.tr(colour);
        }
        int count = 1;
        for (Town town : Town.getOrderedTowns()) {
            if (Town.getTownFromPlayer(board.getPlayer()) == town) {
                newLines.add(colour + count + ": " + town.getTownMoney() + " - " + town.getTownName() + ChatColor.GRAY + " (You)");
            } else {
                newLines.add(colour + count + ": " + town.getTownMoney() + " - " + town.getTownName());
            }
            count += 1;
        }
        while (newLines.size() < 8) {
            newLines.add(count + ": " + ChatColor.GREEN + "--");
            count += 1;
        }
        board.updateLines(newLines);
    }
}