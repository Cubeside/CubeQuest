package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.LinkedHashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class QuestAction implements ConfigurationSerializable {

    public QuestAction() {

    }

    public QuestAction(Map<String, Object> serialized) {

    }

    public abstract void perform(Player player, PlayerData data);

    public abstract BaseComponent[] getActionInfo();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        return result;
    }

    public QuestAction performDataUpdate() {
        return this;
    }
}
