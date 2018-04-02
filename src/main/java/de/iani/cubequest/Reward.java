package de.iani.cubequest;

import com.google.common.base.Verify;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            this.cubes = serialized.containsKey("cubes") ? (Integer) serialized.get("cubes") : 0;
            this.questPoints =
                    serialized.containsKey("questPoints") ? (Integer) serialized.get("questPoints")
                            : 0;
            this.xp = serialized.containsKey("xp") ? (Integer) serialized.get("xp") : 0;
            this.items = serialized.containsKey("items")
                    ? ((List<ItemStack>) serialized.get("items")).toArray(new ItemStack[0])
                    : new ItemStack[0];
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    public int getCubes() {
        return this.cubes;
    }
    
    public int getQuestPoints() {
        return this.questPoints;
    }
    
    public int getXp() {
        return this.xp;
    }
    
    public ItemStack[] getItems() {
        return this.items;
    }
    
    public Reward add(Reward other) {
        ItemStack newItems[] = new ItemStack[this.items.length + other.items.length];
        for (int i = 0; i < this.items.length; i++) {
            newItems[i] = this.items[i];
        }
        for (int i = 0; i < other.items.length; i++) {
            newItems[i + this.items.length] = other.items[i];
        }
        
        return new Reward(this.cubes + other.cubes, this.questPoints + other.questPoints,
                this.xp + other.xp, newItems);
    }
    
    public void pay(Player player) {
        if (!CubeQuest.getInstance().isPayRewards()) {
            ChatAndTextUtil.sendXpAndQuestPointsMessage(player, this.xp, this.questPoints);
            CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
            
            if (this.cubes != 0 || this.items.length != 0) {
                addToTreasureChest(player.getUniqueId());
                ChatAndTextUtil.sendNormalMessage(player,
                        "Deine Belohnung wurde in deine Schatzkiste gelegt.");
            }
            return;
        }
        
        if (this.items.length != 0) {
            ItemStack[] playerInv = player.getInventory().getContents();
            playerInv = Arrays.copyOf(playerInv, 36);
            Inventory clonedPlayerInventory = Bukkit.createInventory(null, 36);
            clonedPlayerInventory.setContents(playerInv);
            
            int priceCount = this.items == null ? 0 : this.items.length;
            if (priceCount > 0) {
                ItemStack[] temp = new ItemStack[priceCount];
                for (int i = 0; i < priceCount; i++) {
                    temp[i] = this.items[i].clone();
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
                    temp[i] = this.items[i].clone();
                }
                player.getInventory().addItem(temp);
                for (ItemStack stack: this.items) {
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
        }
        
        ChatAndTextUtil.sendXpAndQuestPointsMessage(player, this.xp, this.questPoints);
        CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
        CubeQuest.getInstance().payCubes(player, this.cubes);
        if (this.cubes != 0) {
            ChatAndTextUtil.sendNormalMessage(player, "Du hast " + this.cubes + " Cubes erhalten.");
        }
        
        ChatAndTextUtil.sendMessage(player, ChatColor.GRAY + "Du hast eine Belohnung bekommen!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
    
    public void addToTreasureChest(UUID playerId) {
        if (!CubeQuest.getInstance().addToTreasureChest(playerId, this)) {
            try {
                CubeQuest.getInstance().getDatabaseFassade()
                        .addRewardToDeliver(new Reward(getCubes(), getItems()), playerId);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not add Quest-Reward to database for player with UUID " + playerId,
                        e);
            }
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cubes", this.cubes);
        data.put("questPoints", this.questPoints);
        data.put("xp", this.xp);
        data.put("items", this.items);
        return data;
    }
    
    public boolean isEmpty() {
        return this.cubes == 0 && this.questPoints == 0 && this.xp == 0 && this.items.length == 0;
    }
    
    public String toNiceString() {
        if (isEmpty()) {
            return "Nichts";
        }
        
        String result = "";
        result += this.cubes + " Cubes";
        result += ", " + this.questPoints + " Punkte";
        result += ", " + this.xp + " XP";
        
        if (this.items.length != 0) {
            result += ", Items: ";
            for (ItemStack item: this.items) {
                result += ItemStackUtil.toNiceString(item) + ", ";
            }
            result = result.substring(0, result.length() - ", ".length());
        }
        
        return result;
    }
    
}
