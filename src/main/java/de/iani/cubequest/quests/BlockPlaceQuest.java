package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.block.BlockPlaceEvent;

@DelegateDeserialization(Quest.class)
public class BlockPlaceQuest extends MaterialsAndAmountQuest {
    
    public BlockPlaceQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Collection<Material> types, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, types, amount);
    }
    
    public BlockPlaceQuest(int id) {
        this(id, null, null, null, null, null, null, 0);
    }
    
    
    @Override
    public boolean onBlockPlaceEvent(BlockPlaceEvent event, QuestState state) {
        if (!getTypes().contains(event.getBlock().getType())) {
            return false;
        }
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(event.getPlayer());
        }
        return true;
    }
    
}
