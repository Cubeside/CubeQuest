package de.iani.cubequest.interaction;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.ServerSpecific;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class Interactor
        implements ServerSpecific, Comparable<Interactor>, ConfigurationSerializable {
    
    public static final Comparator<Interactor> COMPARATOR = (i1, i2) -> {
        if (i1 == null) {
            return i2 == null ? 0 : -1;
        } else {
            return i2 == null ? 1 : i1.compareTo(i2);
        }
    };
    
    private int serverId;
    private String cachedName;
    
    public Interactor() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }
    
    public Interactor(int serverId) {
        this.serverId = serverId;
    }
    
    public Interactor(Map<String, Object> serialized) {
        this.serverId = (Integer) serialized.get("serverId");
        this.cachedName = (String) serialized.get("cachedName");
    }
    
    public abstract Object getIdentifier();
    
    public String getName() {
        return getName(false);
    }
    
    public String getName(boolean ignoreCache) {
        String name = getUncachedName();
        
        if (name != null) {
            this.cachedName = name;
            return name;
        }
        
        if (ignoreCache) {
            return null;
        }
        
        return this.cachedName;
    }
    
    protected abstract String getUncachedName();
    
    @Override
    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == this.serverId;
    }
    
    public int getServerId() {
        return this.serverId;
    }
    
    public String getServerName() {
        try {
            return (isForThisServer() ? CubeQuest.getInstance().getBungeeServerName()
                    : CubeQuest.getInstance().getDatabaseFassade().getServerName(this.serverId));
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load server name for server with id " + this.serverId, e);
            return null;
        }
    }
    
    public void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }
    
    public void makeAccessible() {
        
    }
    
    public void resetAccessible() {
        
    }
    
    public abstract boolean isLegal();
    
    public abstract String getInfo();
    
    public Location getLocation() {
        return getLocation(false);
    }
    
    public abstract Location getLocation(boolean ignoreCache);
    
    public abstract double getHeight();
    
    public abstract double getWidth();
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverId", this.serverId);
        result.put("cachedName", this.cachedName);
        return result;
    }
    
    @Override
    public int compareTo(Interactor o) {
        int result = this.getClass().getName().compareTo(o.getClass().getName());
        return result != 0 ? result : this.serverId - o.serverId;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Interactor)) {
            return false;
        }
        Interactor interact = (Interactor) other;
        
        if (this.serverId != interact.serverId) {
            return false;
        }
        if (!getIdentifier().equals(interact.getIdentifier())) {
            return false;
        }
        if (getClass() == other.getClass()) {
            return true;
        }
        
        return InteractorType.fromClass(getClass()) == InteractorType
                .fromClass(interact.getClass());
    }
    
    @Override
    public int hashCode() {
        return (31 * this.serverId) + getIdentifier().hashCode();
    }
    
}
