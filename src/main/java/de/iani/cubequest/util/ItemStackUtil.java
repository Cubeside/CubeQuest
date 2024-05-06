package de.iani.cubequest.util;

import java.util.Arrays;
import java.util.Comparator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {
    
    public static final Comparator<ItemStack> ITEMSTACK_COMPARATOR = (i1, i2) -> {
        int result = i1.getType().compareTo(i2.getType());
        if (result != 0) {
            return result;
        }
        
        return i1.toString().compareTo(i2.toString());
    };
    
    public static final Comparator<ItemStack[]> ITEMSTACK_ARRAY_COMPARATOR = (i1, i2) -> {
        ItemStack[] copy1 = i1.clone();
        ItemStack[] copy2 = i2.clone();
        
        Arrays.sort(copy1, ITEMSTACK_COMPARATOR);
        Arrays.sort(copy2, ITEMSTACK_COMPARATOR);
        
        int index1 = 0;
        int index2 = 0;
        ItemStack stack1 = null;
        ItemStack stack2 = null;
        
        for (;;) {
            int count1 = 0;
            for (; index1 < copy1.length; index1++) {
                ItemStack next = copy1[index1];
                if (stack1 == null || stack1.isSimilar(next)) {
                    count1 += next.getAmount();
                    stack1 = next;
                } else {
                    stack2 = null;
                    break;
                }
            }
            
            int count2 = 0;
            for (; index2 < copy2.length; index2++) {
                ItemStack next = copy2[index2];
                if (stack2 == null || stack2.isSimilar(next)) {
                    count2 += next.getAmount();
                    stack2 = next;
                } else {
                    stack2 = null;
                    break;
                }
            }
            
            int result = count1 - count2;
            if (result != 0) {
                return result;
            } else if (index1 >= copy1.length && index2 >= copy2.length) {
                return 0;
            }
        }
    };
    
    @Deprecated
    public static ItemStack getMysteriousSpellBook() {
        ItemStack mysticalSpellBook = new ItemStack(Material.BOOK);
        ItemMeta meta = mysticalSpellBook.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Mysteriöses Zauberbuch");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Lässt zufällige", ChatColor.GOLD + "Items erscheinen!"));
        mysticalSpellBook.setItemMeta(meta);
        mysticalSpellBook.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
        return mysticalSpellBook;
    }
    
    public static ItemStack[] addItem(ItemStack add, ItemStack[] to) {
        int amountToAdd = add.getAmount();
        for (ItemStack stack : to) {
            if (stack.isSimilar(add)) {
                for (; stack.getAmount() < stack.getMaxStackSize() && amountToAdd > 0; amountToAdd--) {
                    stack.setAmount(stack.getAmount() + 1);
                }
            }
        }
        if (amountToAdd <= 0) {
            return to;
        }
        int i = to.length;
        to = Arrays.copyOf(to, to.length + (int) Math.ceil((double) amountToAdd / (double) add.getMaxStackSize()));
        for (; amountToAdd > 0; i++) {
            ItemStack toAdd = add.clone();
            toAdd.setAmount(Math.min(amountToAdd, add.getMaxStackSize()));
            to[i] = toAdd;
            amountToAdd -= toAdd.getAmount();
        }
        
        return to;
    }
    
}
