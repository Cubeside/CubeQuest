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
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

@DelegateDeserialization(Quest.class)
public class FishingQuest extends MaterialsAndAmountQuest {
    
    public FishingQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Collection<Material> types, int amount) {
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
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        String itemsFishedString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state) + " " + ChatColor.GOLD
                    + getDisplayName()).create());
            itemsFishedString += Quest.INDENTION;
        } else {
            itemsFishedString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        itemsFishedString += ChatColor.DARK_AQUA
                + ChatAndTextUtil.multiplieFishablesString(getTypes()) + " geangelt: ";
        itemsFishedString += status.color + "" + (state == null ? 0 : state.getAmount()) + ""
                + ChatColor.DARK_AQUA + " / " + getAmount();
        
        result.add(new ComponentBuilder(itemsFishedString).create());
        
        return result;
    }
    
}
