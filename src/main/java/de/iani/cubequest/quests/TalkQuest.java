package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class TalkQuest extends Quest {

    private Integer targetID;

    public TalkQuest(String name, String giveMessage, String successMessage, Reward successReward,
            NPC target) {
        super (name, giveMessage, successMessage, successReward);

        targetID = target == null? null: target.getId();
    }

    public TalkQuest(String name) {
        this(name, null, null, null, null);
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

    @Override
    public boolean isLegal() {
        return targetID != null;
    }

    public NPC getNPC() {
        if (targetID == null) {
            return null;
        }
        return CubeQuest.getInstance().getNPCReg().getById(targetID);
    }

    //TODO NPCs setzen: fertigen übergeben oder Daten übergeben und dann erstellen?

}
