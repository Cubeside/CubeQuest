package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.Locatable;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class ActionLocation implements ConfigurationSerializable {
    
    public ActionLocation() {
        
    }
    
    public ActionLocation(Map<String, Object> serialized) {
        
    }
    
    public abstract Locatable getLocatable(Player player, PlayerData data);
    
    public Location getLocation(Player player, PlayerData data) {
        return getLocatable(player, data).getLocation();
    }
    
    public abstract BaseComponent[] getLocationInfo(boolean includePreposition);
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        return result;
    }
    
}
