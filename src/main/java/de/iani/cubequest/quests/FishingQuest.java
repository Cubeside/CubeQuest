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
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

@DelegateDeserialization(Quest.class)
public class FishingQuest extends MaterialsAndAmountQuest {

    public FishingQuest(int id, String name, Component displayMessage, Collection<Material> types, int amount) {
        super(id, name, displayMessage, types, amount);
    }

    public FishingQuest(int id) {
        this(id, null, null, null, 0);
    }

    @Override
    public boolean onPlayerFishEvent(PlayerFishEvent event, QuestState state) {
        if (event.getState() != State.CAUGHT_FISH) {
            return false;
        }
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

        Component materials = ChatAndTextUtil.multipleMaterialsComponent(getTypes());
        int current = (state == null) ? 0 : state.getAmount();

        result.add(
                prefix.append(materials.colorIfAbsent(NamedTextColor.DARK_AQUA)).append(Component.text(" geangelt: "))
                        .append(Component.text(String.valueOf(current)).color(status.color))
                        .append(Component.text(" / " + getAmount())).color(NamedTextColor.DARK_AQUA));

        return result;
    }

}
