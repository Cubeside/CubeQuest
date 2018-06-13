package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class QuestCondition implements ConfigurationSerializable {
    
    private boolean visible;
    
    public QuestCondition(boolean visible) {
        this.visible = visible;
    }
    
    public QuestCondition(Map<String, Object> serialized) {
        this.visible =
                serialized.containsKey("visible") ? (boolean) serialized.get("visible") : true;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public abstract boolean fullfills(Player player, PlayerData data);
    
    public abstract BaseComponent[] getConditionInfo();
    
    public BaseComponent[] getConditionInfo(boolean includeHiddenInfo) {
        return getConditionInfo();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("visible", this.visible);
        return result;
    }
    
}
