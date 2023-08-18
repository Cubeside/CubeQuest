package de.iani.cubequest.actions;

import de.cubeside.nmsutils.nbt.CompoundTag;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


public class SpawnEntityAction extends LocatedAction {
    
    private EntityType entityType;
    private CompoundTag nbtTag;
    
    public SpawnEntityAction(long delay, EntityType entityType, CompoundTag nbtTag, ActionLocation location) {
        super(delay, location);
        
        this.entityType = Objects.requireNonNull(entityType);
        this.nbtTag = nbtTag;
    }
    
    public SpawnEntityAction(Map<String, Object> serialized) {
        super(serialized);
        
        this.entityType = EntityType.valueOf((String) serialized.get("entityType"));
        
        String nbtString = (String) serialized.get("nbtTag");
        try {
            this.nbtTag = nbtString == null ? null
                    : CubeQuest.getInstance().getNmsUtils().getNbtUtils().parseString(nbtString);
        } catch (IllegalArgumentException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load NBT-String for EntityAction: " + nbtString);
        }
    }
    
    @Override
    public SpawnEntityAction relocate(ActionLocation location) {
        return new SpawnEntityAction(getDelay(), this.entityType, this.nbtTag, location);
    }
    
    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            Location loc = getLocation().getLocation(player, data);
            Entity entity = loc.getWorld().spawnEntity(loc, this.entityType);
            if (this.nbtTag != null) {
                CubeQuest.getInstance().getNmsUtils().getEntityUtils().mergeNbt(entity, this.nbtTag);
            }
        };
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent typeComp = new TextComponent("Entity: " + this.entityType + " ");
        typeComp.setColor(ChatColor.DARK_AQUA);
        
        String nbtString = this.nbtTag == null ? null
                : CubeQuest.getInstance().getNmsUtils().getNbtUtils().writeString(this.nbtTag);
        TextComponent nbtComp = new TextComponent("NBT: " + nbtString + " ");
        typeComp.addExtra(nbtComp);
        
        TextComponent locComp = new TextComponent(getLocation().getLocationInfo(true));
        typeComp.addExtra(locComp);
        resultMsg[0].addExtra(typeComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityType", this.entityType.name());
        result.put("nbtTag", this.nbtTag == null ? null
                : CubeQuest.getInstance().getNmsUtils().getNbtUtils().writeString(this.nbtTag));
        return result;
    }
    
}
