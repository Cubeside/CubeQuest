package de.iani.cubequest.interaction;

import de.iani.cubequest.util.Util;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityInteractor extends Interactor {
    
    private UUID entityId;
    private Location cachedLocation;
    
    public EntityInteractor(Entity entity) {
        this.entityId = entity.getUniqueId();
    }
    
    public EntityInteractor(UUID entityId) {
        this(Bukkit.getEntity(entityId));
    }
    
    @SuppressWarnings("unchecked")
    public EntityInteractor(Map<String, Object> serialized) {
        super(serialized);
        
        String idString = (String) serialized.get("entityId");
        this.entityId = idString == null ? null : UUID.fromString(idString);
        this.cachedLocation =
                Util.deserializeLocation((Map<String, Object>) serialized.get("cachedLocation"));
    }
    
    @Override
    public UUID getIdentifier() {
        return this.entityId;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityId", this.entityId == null ? null : this.entityId.toString());
        result.put("cachedLocation", Util.serializeLocation(this.cachedLocation));
        return result;
    }
    
    @Override
    public boolean isLegal() {
        return this.entityId != null
                && (isForThisServer() || Bukkit.getEntity(this.entityId) != null);
    }
    
    @Override
    public String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Location getLocation(boolean ignoreCache) {
        Entity entity = Bukkit.getEntity(this.entityId);
        Location loc = entity == null ? null : entity.getLocation();
        
        if (loc != null) {
            this.cachedLocation = loc;
        } else if (!ignoreCache) {
            loc = this.cachedLocation;
        }
        
        return loc;
    }
    
    @Override
    public int compareTo(Interactor o) {
        int result = this.getClass().getName().compareTo(o.getClass().getName());
        
        if (result != 0) {
            return result;
        }
        assert (this.getClass() == o.getClass());
        
        boolean idNull = getIdentifier() == null;
        if (idNull) {
            result--;
        } else if (o.getIdentifier() == null) {
            result++;
        }
        
        if (result != 0 || idNull) {
            return result;
        }
        
        return getIdentifier().compareTo((UUID) o.getIdentifier());
    }
    
}
