package de.iani.cubequest.interaction;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import de.iani.cubequest.CubeQuest;

public abstract class Interactor implements Comparable<Interactor>, ConfigurationSerializable {
    
    public static final Comparator<Interactor> COMPARATOR = (i1, i2) -> {
        if (i1 == null) {
            return i2 == null ? 0 : -1;
        } else {
            return i2 == null ? 1 : i1.compareTo(i2);
        }
    };
    
    private int serverId;
    
    public Interactor() {
        serverId = CubeQuest.getInstance().getServerId();
    }
    
    public Interactor(int serverId) {
        this.serverId = serverId;
    }
    
    public Interactor(Map<String, Object> serialized) {
        serverId = (Integer) serialized.get("serverId");
    }
    
    public abstract Object getIdentifier();
    
    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == serverId;
    }
    
    public int getServerId() {
        return serverId;
    }
    
    public String getServerName() {
        try {
            return (isForThisServer() ? CubeQuest.getInstance().getBungeeServerName()
                    : CubeQuest.getInstance().getDatabaseFassade().getServerName(serverId));
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load server name for server with id " + serverId, e);
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
    
    public abstract Location getLocation();
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverId", serverId);
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Interactor)) {
            return false;
        }
        
        return getIdentifier().equals(((Interactor) other).getIdentifier());
    }
    
    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }
    
}
