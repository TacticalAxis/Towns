package net.tak7.towns.listeners;

import net.tak7.api.FastBoard;
import net.tak7.towns.PlayerTowns;
import net.tak7.towns.objects.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ServerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (!(PlayerTowns.money.containsKey(player.getUniqueId()))) {
            PlayerTowns.money.put(e.getPlayer().getUniqueId(), 0.0d);
        }

        FastBoard board = new FastBoard(player);
        String title = PlayerTowns.mainConfig.cfg().getString("scoreboard-title");
        title = CC.tr(title != null ? title : ChatColor.GREEN + "Town Scoreboard");
        board.updateTitle(title);
        PlayerTowns.boards.add(board);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        FastBoard remove = null;
        for (FastBoard board : PlayerTowns.boards) {
            if (board.getPlayer() == e.getPlayer()) {
                remove = board;
                break;
            }
        }
        if (remove != null) {
            remove.delete();
            PlayerTowns.boards.remove(remove);
        }
    }
}