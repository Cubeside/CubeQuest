package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTameEvent;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;

public class TameEntitiesQuest extends EntityTypesAndAmountQuest {

    public TameEntitiesQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            Collection<EntityType> types, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, types, amount);
    }

    public TameEntitiesQuest(int id) {
        super(id);
    }

    @Override
    public boolean onEntityTamedByPlayerEvent(EntityTameEvent event, QuestState state) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess((Player) event.getOwner());
        }
        return true;
    }

}
