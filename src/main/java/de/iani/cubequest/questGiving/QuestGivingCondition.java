package de.iani.cubequest.questGiving;

import java.util.List;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import de.iani.cubequest.PlayerData;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class QuestGivingCondition implements ConfigurationSerializable {

    public abstract boolean fullfills(PlayerData data);

    public abstract List<BaseComponent[]> getConditionInfo();

}
