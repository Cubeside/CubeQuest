package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import java.util.Collection;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

@DelegateDeserialization(Quest.class)
public class KillEntitiesQuest extends EntityTypesAndAmountQuest {
    
    public KillEntitiesQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Collection<EntityType> types, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, types, amount);
    }
    
    public KillEntitiesQuest(int id) {
        this(id, null, null, null, null, null, null, 0);
    }
    
    @Override
    public boolean onEntityKilledByPlayerEvent(EntityDeathEvent event, QuestState state) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(event.getEntity().getKiller());
        }
        return true;
    }
    
}
