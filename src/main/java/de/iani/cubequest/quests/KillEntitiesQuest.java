package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;

public class KillEntitiesQuest extends EntityTypesAndAmountQuest {

    public KillEntitiesQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<EntityType> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, types, amount);
    }

    public KillEntitiesQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public boolean onEntityDeathEvent(EntityDeathEvent event, QuestState state) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        AmountQuestState amountState = (AmountQuestState) state;
        if (amountState.getAmount()+1 >= getAmount()) {
            onSuccess(event.getEntity().getKiller());
        } else {
            amountState.changeAmount(1);
        }
        return true;
    }

}
