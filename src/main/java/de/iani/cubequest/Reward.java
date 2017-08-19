package de.iani.cubequest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Verify;

import net.md_5.bungee.api.ChatColor;

public class Reward implements ConfigurationSerializable {

    private int cubes;
    private ItemStack[] items;

    public Reward() {
        cubes = 0;
        items = new ItemStack[0];
    }

    public Reward(int cubes) {
        Verify.verify(cubes >= 0);

        this.cubes = cubes;
        items = new ItemStack[0];
    }

    public Reward(ItemStack[] items) {
        cubes = 0;
        this.items = items;
    }

    public Reward(int cubes, ItemStack[] items) {
        Verify.verify(cubes >= 0);

        this.cubes = cubes;
        this.items = items;
    }

    public Reward(String serialized) throws InvalidConfigurationException {
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        this.cubes = yc.getInt("reward.cubes");
        this.items = yc.getList("reward.items").toArray(new ItemStack[0]);
    }

    public int getCubes() {
        return cubes;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public Reward add(Reward other) {
        ItemStack newItems[] = new ItemStack[items.length + other.items.length];
        for (int i=0; i<items.length; i++) {
            newItems[i] = items[i];
        }
        for (int i=0; i<other.items.length; i++) {
            newItems[i+items.length] = other.items[i];
        }

        return new Reward(cubes + other.cubes, newItems);
    }

    public void pay(Player player) {
        if (!CubeQuest.getInstance().isPayRewards()) {
            addToTreasureChest(player.getUniqueId());
            return;
        }

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
                    CubeQuest.sendWarningMessage(player, "Du hast nicht genügend Platz in deinem Inventar! Deine Belohnung wird in deine Schatzkiste gelegt.");
                    player.updateInventory();
                    addToTreasureChest(player.getUniqueId());
                    return;
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

            CubeQuest.getInstance().payCubes(player.getUniqueId(), cubes);

            CubeQuest.sendMessage(player, ChatColor.GRAY + "Du hast eine Belohnung abgeholt!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    public void addToTreasureChest(UUID playerId) {
        if (CubeQuest.getInstance().hasTreasureChest()) {
            CubeQuest.getInstance().addToTreasureChest(playerId, this);
        } else {
            try {
                CubeQuest.getInstance().getDatabaseFassade().addRewardToDeliver(this, playerId);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not add Quest-Reward to database for player with UUID " + playerId, e);
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("cubes", cubes);
        data.put("items", items);
        return data;
    }

    public boolean isEmpty() {
        if (cubes != 0) {
            return false;
        }
        for (ItemStack s: items) {
            if (s != null && s.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }

}
