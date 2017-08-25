package de.iani.cubequest.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {

    public static ItemStack[] shrinkItemStack(ItemStack[] items) {
        List<ItemStack> stackList = Arrays.asList(items);
        stackList.removeIf(item -> item == null || item.getAmount() == 0 || item.getType() == Material.AIR);
        items = stackList.toArray(new ItemStack[0]);
        return items;
    }

    public static boolean isEmpty(ItemStack[] items) {
        for (ItemStack item: items) {
            if (item != null && item.getAmount() > 0 && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    public static String toNiceString(ItemStack item) {
        return item.toString(); //TODO
    }

}
