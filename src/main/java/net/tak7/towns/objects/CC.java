package net.tak7.towns.objects;

import net.tak7.towns.PlayerTowns;
import org.bukkit.ChatColor;

public class CC {

    public static String tr(String toTranslate) {
        return ChatColor.translateAlternateColorCodes('&', toTranslate);
    }

    public static String getMessage(String type) {
        return CC.tr(PlayerTowns.mainConfig.cfg().getString("message-" + type));
    }
}
