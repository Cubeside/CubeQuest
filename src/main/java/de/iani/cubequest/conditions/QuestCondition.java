package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class QuestCondition implements ConfigurationSerializable {

    private boolean visible;

    public QuestCondition(boolean visible) {
        this.visible = visible;
    }

    public QuestCondition(Map<String, Object> serialized) {
        this.visible = serialized.containsKey("visible") ? (boolean) serialized.get("visible") : true;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public abstract boolean fulfills(Player player, PlayerData data);

    public abstract Component getConditionInfo(boolean includeHiddenInfo);

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("visible", this.visible);
        return result;
    }

    public QuestCondition performDataUpdate() {
        return this;
    }

}
