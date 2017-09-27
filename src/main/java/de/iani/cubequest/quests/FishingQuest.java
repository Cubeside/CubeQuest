package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;

public class FishingQuest extends MaterialsAndAmountQuest {

    public FishingQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, types, amount);
    }

    public FishingQuest(int id) {
        this(id, null, null, null, null, null, null, 0);
    }

    @Override
    public boolean onPlayerFishEvent(PlayerFishEvent event, QuestState state) {
        if (!(event.getCaught() instanceof Item)) {
            return false;
        }
        Item item = (Item) event.getCaught();
        if (!getTypes().contains(item.getItemStack().getType())) {
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
