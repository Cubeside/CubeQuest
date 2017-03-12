package de.iani.cubequest.quests;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.iani.cubequest.CubeQuest;
import net.md_5.bungee.api.ChatColor;

public class Reward {

    private double cubes;
    private ItemStack[] items;

    public Reward() {
        cubes = 0;
        items = new ItemStack[0];
    }

    public Reward(double cubes) {
        this.cubes = cubes;
        items = new ItemStack[0];
    }

    public Reward(ItemStack[] items) {
        cubes = 0;
        this.items = items;

    }

    public Reward(double cubes, ItemStack[] items) {
        this.cubes = cubes;
        this.items = items;
    }

    public double getCubes() {
        return cubes;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public Reward add(Reward other) {
        ItemStack newItems[] = new ItemStack[items.length + other.items.length];
        for (int i=0; i<items.length; i++) newItems[i] = items[i];
        for (int i=0; i<other.items.length; i++) newItems[i+items.length] = other.items[i];

        return new Reward(cubes + other.cubes, newItems);
    }

    public boolean pay(Player player) {
        ItemStack[] playerInv = player.getInventory().getContents();
        playerInv = Arrays.copyOf(playerInv, 36);
        Inventory clonedPlayerInventory = Bukkit.createInventory(null, 36);
        clonedPlayerInventory.setContents(playerInv);

            int priceCount = items == null ? 0 : items.length;
            if (priceCount > 0) {
                ItemStack[] temp = new ItemStack[priceCount];
                for (int i = 0; i < priceCount; i++) {
                    temp[i] = items[i].clone();
                }
                if (!clonedPlayerInventory.addItem(temp).isEmpty()) {
                    CubeQuest.sendWarningMessage(player, "Du hast nicht genügend Platz in deinem Inventar!");
                    player.updateInventory();
                    return false;
                }
            }

            if (priceCount > 0) {
                ItemStack[] temp = new ItemStack[priceCount];
                for (int i = 0; i < priceCount; i++) {
                    temp[i] = items[i].clone();
                }
                player.getInventory().addItem(temp);
                for (ItemStack stack : items) {
                    StringBuilder t = new StringBuilder("  ");
                    if (stack.getAmount() > 1) {
                        t.append(stack.getAmount()).append(" ");
                    }
                    t.append(CubeQuest.capitalize(stack.getType().name(), true));
                    if (stack.getDurability() > 0) {
                        t.append(':').append(stack.getDurability());
                    }
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        t.append(" (").append(meta.getDisplayName()).append(ChatColor.YELLOW).append(")");
                    }
                    CubeQuest.sendMessage(player, t.toString());
                }
            }
            CubeQuest.sendMessage(player, ChatColor.GRAY + "Du hast eine Belohnung abgeholt!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            return true;
    }

}
