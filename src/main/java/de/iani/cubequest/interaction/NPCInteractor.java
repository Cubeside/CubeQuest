package de.iani.cubequest.interaction;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.util.Map;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;

public class NPCInteractor extends Interactor {
    
    private Integer npcId;
    private boolean wasSpawned;
    
    public NPCInteractor(Integer npcId) {
        if (npcId == null) {
            throw new NullPointerException();
        }
        
        this.npcId = npcId;
        setWasSpawned();
    }
    
    public NPCInteractor(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);
        
        this.npcId = (Integer) serialized.get("npcId");
        
        if (this.npcId == null) {
            throw new InvalidConfigurationException();
        }
        
        if (serialized.containsKey("wasSpawned")) {
            this.wasSpawned = (Boolean) serialized.get("wasSpawned");
        } else {
            if (isForThisServer()) {
                setWasSpawned();
            }
        }
    }
    
    public static class NPCWrapper {
        
        public final NPC npc;
        
        private NPCWrapper(NPC npc) {
            this.npc = npc;
        }
    }
    
    private void setWasSpawned() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        setWasSpawnedInternal();
    }
    
    private void setWasSpawnedInternal() {
        this.wasSpawned = getNPCInternal().npc.isSpawned();
    }
    
    public NPCWrapper getNPC() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        return getNPCInternal();
    }
    
    private NPCWrapper getNPCInternal() {
        return new NPCWrapper(CubeQuest.getInstance().getNPCReg().getById(this.npcId));
    }
    
    @Override
    public Integer getIdentifier() {
        return this.npcId;
    }
    
    @Override
    protected String getAndCacheName() {
        if (!isForThisServer()) {
            return null;
        }
        
        Util.assertCitizens();
        return getAndCacheNameInternal();
    }
    
    private String getAndCacheNameInternal() {
        return getNPCInternal().npc.getName();
    }
    
    @Override
    public void makeAccessible() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        makeAccessibleInternal();
    }
    
    private void makeAccessibleInternal() {
        setWasSpawnedInternal();
        getNPC().npc.spawn(getNPC().npc.getStoredLocation());
    }
    
    @Override
    public void resetAccessible() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        resetAccessibleInternal();
    }
    
    private void resetAccessibleInternal() {
        if (!this.wasSpawned && getNPCInternal().npc.isSpawned()) {
            getNPCInternal().npc.despawn();
        }
    }
    
    @Override
    public boolean isLegal() {
        if (!isForThisServer()) {
            return true;
        }
        
        Util.assertCitizens();
        return isLegalInternal();
    }
    
    private boolean isLegalInternal() {
        NPC npc = getNPCInternal().npc;
        return npc != null && npc.getStoredLocation() != null;
    }
    
    @Override
    public String getInfo() {
        return ChatAndTextUtil.getNPCInfoString(getServerId(), this.npcId);
    }
    
    @Override
    public Location getLocation(boolean ignoreCache) {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        return getLocationInternal(ignoreCache);
    }
    
    private Location getLocationInternal(boolean ignoreCache) {
        NPC npc = getNPC().npc;
        if (npc == null) {
            return null;
        }
        
        return npc.isSpawned() ? npc.getEntity().getLocation()
                : ignoreCache ? null : npc.getStoredLocation();
    }
    
    @Override
    public double getHeight() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        return getHeightInternal();
    }
    
    private double getHeightInternal() {
        NPC npc = getNPCInternal().npc;
        Entity entity = npc != null ? npc.getEntity() : null;
        return entity != null ? entity.getHeight() : 2;
    }
    
    @Override
    public double getWidth() {
        Util.assertForThisServer(this);
        Util.assertCitizens();
        
        return getWidthInternal();
    }
    
    private double getWidthInternal() {
        NPC npc = getNPCInternal().npc;
        Entity entity = npc != null ? npc.getEntity() : null;
        return entity != null ? entity.getWidth() : 1;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("npcId", this.npcId);
        result.put("wasSpawned", this.wasSpawned);
        return result;
    }
    
    @Override
    public int compareTo(Interactor o) {
        int result = super.compareTo(o);
        
        if (result != 0) {
            return result;
        }
        assert (this.getClass() == o.getClass());
        
        return getIdentifier().compareTo((Integer) o.getIdentifier());
    }
    
}
