package de.iani.cubequest.interaction;

import java.util.HashMap;
import java.util.Map;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.Util;
import net.citizensnpcs.api.npc.NPC;

public class NPCInteractor extends Interactor {

    private Integer npcId;
    private boolean wasSpawned;

    public NPCInteractor(NPC npc) {
        this(npc == null? null : npc.getId());
    }

    public NPCInteractor(Integer npcId) {
        this.npcId = npcId;
        setWasSpawned();
    }

    public NPCInteractor(Map<String, Object> serialized) {
        npcId = (Integer) serialized.get("npcId");
        if (serialized.containsKey("wasSpawned")) {
            wasSpawned = (Boolean) serialized.get("wasSpawned");
        } else {
            setWasSpawned();
        }
    }

    private void setWasSpawned() {
        if (npcId == null) {
            wasSpawned = false;
        } else {
            Util.assertCitizens();
            setWasSpawnedInternal();
        }
    }

    private void setWasSpawnedInternal() {
        NPC npc = getNPCInternal();
        wasSpawned = npc == null? false : npc.isSpawned();
    }

    public NPC getNPC() {
        Util.assertCitizens();

        return getNPCInternal();
    }

    private NPC getNPCInternal() {
        return npcId == null? null : CubeQuest.getInstance().getNPCReg().getById(npcId);
    }

    @Override
    public void makeAccessible() {
        setWasSpawned();
        getNPC().spawn(getNPC().getStoredLocation());
    }

    @Override
    public void resetAccessible() {
        Util.assertCitizens();
        resetAccessibleInternal();
    }

    private void resetAccessibleInternal() {
        if (!wasSpawned && getNPCInternal().isSpawned()) {
            getNPCInternal().despawn();
        }
    }

    @Override
    public boolean isLegal() {
        return npcId != null && (!isForThisServer() || (getNPCInternal() != null && getNPCInternal().getStoredLocation() != null));
    }

    @Override
    public String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("npcId", npcId);
        result.put("wasSpawned", wasSpawned);
        return result;
    }

}
