package de.iani.cubequest.interaction;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.Util;
import net.citizensnpcs.api.npc.NPC;

public class NPCInteractor extends Interactor {

    private Integer npcId;
    private boolean wasSpawned;

    public NPCInteractor(NPC npc) {
        this(npc.getId());
    }

    public NPCInteractor(Integer npcId) {
        if (npcId == null) {
            throw new NullPointerException();
        }

        this.npcId = npcId;
        setWasSpawned();
    }

    public NPCInteractor(Map<String, Object> serialized) throws InvalidConfigurationException {
        npcId = (Integer) serialized.get("npcId");

        if (npcId == null) {
            throw new InvalidConfigurationException();
        }

        if (serialized.containsKey("wasSpawned")) {
            wasSpawned = (Boolean) serialized.get("wasSpawned");
        } else {
            setWasSpawned();
        }
    }

    private void setWasSpawned() {
        Util.assertCitizens();
        setWasSpawnedInternal();
    }

    private void setWasSpawnedInternal() {
        wasSpawned = getNPCInternal().isSpawned();
    }

    public NPC getNPC() {
        Util.assertCitizens();

        return getNPCInternal();
    }

    private NPC getNPCInternal() {
        return CubeQuest.getInstance().getNPCReg().getById(npcId);
    }

    @Override
    public Integer getIdentifier() {
        return npcId;
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
        return !isForThisServer() || (getNPCInternal() != null && getNPCInternal().getStoredLocation() != null);
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

    @Override
    public int compareTo(Interactor o) {
        int result = this.getClass().getName().compareTo(o.getClass().getName());

        if (result != 0) {
            return result;
        }
        assert(this.getClass() == o.getClass());

        return getIdentifier().compareTo((Integer) o.getIdentifier());
    }

}
