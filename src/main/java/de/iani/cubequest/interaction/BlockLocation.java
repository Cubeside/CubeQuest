package de.iani.cubequest.interaction;

import de.iani.cubequest.util.SafeLocation;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BlockLocation implements ConfigurationSerializable, Comparable<BlockLocation> {
    
    private SafeLocation location;
    private SafeLocation centerLocation;
    
    public BlockLocation(Location location) {
        this.location = new SafeLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ());
    }
    
    public BlockLocation(String fromString) {
        fromString = fromString.substring(fromString.indexOf('{') + 1, fromString.lastIndexOf('}'));
        String[] parts = fromString.split(Pattern.quote(","));
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
        }
        
        int serverId = Integer.parseInt(parts[0]);
        String world = parts[1];
        int x = Integer.parseInt(parts[2]);
        int y = Integer.parseInt(parts[3]);
        int z = Integer.parseInt(parts[4]);
        
        this.location = new SafeLocation(serverId, world, x, y, z);
    }
    
    public BlockLocation(Map<String, Object> serialized) {
        this.location = (SafeLocation) serialized.get("location");
        if (this.location == null) {
            throw new NullPointerException();
        }
    }
    
    public Location getLocation() {
        return this.location.getLocation();
    }
    
    public Location getBottomCenterLocation() {
        if (this.centerLocation == null) {
            this.centerLocation = new SafeLocation(this.location.getWorld(), this.location.getX() + 0.5,
                    this.location.getY(), this.location.getZ() + 0.5);
        }
        return this.centerLocation.getLocation();
    }
    
    public int getX() {
        return this.location.getBlockX();
    }
    
    public int getY() {
        return this.location.getBlockY();
    }
    
    public int getZ() {
        return this.location.getBlockZ();
    }
    
    @Override
    public Map<String, Object> serialize() {
        return Collections.singletonMap("location", this.location);
    }
    
    @Override
    public int compareTo(BlockLocation other) {
        int result = this.location.getBlockX() - other.location.getBlockX();
        if (result != 0) {
            return result;
        }
        
        result = this.location.getBlockY() - other.location.getBlockY();
        if (result != 0) {
            return result;
        }
        
        return this.location.getBlockZ() - other.location.getBlockZ();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("BlockLocation:{").append("serverId=").append(this.location.getServerId())
                .append(",world=").append(this.location.getWorld()).append(",x=").append(this.location.getBlockX())
                .append(",y=").append(this.location.getBlockY()).append(",z=").append(this.location.getBlockZ())
                .append("}").toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        
        if (other instanceof Location) {
            return equals(new BlockLocation((Location) other));
        }
        
        if (!(other instanceof BlockLocation)) {
            return false;
        }
        
        return this.location.equals(((BlockLocation) other).location);
    }
    
    @Override
    public int hashCode() {
        return this.location.hashCode();
    }
    
}
