package net.tak7.towns.events;

import net.tak7.towns.objects.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class JoinTownEvent extends Event implements Cancellable {

    private Player player;
    private Town town;
    private boolean isCancelled;

    public JoinTownEvent(Player player, Town town) {
        this.player = player;
        this.town = town;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Town getTown() {
        return town;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        isCancelled = arg0;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}