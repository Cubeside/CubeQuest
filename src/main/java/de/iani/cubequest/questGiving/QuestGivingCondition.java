package de.iani.cubequest.questGiving;

import de.iani.cubequest.PlayerData;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class QuestGivingCondition implements ConfigurationSerializable {
    
    public abstract boolean fullfills(PlayerData data);
    
    public abstract List<BaseComponent[]> getConditionInfo();
    
}
