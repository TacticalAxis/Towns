package net.tak7.towns.objects;

import net.tak7.towns.PlayerTowns;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Invite {

    private final Player player;
    private final Town town;
    private final String code;

    public Invite(Player player, Town town) {
        this.player = player;
        this.town = town;
        this.code = UUID.randomUUID().toString().split("-")[0];
        PlayerTowns.invitations.add(this);
    }

    public Player getPlayer() {
        return player;
    }

    public Town getTown() {
        return town;
    }

    public String getCode() {
        return code;
    }
}
