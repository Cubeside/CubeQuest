package de.iani.cubequest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {

    public static ItemStack[] shrinkItemStack(ItemStack[] items) {
        List<ItemStack> stackList = new ArrayList<ItemStack>(Arrays.asList(items));
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

    public static ItemStack getMysteriousSpellBook() {
        ItemStack mysticalSpellBook = new ItemStack(Material.BOOK);
        ItemMeta meta = mysticalSpellBook.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Mysteriöses Zauberbuch");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Lässt zufällige", ChatColor.GOLD + "Items erscheinen!"));
        mysticalSpellBook.setItemMeta(meta);
        mysticalSpellBook.addUnsafeEnchantment(Enchantment.LUCK, 3);
        return mysticalSpellBook;
    }

}
