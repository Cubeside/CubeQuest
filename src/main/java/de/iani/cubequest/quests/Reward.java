package de.iani.cubequest.quests;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

	public void add(Reward other) {
		cubes += other.cubes;
		ItemStack newItems[] = new ItemStack[items.length + other.items.length];
		for (int i=0; i<items.length; i++) newItems[i] = items[i];
		for (int i=0; i<other.items.length; i++) newItems[i+items.length] = other.items[i];
		items = newItems;
	}

	public void pay(Player player) {
		//TODO: Abfragen, ob Platz ist, und ggF. Ausgabe machen oder Items hinzufügen.
		//TODO: Geld überweisen.
	}

}
