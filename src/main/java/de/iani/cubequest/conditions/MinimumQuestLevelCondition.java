package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class MinimumQuestLevelCondition extends QuestCondition {
    
    private int minLevel;
    
    public MinimumQuestLevelCondition(int minLevel) {
        this.minLevel = minLevel;
    }
    
    public MinimumQuestLevelCondition(Map<String, Object> serialized) {
        this.minLevel = (Integer) serialized.get("minLevel");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("minLevel", this.minLevel);
        return result;
    }
    
    @Override
    public boolean fullfills(Player player, PlayerData data) {
        return data.getLevel() >= this.minLevel;
    }
    
    @Override
    public List<BaseComponent[]> getConditionInfo() {
        return Collections.singletonList(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Min. Level: " + ChatColor.GREEN + this.minLevel).create());
    }
    
}
