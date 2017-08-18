package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;

public class FishingQuest extends MaterialsAndAmountQuest {

    public FishingQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, types, amount);
    }

    public FishingQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item)) {
            return false;
        }
        Item item = (Item) event.getCaught();
        if (!getTypes().contains(item.getItemStack().getType())) {
            return false;
        }
        PlayerData pData = CubeQuest.getInstance().getPlayerData(event.getPlayer());
        if (!pData.isGivenTo(this.getId())) {
            return false;
        }
        AmountQuestState state = (AmountQuestState) pData.getPlayerState(this.getId());
        if (state.getAmount()+1 >= getAmount()) {
            onSuccess(event.getPlayer());
        } else {
            state.changeAmount(1);
        }
        return true;
    }

}
