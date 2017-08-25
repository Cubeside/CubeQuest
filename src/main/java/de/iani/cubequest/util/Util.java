package de.iani.cubequest.util;

import java.util.Locale;

import org.bukkit.entity.EntityType;

public class Util {

    @SuppressWarnings("deprecation")
    public static EntityType matchEnum(String from) {
        EntityType res = EntityType.valueOf(from.toUpperCase(Locale.ENGLISH));
        if (res != null) {
            return res;
        }
        res = EntityType.fromName(from);
        if (res != null) {
            return res;
        }
        try {
            return EntityType.fromId(Integer.parseInt(from));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
