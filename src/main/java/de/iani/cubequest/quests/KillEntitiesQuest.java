package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

@DelegateDeserialization(Quest.class)
public class KillEntitiesQuest extends EntityTypesAndAmountQuest {

    public KillEntitiesQuest(int id, String name, Component displayMessage, Collection<EntityType> types, int amount) {
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
    public List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = (state == null) ? Status.NOTGIVENTO : state.getStatus();

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            result.add(baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "))
                    .append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD)).color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "));
        }

        Component types = ChatAndTextUtil.multipleEntityTypesComponent(getTypes());
        int current = (state == null) ? 0 : state.getAmount();

        result.add(prefix.append(types).append(Component.text(" getötet: "))
                .append(Component.text(String.valueOf(current)).color(status.color))
                .append(Component.text(" / " + getAmount())).color(NamedTextColor.DARK_AQUA));

        return result;
    }

}
