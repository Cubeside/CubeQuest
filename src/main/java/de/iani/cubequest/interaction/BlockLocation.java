package de.iani.cubequest.interaction;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BlockLocation implements ConfigurationSerializable, Comparable<BlockLocation> {
    
    private Location location;
    private Location centerLocation;
    
    public BlockLocation(Location location) {
        this.location = new Location(location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
    }
    
    public BlockLocation(String fromString) {
        fromString = fromString.substring(fromString.indexOf('{') + 1, fromString.lastIndexOf('}'));
        String[] parts = fromString.split(Pattern.quote(","));
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
        }
        
        World world = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        
        this.location = new Location(world, x, y, z);
    }
    
    public BlockLocation(Map<String, Object> serialized) {
        this.location = (Location) serialized.get("location");
        if (this.location == null) {
            throw new NullPointerException();
        }
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public Location getBottomCenterLocation() {
        if (this.centerLocation == null) {
            this.centerLocation = new Location(this.location.getWorld(), this.location.getX() + 0.5,
                    this.location.getY(), this.location.getZ() + 0.5);
        }
        return this.centerLocation;
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
        return builder.append("BlockLocation:{").append("world=")
                .append(this.location.getWorld().getName()).append(",x=")
                .append(this.location.getBlockX()).append(",y=").append(this.location.getBlockY())
                .append(",z=").append(this.location.getBlockZ()).append("}").toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Location) {
            return equals(new BlockLocation((Location) other));
        }
        
        if (!(other instanceof BlockLocation)) {
            return false;
        }
        
        return this.location.equals(((BlockLocation) other).location);
    }
    
}
