package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.wrapper.NPCClickEventWrapper;

public class ClickNPCQuest extends NPCQuest {

    public ClickNPCQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, Integer target) {
        super (id, name, giveMessage, successMessage, successReward, target);
    }

    public ClickNPCQuest(int id) {
        this(id, null, null, null, null, null);
    }

    @Override
    public boolean onNPCClickEvent(NPCClickEventWrapper event, QuestState state) {
        if (!super.onNPCClickEvent(event, state)) {
            return false;
        }
        onSuccess(event.getOriginal().getClicker());
        return true;
    }

}
