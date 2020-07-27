package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.Locatable;
import de.iani.cubesideutils.bukkit.Locatable.EntityWrapper;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class PlayerActionLocation extends ActionLocation {
    
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    
    public PlayerActionLocation(double offsetX, double offsetY, double offsetZ) {
        init(offsetX, offsetY, offsetZ);
    }
    
    public PlayerActionLocation(Map<String, Object> serialized) {
        super(serialized);
        
        init(((Number) serialized.get("offsetX")).doubleValue(), ((Number) serialized.get("offsetY")).doubleValue(),
                ((Number) serialized.get("offsetZ")).doubleValue());
    }
    
    private void init(double offsetX, double offsetY, double offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }
    
    @Override
    public Locatable getLocatable(Player player, PlayerData data) {
        return new EntityWrapper(player, this.offsetX, this.offsetY, this.offsetZ);
    }
    
    @Override
    public Location getLocation(Player player, PlayerData data) {
        return player.getLocation().add(this.offsetX, this.offsetY, this.offsetZ);
    }
    
    @Override
    public BaseComponent[] getLocationInfo(boolean includePreposition) {
        ComponentBuilder cb = new ComponentBuilder((includePreposition ? "an " : "")).append("Spielerposition");
        if (this.offsetX != 0.0 || this.offsetY != 0.0 || this.offsetZ != 0.0) {
            cb.append(" + (" + this.offsetX + ", " + this.offsetY + ", " + this.offsetZ + ")");
        }
        return cb.create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("offsetX", this.offsetX);
        result.put("offsetY", this.offsetY);
        result.put("offsetZ", this.offsetZ);
        
        return result;
    }
    
}
