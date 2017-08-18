package de.iani.cubequest.quests;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;

public class BlockBreakQuest extends MaterialsAndAmountQuest {

    public BlockBreakQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, types, amount);
    }

    public BlockBreakQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public boolean onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return false;
        }
        if (!getTypes().contains(event.getBlock().getType())) {
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
