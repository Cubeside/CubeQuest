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
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

@DelegateDeserialization(Quest.class)
public class KillEntitiesQuest extends EntityTypesAndAmountQuest {
    
    public KillEntitiesQuest(int id, String name, String displayMessage, Collection<EntityType> types, int amount) {
        super(id, name, displayMessage, types, amount);
    }
    
    public KillEntitiesQuest(int id) {
        this(id, null, null, null, 0);
    }
    
    @Override
    public boolean onEntityKilledByPlayerEvent(EntityDeathEvent event, QuestState state) {
        if (!getTypes().contains(event.getEntityType())) {
            return false;
        }
        if (!this.fulfillsProgressConditions(event.getEntity().getKiller(), state.getPlayerData())) {
            return false;
        }
        
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(1);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(event.getEntity().getKiller());
        }
        return true;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        String entitiesKilledString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            entitiesKilledString += Quest.INDENTION;
        } else {
            entitiesKilledString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        entitiesKilledString +=
                ChatColor.DARK_AQUA + ChatAndTextUtil.multipleEntityTypesString(getTypes()) + " getötet: ";
        entitiesKilledString += status.color + "" + (state == null ? 0 : state.getAmount()) + "" + ChatColor.DARK_AQUA
                + " / " + getAmount();
        
        result.add(new ComponentBuilder(entitiesKilledString).create());
        
        return result;
    }
    
}
