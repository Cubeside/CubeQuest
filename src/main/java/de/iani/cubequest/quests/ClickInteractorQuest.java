package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

@DelegateDeserialization(Quest.class)
public class ClickInteractorQuest extends InteractorQuest {

    public ClickInteractorQuest(int id, Component name, Component displayMessage, Interactor target) {
        super(id, name, displayMessage, target);
    }

    public ClickInteractorQuest(int id) {
        this(id, null, null, null);
    }

    @Override
    public boolean playerConfirmedInteraction(Player player, QuestState state) {
        if (!super.playerConfirmedInteraction(player, state)) {
            return false;
        }
        onSuccess(state.getPlayerData().getPlayer());
        return true;
    }

    @Override
    public List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        QuestState state = data.getPlayerState(getId());
        Status status = (state == null) ? Status.NOTGIVENTO : state.getStatus();

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);

        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            Component titleLine = baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(state))
                    .append(Component.text(" ")).append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD));

            result.add(titleLine.color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "));
        }

        Component line = prefix.append(getInteractorName()).append(Component.text(" gefunden: "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color));

        result.add(line.color(NamedTextColor.DARK_AQUA));
        return result;
    }

}
