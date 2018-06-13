package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import org.bukkit.entity.Player;


public class RenamedCondition extends QuestCondition {
    
    private String text;
    private QuestCondition original;
    
    public static QuestCondition rename(String text, QuestCondition original) {
        original = (original instanceof RenamedCondition) ? ((RenamedCondition) original).original
                : original;
        return text.isEmpty() ? original : new RenamedCondition(text, original);
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
    public BaseComponent[] getConditionInfo(boolean includeHiddenInfo) {
        BaseComponent[] result = getConditionInfo();
        if (!includeHiddenInfo) {
            return result;
        }
        
        return new ComponentBuilder("").append(result).append(" (Intern: ").reset()
                .color(ChatColor.DARK_AQUA).append(this.original.getConditionInfo(true))
                .retain(FormatRetention.NONE).append(")").reset().color(ChatColor.DARK_AQUA)
                .create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("text", this.text);
        result.put("original", this.original);
        return result;
    }
    
}
