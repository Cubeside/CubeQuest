package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;

public class KillEntitiesQuest extends EntityTypesAndAmountQuest {

    public KillEntitiesQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<EntityType> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, types, amount);
    }

    public KillEntitiesQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public boolean onEntityDeathEvent(EntityDeathEvent event) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return false;
        }
        PlayerData pData = CubeQuest.getInstance().getPlayerData(player);
        if (!pData.isGivenTo(this.getId())) {
            return false;
        }
        AmountQuestState state = (AmountQuestState) pData.getPlayerState(this.getId());
        if (state.getAmount()+1 >= getAmount()) {
            onSuccess(player);
        } else {
            state.changeAmount(1);
        }
        return true;
    }

}
