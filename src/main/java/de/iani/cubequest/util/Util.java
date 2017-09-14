package de.iani.cubequest.util;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;

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

    public static <T> T randomElement(List<T> list, Random ran) {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }

        return list.get(ran.nextInt(list.size()));
    }

}
