package de.iani.cubequest.interaction;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubequest.util.Util;
import java.util.Map;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class NPCInteractor extends Interactor {
    
    private Integer npcId;
    private boolean wasSpawned;
    private SafeLocation cachedLocation;
    
    public NPCInteractor(Integer npcId) {
        if (npcId == null) {
            throw new NullPointerException();
        }
        
        this.npcId = npcId;
        setWasSpawned();
    }
    
    public NPCInteractor(Map<String, Object> serialized) {
        super(serialized);
        
        this.npcId = (Integer) serialized.get("npcId");
        this.cachedLocation = (SafeLocation) serialized.get("cachedLocation");
        
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
    protected String getUncachedName() {
        if (!isForThisServer()) {
            return null;
        }
        
        // Util.assertCitizens();
        return CubeQuest.getInstance().hasCitizensPlugin() ? getUncachedNameInternal() : null;
    }
    
    private String getUncachedNameInternal() {
        NPC npc = getNPCInternal().npc;
        return npc == null ? null : npc.getName();
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
        
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return false;
        }
        
        // Util.assertCitizens();
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
        // Util.assertCitizens();
        
        Location loc = CubeQuest.getInstance().hasCitizensPlugin()
                ? getNonCachedLocationInternal(ignoreCache)
                : null;
        if (loc != null) {
            SafeLocation oldCachedLocation = this.cachedLocation;
            this.cachedLocation = new SafeLocation(loc);
            
            if (oldCachedLocation == null || !oldCachedLocation.isSimilar(loc)) {
                cacheChanged();
            }
        } else if (!ignoreCache && this.cachedLocation != null) {
            loc = this.cachedLocation.getLocation();
        }
        return loc;
    }
    
    private Location getNonCachedLocationInternal(boolean ignoreNpcCache) {
        NPC npc = getNPC().npc;
        if (npc == null) {
            return null;
        }
        
        return npc.isSpawned() ? npc.getEntity().getLocation()
                : ignoreNpcCache ? null : npc.getStoredLocation();
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
        result.put("cachedLocation", this.cachedLocation);
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
