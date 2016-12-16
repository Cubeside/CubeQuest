package de.iani.cubequest.quests;

import com.google.common.base.Verify;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class TalkQuest extends Quest {

    int targetID;

    public TalkQuest(String name, String giveMessage, String successMessage, Reward successReward,
            NPC target) {
        super (name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(target);

        targetID = target.getId();
    }

    @Override
    public void onNPCClickEvent(NPCClickEvent event) {
        if (event.getNPC().getId() != targetID) {
            return;
        }
        if (getPlayerStatus(event.getClicker().getUniqueId()) != Status.GIVENTO) {
            return;
        }
        onSuccess(event.getClicker());
    }

}
