package de.iani.cubequest.quests;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;

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
        if (!hasSpace(player)) {
            CubeQuest.sendWarningMessage(player, "Du hast nicht genug Platz in deinem Inventar, um diese Belohnung zu erhalten.");
            return false;
        }
        //TODO: Items übertragen.
        //TODO: Geld überweisen.

        return true;
    }

    public boolean hasSpace(Player player) {
        //TODO
        return true;
    }

}
