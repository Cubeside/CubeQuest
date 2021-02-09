package de.iani.cubequest.cubeshop;

import de.iani.cubequest.CubeQuest;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.shopitemconditions.ShopItemCondition;
import org.bukkit.entity.Player;


public class QuestLevelShopItemCondition extends ShopItemCondition {
    
    private int minLevel;
    
    public QuestLevelShopItemCondition(int minLevel) {
        if (minLevel < 0) {
            throw new IllegalArgumentException("minLevel must not be negative");
        }
        this.minLevel = minLevel;
    }
    
    public QuestLevelShopItemCondition(String serialized) throws DeserializationException {
        try {
            this.minLevel = Integer.parseInt(serialized);
            if (this.minLevel < 0) {
                throw new IllegalArgumentException("minLevel muast not be negative");
            }
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e);
        }
    }
    
    public int getMinLevel() {
        return this.minLevel;
    }
    
    @Override
    public QuestLevelShopItemConditionType getType() {
        return QuestLevelShopItemConditionType.getInstance();
    }
    
    @Override
    public boolean fullfills(Player player) {
        return CubeQuest.getInstance().getPlayerData(player).getLevel() >= this.minLevel;
    }
    
    @Override
    public String getDescription() {
        return "Mindest-Questlevel: " + this.minLevel;
    }
    
    @Override
    public String serialize() {
        return String.valueOf(this.minLevel);
    }
    
}
