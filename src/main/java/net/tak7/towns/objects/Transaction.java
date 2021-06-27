package net.tak7.towns.objects;

import net.tak7.towns.PlayerTowns;
import org.bukkit.ChatColor;

public enum Transaction {
    SUCCESS,
    FAIL_INVALID_VALUE,
    FAIL_INSUFFICIENT_FUNDS,
    FAIL_UNKNOWN_ERROR;

    Transaction() {

    }

    public static String getMessage(Transaction transaction) {
        switch (transaction) {
            case SUCCESS:
                return ChatColor.GREEN + "Transaction Success";
            case FAIL_INVALID_VALUE:
                return ChatColor.RED + "Invalid Money Value!";
            case FAIL_INSUFFICIENT_FUNDS:
                return ChatColor.RED + "Insufficient Funds!";
            case FAIL_UNKNOWN_ERROR:
                return ChatColor.RED + "An unknown error occurred";
            default:
                return ChatColor.RED + "There was an internal error. Please contact NathanD256.";
        }
    }

    public static String getBuyMessage(Transaction transaction, String itemName, double itemPrice) {
        switch (transaction) {
            case SUCCESS:
                return ChatColor.GREEN + "You bought " + ChatColor.YELLOW + itemName + ChatColor.GREEN + " for " + ChatColor.YELLOW + itemPrice + " " + PlayerTowns.CURRENCY_NAME;
            case FAIL_INVALID_VALUE:
                return ChatColor.RED + "Invalid Money Value!";
            case FAIL_INSUFFICIENT_FUNDS:
                return ChatColor.RED + "Insufficient Funds!";
            case FAIL_UNKNOWN_ERROR:
                return ChatColor.RED + "An unknown error occurred";
            default:
                return ChatColor.RED + "There was an internal error. Please contact NathanD256.";
        }
    }
}