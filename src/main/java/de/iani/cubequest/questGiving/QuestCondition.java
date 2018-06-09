package de.iani.cubequest.questGiving;

import de.iani.cubequest.PlayerData;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class QuestCondition implements ConfigurationSerializable {
    
    public abstract boolean fullfills(Player player, PlayerData data);
    
    public abstract List<BaseComponent[]> getConditionInfo();
    
}
