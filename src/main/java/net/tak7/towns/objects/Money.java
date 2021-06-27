package net.tak7.towns.objects;

import net.tak7.towns.PlayerTowns;

import java.util.UUID;

public class Money {

    public static void addMoney(UUID player, double amount) {
        double original = PlayerTowns.money.get(player);
        PlayerTowns.money.remove(player);
        PlayerTowns.money.put(player, original + amount);
    }

    public static void subtractMoney(UUID player, double amount) {
        double original = PlayerTowns.money.get(player);
        PlayerTowns.money.remove(player);
        PlayerTowns.money.put(player, original - amount);
    }

    public static double getMoney(UUID player) {
        return PlayerTowns.money.get(player) != null ? PlayerTowns.money.get(player) : -1.0d;
    }

    public static void setMoney(UUID player, double amount) {
        PlayerTowns.money.remove(player);
        PlayerTowns.money.put(player, amount);
    }
}
