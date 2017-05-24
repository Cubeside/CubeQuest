package de.iani.cubequest.quests;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class TalkQuest extends NPCQuest {

    public TalkQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, NPC target) {
        super (id, name, giveMessage, successMessage, successReward, target);
    }

    public TalkQuest(int id) {
        this(id, null, null, null, null, null);
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
