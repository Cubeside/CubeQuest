package de.iani.cubequest.util;


import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigurationUtils {
    
    public static Location getLocation(ConfigurationSection section, String name, Server server) {
        ConfigurationSection subsection = section.getConfigurationSection(name);
        if (subsection == null) {
            return null;
        }
        String worldName = subsection.getString("world");
        World world = worldName != null && server != null ? server.getWorld(worldName) : null;
        double x = subsection.getDouble("x");
        double y = subsection.getDouble("y");
        double z = subsection.getDouble("z");
        float yaw = (float) subsection.getDouble("yaw");
        float pitch = (float) subsection.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public static void setLocation(ConfigurationSection section, String name, Location location) {
        setLocation(section, name, location, false);
    }
    
    public static void setLocation(ConfigurationSection section, String name, Location location,
            boolean includeYawAndPitch) {
        if (location != null) {
            ConfigurationSection subsection = section.createSection(name);
            if (location.getWorld() != null) {
                subsection.set("world", location.getWorld().getName());
            }
            subsection.set("x", location.getX());
            subsection.set("y", location.getY());
            subsection.set("z", location.getZ());
            if (includeYawAndPitch) {
                subsection.set("yaw", location.getYaw());
                subsection.set("pitch", location.getPitch());
            }
        }
    }
}
