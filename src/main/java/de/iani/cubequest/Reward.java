package de.iani.cubequest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.base.Verify;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import net.md_5.bungee.api.ChatColor;

public class Reward implements ConfigurationSerializable {
    
    private int cubes;
    private int questPoints;
    private int xp;
    private ItemStack[] items;
    
    public Reward() {
        this(0, new ItemStack[0]);
    }
    
    public Reward(int cubes) {
        this(cubes, new ItemStack[0]);
    }
    
    public Reward(ItemStack[] items) {
        this(0, items);
    }
    
    public Reward(int cubes, ItemStack[] items) {
        this(cubes, 0, 0, items);
        
    }
    
    public Reward(int cubes, int questPoints, int xp, ItemStack[] items) {
        Verify.verify(cubes >= 0);
        Verify.verify(questPoints >= 0);
        Verify.verify(xp >= 0);
        
        this.cubes = cubes;
        this.questPoints = questPoints;
        this.xp = xp;
        this.items = items == null ? new ItemStack[0] : ItemStackUtil.shrinkItemStack(items);
    }
    
    @SuppressWarnings("unchecked")
    public Reward(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            cubes = serialized.containsKey("cubes") ? (int) serialized.get("cubes") : 0;
            questPoints =
                    serialized.containsKey("questPoints") ? (int) serialized.get("questPoints") : 0;
            xp = serialized.containsKey("xp") ? (int) serialized.get("xp") : 0;
            items = serialized.containsKey("items")
                    ? ((List<ItemStack>) serialized.get("items")).toArray(new ItemStack[0])
                    : new ItemStack[0];
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    public int getCubes() {
        return cubes;
    }
    
    public int getQuestPoints() {
        return questPoints;
    }
    
    public int getXp() {
        return xp;
    }
    
    public ItemStack[] getItems() {
        return items;
    }
    
    public Reward add(Reward other) {
        ItemStack newItems[] = new ItemStack[items.length + other.items.length];
        for (int i = 0; i < items.length; i++) {
            newItems[i] = items[i];
        }
        for (int i = 0; i < other.items.length; i++) {
            newItems[i + items.length] = other.items[i];
        }
        
        return new Reward(cubes + other.cubes, questPoints + other.questPoints, xp + other.xp,
                newItems);
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
                ChatAndTextUtil.sendWarningMessage(player,
                        "Du hast nicht genügend Platz in deinem Inventar! Deine Belohnung wird in deine Schatzkiste gelegt.");
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
            for (ItemStack stack: items) {
                StringBuilder t = new StringBuilder("  ");
                if (stack.getAmount() > 1) {
                    t.append(stack.getAmount()).append(" ");
                }
                t.append(ChatAndTextUtil.capitalize(stack.getType().name(), true));
                if (stack.getDurability() > 0) {
                    t.append(':').append(stack.getDurability());
                }
                ItemMeta meta = stack.getItemMeta();
                if (meta.hasDisplayName()) {
                    t.append(" (").append(meta.getDisplayName()).append(ChatColor.YELLOW)
                            .append(")");
                }
                ChatAndTextUtil.sendMessage(player, t.toString());
            }
        }
        
        CubeQuest.getInstance().payCubes(player, cubes);
        CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
        
        ChatAndTextUtil.sendMessage(player, ChatColor.GRAY + "Du hast eine Belohnung bekommen!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
    
    public void addToTreasureChest(UUID playerId) {
        CubeQuest.getInstance().addQuestPoints(playerId, questPoints);
        CubeQuest.getInstance().addXp(playerId, xp);
        if (!CubeQuest.getInstance().addToTreasureChest(playerId, this)) {
            try {
                CubeQuest.getInstance().getDatabaseFassade()
                        .addRewardToDeliver(new Reward(this.getCubes(), this.getItems()), playerId);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not add Quest-Reward to database for player with UUID " + playerId,
                        e);
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
        return cubes == 0 && items.length == 0;
    }
    
    public String toNiceString() {
        if (isEmpty()) {
            return "Nichts";
        }
        
        String result = "";
        result += cubes + " Cubes";
        
        if (items.length != 0) {
            result += ", Items: ";
            for (ItemStack item: items) {
                result += ItemStackUtil.toNiceString(item) + ", ";
            }
            result = result.substring(0, result.length() - ", ".length());
        }
        
        return result;
    }
    
}
