package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

@DelegateDeserialization(Quest.class)
public class BlockPlaceQuest extends SymmetricalMaterialsAndAmountQuest {
    
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
        if (!this.fulfillsProgressConditions(event.getPlayer(), state.getPlayerData())) {
            return false;
        }
        
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(event.getPlayer());
        }
        return true;
    }
    
    @Override
    public boolean onBlockBreakEvent(BlockBreakEvent event, QuestState state) {
        if (isIgnoreOpposite()) {
            return false;
        }
        if (!getTypes().contains(event.getBlock().getType())) {
            return false;
        }
        if (!this.fulfillsProgressConditions(event.getPlayer(), state.getPlayerData())) {
            return false;
        }
        
        AmountQuestState amountState = (AmountQuestState) state;
        if (amountState.getAmount() > 0) {
            amountState.changeAmount(-1);
        }
        return true;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        String blocksPlacedString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state) + " " + ChatColor.GOLD
                    + getDisplayName()).create());
            blocksPlacedString += Quest.INDENTION;
        } else {
            blocksPlacedString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        blocksPlacedString += ChatColor.DARK_AQUA + ChatAndTextUtil.multipleBlockString(getTypes())
                + " platziert: ";
        blocksPlacedString += status.color + "" + (state == null ? 0 : state.getAmount()) + ""
                + ChatColor.DARK_AQUA + " / " + getAmount();
        
        result.add(new ComponentBuilder(blocksPlacedString).create());
        
        return result;
    }
    
}
