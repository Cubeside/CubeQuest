package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

@DelegateDeserialization(Quest.class)
public class ClickInteractorQuest extends InteractorQuest {
    
    public ClickInteractorQuest(int id, String name, String displayMessage, Interactor target) {
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
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        ComponentBuilder interactorClickedBuilder =
                new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel));
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            interactorClickedBuilder.append(Quest.INDENTION);
        } else {
            interactorClickedBuilder.append(ChatAndTextUtil.getStateStringStartingToken(state) + " ");
        }
        
        interactorClickedBuilder.append("" + ChatColor.DARK_AQUA)
                .append(TextComponent.fromLegacyText(String.valueOf(getInteractorName()))).append(" gefunden: ")
                .color(ChatColor.DARK_AQUA);
        interactorClickedBuilder.append(status == Status.SUCCESS ? "ja" : "nein").color(status.color);
        
        result.add(interactorClickedBuilder.create());
        
        return result;
    }
    
}
