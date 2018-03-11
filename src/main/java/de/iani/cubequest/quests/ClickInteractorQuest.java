package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import org.bukkit.configuration.serialization.DelegateDeserialization;

@DelegateDeserialization(Quest.class)
public class ClickInteractorQuest extends InteractorQuest {
    
    public ClickInteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Interactor target) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, target);
    }
    
    public ClickInteractorQuest(int id) {
        this(id, null, null, null, null, null, null);
    }
    
    public boolean playerConfirmedInteraction(QuestState state) {
        onSuccess(state.getPlayerData().getPlayer());
        return true;
    }
    
}
