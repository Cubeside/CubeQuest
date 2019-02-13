package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
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
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTameEvent;

@DelegateDeserialization(Quest.class)
public class TameEntitiesQuest extends EntityTypesAndAmountQuest {
    
    public TameEntitiesQuest(int id, String name, String displayMessage,
            Collection<EntityType> types, int amount) {
        super(id, name, displayMessage, types, amount);
    }
    
    public TameEntitiesQuest(int id) {
        super(id);
    }
    
    @Override
    public boolean onEntityTamedByPlayerEvent(EntityTameEvent event, QuestState state) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        if (!this.fulfillsProgressConditions((Player) event.getOwner(), state.getPlayerData())) {
            return false;
        }
        
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess((Player) event.getOwner());
        }
        return true;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        String entitiesTamedString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state) + " " + ChatColor.GOLD
                    + getDisplayName()).create());
            entitiesTamedString += Quest.INDENTION;
        } else {
            entitiesTamedString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        entitiesTamedString +=
                ChatColor.DARK_AQUA + ChatAndTextUtil.multipleMobsString(getTypes()) + " gezähmt: ";
        entitiesTamedString += status.color + "" + (state == null ? 0 : state.getAmount()) + ""
                + ChatColor.DARK_AQUA + " / " + getAmount();
        
        result.add(new ComponentBuilder(entitiesTamedString).create());
        
        return result;
    }
    
}
