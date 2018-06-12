package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    
    public List<BaseComponent[]> getConditionInfo(boolean includeInvisible) {
        return this.visible || includeInvisible ? getConditionInfoInternal()
                : Collections.emptyList();
    }
    
    protected abstract List<BaseComponent[]> getConditionInfoInternal();
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("visible", this.visible);
        return result;
    }
    
}
