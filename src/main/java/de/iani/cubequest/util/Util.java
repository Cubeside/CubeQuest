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

    public static String capitalize(String s, boolean replaceUnderscores) {
        char[] cap = s.toCharArray();
        boolean lastSpace = true;
        for (int i = 0; i < cap.length; i++) {
            if (cap[i] == '_') {
                if (replaceUnderscores) {
                    cap[i] = ' ';
                }
                lastSpace = true;
            } else if (cap[i] >= '0' && cap[i] <= '9') {
                lastSpace = true;
            } else {
                if (lastSpace) {
                    cap[i] = Character.toUpperCase(cap[i]);
                } else {
                    cap[i] = Character.toLowerCase(cap[i]);
                }
                lastSpace = false;
            }
        }
        return new String(cap);
    }

}
