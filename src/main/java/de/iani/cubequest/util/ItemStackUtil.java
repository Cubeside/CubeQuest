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

    public static ItemStack[] addItem(ItemStack add, ItemStack[] to) {
        int amountToAdd = add.getAmount();
        for (ItemStack stack: to) {
            if (stack.isSimilar(add)) {
                for (; stack.getAmount() < stack.getMaxStackSize() && amountToAdd > 0; amountToAdd --) {
                    stack.setAmount(stack.getAmount() + 1);
                }
            }
        }
        if (amountToAdd <= 0) {
            return to;
        }
        int i = to.length;
        to = Arrays.copyOf(to, to.length + (int) Math.ceil(amountToAdd / add.getMaxStackSize()));
        for (; amountToAdd > 0; i++) {
            ItemStack toAdd = add.clone();
            toAdd.setAmount(Math.min(amountToAdd, add.getMaxStackSize()));
            to[i] = toAdd;
            amountToAdd -= toAdd.getAmount();
        }

        return to;
    }

}
