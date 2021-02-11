package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


public class SpawnEntityAction extends LocatedAction {
    
    private EntityType entityType;
    
    public SpawnEntityAction(EntityType entityType, ActionLocation location) {
        super(location);
        
        this.entityType = Objects.requireNonNull(entityType);
    }
    
    public SpawnEntityAction(Map<String, Object> serialized) {
        super(serialized);
        
        this.entityType = EntityType.valueOf((String) serialized.get("entityType"));
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        Location loc = getLocation().getLocation(player, data);
        loc.getWorld().spawnEntity(loc, this.entityType);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder("Entity: " + this.entityType + " bei ").color(ChatColor.DARK_AQUA)
                .append(getLocation().getLocationInfo(true)).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityType", this.entityType.name());
        return result;
    }
    
}
