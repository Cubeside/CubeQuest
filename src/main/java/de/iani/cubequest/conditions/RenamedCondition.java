package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class RenamedCondition extends QuestCondition {
    
    private String text;
    private QuestCondition original;
    
    public static QuestCondition rename(String text, QuestCondition original) {
        return new RenamedCondition(text,
                (original instanceof RenamedCondition) ? ((RenamedCondition) original).original
                        : original);
    }
    
    private RenamedCondition(String text, QuestCondition original) {
        super(true);
        init(text, original);
    }
    
    public RenamedCondition(Map<String, Object> serialized) {
        super(true);
        init((String) serialized.get("text"), (QuestCondition) serialized.get("original"));
    }
    
    private void init(String text, QuestCondition original) {
        this.text = Objects.requireNonNull(text);
        this.original = Objects.requireNonNull(original);
    }
    
    @Override
    public boolean fullfills(Player player, PlayerData data) {
        return this.original.fullfills(player, data);
    }
    
    @Override
    public BaseComponent[] getConditionInfo() {
        return new ComponentBuilder(this.text).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("text", this.text);
        result.put("original", this.original);
        return result;
    }
    
}
