package net.tak7.towns.events;

import net.tak7.towns.objects.Town;
import net.tak7.towns.objects.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BuyItemEvent  extends Event implements Cancellable {

    private final Player player;
    private final Town fromTown;
    private final Town toTown;
    private final ItemStack item;
    private final Transaction transaction;
    private final double price;
    private boolean isCancelled;

    public BuyItemEvent(Player player, Town fromTown, Town toTown, ItemStack item, Transaction transaction, double price) {
        this.player = player;
        this.fromTown = fromTown;
        this.toTown = toTown;
        this.item = item;
        this.transaction = transaction;
        this.price = price;
        this.isCancelled = false;

        player.sendMessage(Transaction.getBuyMessage(transaction, item.getType().name(), price));
    }

    public Player getPlayer() {
        return player;
    }

    public Town getFromTown() {
        return fromTown;
    }

    public Town getToTown() {
        return toTown;
    }

    public ItemStack getItem() {
        return item;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public double getPrice() {
        return price;
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