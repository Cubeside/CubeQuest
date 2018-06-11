package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class NegatedQuestCondition extends QuestCondition {
    
    private QuestCondition original;
    
    public NegatedQuestCondition(QuestCondition original) {
        this.original = Objects.requireNonNull(original);
    }
    
    public NegatedQuestCondition(Map<String, Object> serialized) {
        this((QuestCondition) serialized.get("original"));
    }
    
    @Override
    public boolean fullfills(Player player, PlayerData data) {
        return !this.original.fullfills(player, data);
    }
    
    @Override
    public List<BaseComponent[]> getConditionInfo() {
        List<BaseComponent[]> originalInfo = this.original.getConditionInfo();
        if (originalInfo.isEmpty()) {
            return Collections.emptyList();
        }
        
        BaseComponent[] oldFirst = originalInfo.get(0);
        BaseComponent[] newFirst = new ComponentBuilder("Nicht: ").color(ChatColor.DARK_AQUA)
                .append(oldFirst).create();
        
        if (originalInfo.size() == 1) {
            return Collections.singletonList(newFirst);
        }
        List<BaseComponent[]> result = new ArrayList<>(originalInfo);
        result.set(0, newFirst);
        return result;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("original", this.original);
        return result;
    }
}
