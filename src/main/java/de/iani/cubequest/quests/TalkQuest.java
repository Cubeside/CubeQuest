package de.iani.cubequest.quests;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class TalkQuest extends NPCQuest {

    public TalkQuest(String name, String giveMessage, String successMessage, Reward successReward, NPC target) {
        super (name, giveMessage, successMessage, successReward, target);
    }

    public TalkQuest(String name) {
        this(name, null, null, null, null);
    }

    @Override
    public boolean onNPCClickEvent(NPCClickEvent event) {
        if (!super.onNPCClickEvent(event)) {
            return false;
        }
        onSuccess(event.getClicker());
        return true;
    }

}
