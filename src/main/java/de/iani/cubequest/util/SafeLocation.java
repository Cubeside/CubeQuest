package de.iani.cubequest.util;

import de.iani.cubequest.CubeQuest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class SafeLocation implements ConfigurationSerializable, Comparable<SafeLocation> {
    
    private int serverId;
    private String world;
    private double x, y, z;
    private float yaw, pitch;
    
    public SafeLocation(int serverId, String world, double x, double y, double z, float yaw,
            float pitch) {
        super();
        this.serverId = serverId;
        this.world = Objects.requireNonNull(world);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public SafeLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this(CubeQuest.getInstance().getServerId(), world, x, y, z, yaw, pitch);
    }
    
    public SafeLocation(int serverId, String world, double x, double y, double z) {
        this(serverId, world, x, y, z, 0.0f, 0.0f);
    }
    
    public SafeLocation(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }
    
    public SafeLocation(Location loc) {
        this(CubeQuest.getInstance().getServerId(), loc.getWorld().getName(), loc.getX(),
                loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    
    public SafeLocation(Map<String, Object> serialized) {
        this.serverId = (Integer) serialized.get("serverId");
        this.world = Objects.requireNonNull((String) serialized.get("world"));
        this.x = (Double) serialized.get("x");
        this.y = (Double) serialized.get("y");
        this.z = (Double) serialized.get("z");
        this.yaw = serialized.containsKey("yaw") ? (Float) serialized.get("yaw") : 0.0f;
        this.pitch = serialized.containsKey("pitch") ? (Float) serialized.get("pitch") : 0.0f;
    }
    
    public Location getLocation() {
        return this.serverId != CubeQuest.getInstance().getServerId() ? null
                : new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw,
                        this.pitch);
    }
    
    public int getServerId() {
        return this.serverId;
    }
    
    public String getWorld() {
        return this.world;
    }
    
    public World getBukkitWorld() {
        return this.serverId != CubeQuest.getInstance().getServerId() ? null
                : Bukkit.getWorld(this.world);
    }
    
    public double getX() {
        return this.x;
    }
    
    public int getBlockX() {
        return (int) Math.floor(this.x);
    }
    
    public double getY() {
        return this.y;
    }
    
    public int getBlockY() {
        return (int) Math.floor(this.y);
    }
    
    public double getZ() {
        return this.z;
    }
    
    public int getBlockZ() {
        return (int) Math.floor(this.z);
    }
    
    public float getYaw() {
        return this.yaw;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>(7);
        result.put("serverId", this.serverId);
        result.put("world", this.world);
        result.put("x", this.x);
        result.put("y", this.y);
        result.put("z", this.z);
        if (this.yaw != 0.0f) {
            result.put("yaw", this.yaw);
        }
        if (this.pitch != 0.0f) {
            result.put("pitch", this.pitch);
        }
        return result;
    }
    
    @Override
    public int compareTo(SafeLocation other) {
        int result = this.serverId - other.serverId;
        if (result != 0) {
            return result;
        }
        
        result = this.world.compareTo(other.world);
        if (result != 0) {
            return result;
        }
        
        result = Double.compare(this.x, other.x);
        if (result != 0) {
            return result;
        }
        
        result = Double.compare(this.y, other.y);
        if (result != 0) {
            return result;
        }
        
        result = Double.compare(this.z, other.z);
        if (result != 0) {
            return result;
        }
        
        result = Float.compare(this.yaw, other.yaw);
        if (result != 0) {
            return result;
        }
        
        result = Float.compare(this.pitch, other.pitch);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SafeLocation[");
        builder.append("serverId: ").append(this.serverId);
        builder.append(", ").append("world: ").append(this.world);
        builder.append(", ").append("x: ").append(this.x);
        builder.append(", ").append("y: ").append(this.y);
        builder.append(", ").append("z: ").append(this.z);
        if (this.yaw != 0.0f || this.pitch != 0.0f) {
            builder.append(", ").append("yaw: ").append(this.yaw);
            builder.append(", ").append("pitch: ").append(this.pitch);
        }
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SafeLocation)) {
            return false;
        }
        
        SafeLocation loc = (SafeLocation) other;
        if (this.serverId != loc.serverId) {
            return false;
        }
        if (!(this.world.equals(loc.world))) {
            return false;
        }
        if (this.x != loc.x) {
            return false;
        }
        if (this.y != loc.y) {
            return false;
        }
        if (this.z != loc.z) {
            return false;
        }
        if (this.yaw != loc.yaw) {
            return false;
        }
        if (this.pitch != loc.pitch) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = this.serverId;
        result = 31 * result + this.world.hashCode();
        result = 31 * result + Double.hashCode(this.x);
        result = 31 * result + Double.hashCode(this.y);
        result = 31 * result + Double.hashCode(this.z);
        result = 31 * result + Float.hashCode(this.yaw);
        result = 31 * result + Float.hashCode(this.pitch);
        return result;
    }
    
}
