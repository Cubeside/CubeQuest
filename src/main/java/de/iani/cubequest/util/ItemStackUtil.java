package de.iani.cubequest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

public class ItemStackUtil {
    
    public static final Comparator<ItemStack> ITEMSTACK_COMPARATOR = (i1, i2) -> {
        int result = i1.getType().compareTo(i2.getType());
        if (result != 0) {
            return result;
        }
        
        result = i1.getDurability() - i2.getDurability();
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
    
    public static ItemStack[] shrinkItemStack(ItemStack[] items) {
        List<ItemStack> stackList = new ArrayList<>(Arrays.asList(items));
        stackList.removeIf(
                item -> item == null || item.getAmount() == 0 || item.getType() == Material.AIR);
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
    
    public static ItemStack[] deepCopy(ItemStack[] of) {
        ItemStack[] result = new ItemStack[of.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = of[i] == null ? null : of[i].clone();
        }
        return result;
    }
    
    public static String toNiceString(ItemStack[] items) {
        return toNiceString(items, "");
    }
    
    public static String toNiceString(ItemStack[] items, String colorPrefix) {
        TreeMap<ItemStack, Integer> itemMap = new TreeMap<>((item1, item2) -> {
            if (item1.isSimilar(item2)) {
                return 0;
            }
            
            int result = item1.getType().compareTo(item2.getType());
            if (result != 0) {
                return result;
            }
            
            result = item1.getDurability() - item2.getDurability();
            if (result != 0) {
                return result;
            }
            
            if (item1.getItemMeta().hasDisplayName()) {
                if (item2.getItemMeta().hasDisplayName()) {
                    result = item1.getItemMeta().getDisplayName()
                            .compareTo(item2.getItemMeta().getDisplayName());
                } else {
                    return 1;
                }
            } else {
                if (item2.getItemMeta().hasDisplayName()) {
                    return -1;
                }
            }
            if (result != 0) {
                return result;
            }
            
            return item1.getItemMeta().toString().compareTo(item2.getItemMeta().toString());
        });
        
        Arrays.stream(items).filter(
                item -> item != null && item.getType() != Material.AIR && item.getAmount() > 0)
                .forEach(item -> itemMap.put(item,
                        item.getAmount() + (itemMap.containsKey(item) ? itemMap.get(item) : 0)));
        
        
        
        StringBuilder builder = new StringBuilder();
        int index = 0;
        
        for (ItemStack item: itemMap.keySet()) {
            int amount = itemMap.get(item);
            
            builder.append(ItemStackUtil.toNiceString(item, amount, colorPrefix));
            if (index + 1 < itemMap.size()) {
                if (index + 2 < itemMap.size()) {
                    builder.append(", ");
                } else {
                    builder.append(" und ");
                }
            }
            index++;
        }
        
        return builder.toString();
    }
    
    public static String toNiceString(ItemStack item) {
        return toNiceString(item, item.getAmount(), ChatColor.RESET.toString());
    }
    
    public static String toNiceString(ItemStack item, int amount) {
        return toNiceString(item, amount, ChatColor.RESET.toString());
    }
    
    public static String toNiceString(ItemStack item, String colorPrefix) {
        return toNiceString(item, item.getAmount(), colorPrefix);
    }
    
    public static String toNiceString(ItemStack item, int amount, String colorPrefix) {
        StringBuilder builder = new StringBuilder(colorPrefix);
        builder.append(amount).append(" ");
        ItemMeta meta = item.getItemMeta();
        
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
            Color color = armorMeta.getColor();
            // ignore "default" color:
            if (color.asRGB() != 0xA06540) {
                builder.append(ChatAndTextUtil.toNiceString(color)).append(" ");
            }
        } else if (meta instanceof SpawnEggMeta) {
            SpawnEggMeta eggMeta = (SpawnEggMeta) meta;
            builder.append(ChatAndTextUtil.capitalize(eggMeta.getSpawnedType().toString(), true))
                    .append(" ");
        }
        
        builder.append(ChatAndTextUtil.capitalize(item.getType().name(), true));
        
        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            PotionData data = potionMeta.getBasePotionData();
            builder.append(" of ").append(ChatAndTextUtil.capitalize(data.getType().name(), true));
            builder.append(data.isUpgraded() ? " II" : " I");
            if (data.isExtended()) {
                builder.append(" (verlängert)");
            }
            
            int index = 0;
            for (PotionEffect effect: potionMeta.getCustomEffects()) {
                builder.append((index + 1 < potionMeta.getCustomEffects().size()) ? ", " : " and ");
                builder.append(ChatAndTextUtil.capitalize(effect.getType().getName(), true))
                        .append(" ").append(ChatAndTextUtil.toRomanNumber(effect.getAmplifier()));
                if (!effect.getType().isInstant()) {
                    builder.append(" (")
                            .append(ChatAndTextUtil.formatTimespan(50 * effect.getDuration(), "",
                                    "", "", "", ":", ":", false, true))
                            .append(")");
                }
                index++;
            }
        }
        
        if (item.getDurability() > 0) {
            builder.append(':').append(item.getDurability());
        }
        
        if (meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta;
            boolean appended = false;
            
            if (meta.hasDisplayName()) {
                builder.append(" (\"").append(meta.getDisplayName()).append(colorPrefix)
                        .append('"');
                appended = true;
            } else if (bookMeta.hasTitle()) {
                builder.append(" (\"").append(bookMeta.getTitle()).append(colorPrefix).append('"');
                appended = true;
            }
            
            if (appended && bookMeta.hasAuthor()) {
                builder.append(" von ").append(bookMeta.getAuthor()).append(colorPrefix);
            }
            
            if (appended) {
                builder.append(")");
            }
        } else if (meta.hasDisplayName()) {
            builder.append(" (\"").append(meta.getDisplayName()).append(colorPrefix).append("\")");
        }
        
        Map<Enchantment, Integer> enchantments = new HashMap<>(meta.getEnchants());
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) meta;
            enchantments.putAll(enchMeta.getStoredEnchants());
        }
        
        if (!enchantments.isEmpty()) {
            builder.append(", verzaubert mit ");
            
            List<Enchantment> enchList = new ArrayList<>(enchantments.keySet());
            enchList.sort(
                    (e1, e2) -> ChatAndTextUtil.getName(e1).compareTo(ChatAndTextUtil.getName(e2)));
            
            int index = 0;
            for (Enchantment ench: enchList) {
                builder.append(ChatAndTextUtil.capitalize(ChatAndTextUtil.getName(ench), true));
                if (ench.getMaxLevel() > 1 || enchantments.get(ench) > 1) {
                    builder.append(" ")
                            .append(ChatAndTextUtil.toRomanNumber(enchantments.get(ench)));
                }
                if (index + 1 < enchantments.size()) {
                    if (index + 2 < enchantments.size()) {
                        builder.append(", ");
                    } else {
                        builder.append(" und ");
                    }
                }
                index++;
            }
        }
        
        return builder.toString();
    }
    
    public static String toNiceString(Material m) {
        return ChatAndTextUtil.capitalize(m.name(), true);
    }
    
    public static ItemStack getMysteriousSpellBook() {
        ItemStack mysticalSpellBook = new ItemStack(Material.BOOK);
        ItemMeta meta = mysticalSpellBook.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Mysteriöses Zauberbuch");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Lässt zufällige",
                ChatColor.GOLD + "Items erscheinen!"));
        mysticalSpellBook.setItemMeta(meta);
        mysticalSpellBook.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
        return mysticalSpellBook;
    }
    
    public static ItemStack[] addItem(ItemStack add, ItemStack[] to) {
        int amountToAdd = add.getAmount();
        for (ItemStack stack: to) {
            if (stack.isSimilar(add)) {
                for (; stack.getAmount() < stack.getMaxStackSize()
                        && amountToAdd > 0; amountToAdd--) {
                    stack.setAmount(stack.getAmount() + 1);
                }
            }
        }
        if (amountToAdd <= 0) {
            return to;
        }
        int i = to.length;
        to = Arrays.copyOf(to,
                to.length + (int) Math.ceil((double) amountToAdd / (double) add.getMaxStackSize()));
        for (; amountToAdd > 0; i++) {
            ItemStack toAdd = add.clone();
            toAdd.setAmount(Math.min(amountToAdd, add.getMaxStackSize()));
            to[i] = toAdd;
            amountToAdd -= toAdd.getAmount();
        }
        
        return to;
    }
    
}
