package de.iani.cubequest.questGiving;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class QuestGivingCondition implements ConfigurationSerializable {

    public abstract boolean fullfills(PlayerData data);

    public boolean fullfills(UUID id) {
        return fullfills(CubeQuest.getInstance().getPlayerData(id));
    }

    public boolean fullfills(Player player) {
        return fullfills(player.getUniqueId());
    }

    public abstract List<BaseComponent[]> getConditionInfo();

}
