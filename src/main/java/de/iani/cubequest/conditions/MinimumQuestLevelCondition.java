package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class MinimumQuestLevelCondition extends QuestCondition {
    
    private int minLevel;
    
    public MinimumQuestLevelCondition(boolean visible, int minLevel) {
        super(visible);
        init(minLevel);
    }
    
    public MinimumQuestLevelCondition(Map<String, Object> serialized) {
        super(serialized);
        init(((Number) serialized.get("minLevel")).intValue());
    }
    
    private void init(int minLevel) {
        if (minLevel < 0) {
            throw new IllegalArgumentException("minLevel must not be negative");
        }
        this.minLevel = minLevel;
    }
    
    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return data.getLevel() >= this.minLevel;
    }
    
    @Override
    public BaseComponent[] getConditionInfo() {
        return new ComponentBuilder(
                ChatColor.DARK_AQUA + "Min. Level: " + ChatColor.GREEN + this.minLevel).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("minLevel", this.minLevel);
        return result;
    }
    
}
