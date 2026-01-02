package de.iani.cubequest.actions;

import de.cubeside.nmsutils.nbt.CompoundTag;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


public class SpawnEntityAction extends LocatedAction {

    private EntityType entityType;
    private long duration; // in ticks
    private CompoundTag nbtTag;

    public SpawnEntityAction(long delay, EntityType entityType, long duration, CompoundTag nbtTag,
            ActionLocation location) {
        super(delay, location);

        this.entityType = Objects.requireNonNull(entityType);
        this.duration = duration;
        this.nbtTag = nbtTag;
    }

    public SpawnEntityAction(Map<String, Object> serialized) {
        super(serialized);

        this.entityType = EntityType.valueOf(DataUpdater.updateEntityTypeName((String) serialized.get("entityType")));
        this.duration = ((Number) serialized.getOrDefault("duration", -1)).longValue();

        String nbtString = (String) serialized.get("nbtTag");
        try {
            this.nbtTag = nbtString == null ? null
                    : CubeQuest.getInstance().getNmsUtils().getNbtUtils().parseString(nbtString);
            if (this.nbtTag != null) {
                Integer nbtTagVersion = (Integer) serialized.get("nbtTagVersion");
                this.nbtTag.setString("id", this.entityType.getKey().asString());
                this.nbtTag = CubeQuest.getInstance().getNmsUtils().getNbtUtils().updateEntity(this.nbtTag,
                        nbtTagVersion != null ? nbtTagVersion : 3700); // default: data version of minecraft 1.20.4
                this.nbtTag.remove("id");
            }
        } catch (IllegalArgumentException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load NBT-String for EntityAction: " + nbtString);
        }
    }

    @Override
    public SpawnEntityAction relocate(ActionLocation location) {
        return new SpawnEntityAction(getDelay(), this.entityType, this.duration, this.nbtTag, location);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            Location loc = getLocation().getLocation(player, data);
            Entity entity = loc.getWorld().spawnEntity(loc, this.entityType);
            if (this.duration > 0) {
                entity.setPersistent(false);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                    if (entity.isValid()) {
                        entity.remove();
                    }
                }, this.duration);
            }
            if (this.nbtTag != null) {
                CubeQuest.getInstance().getNmsUtils().getEntityUtils().mergeNbt(entity, this.nbtTag);
            }
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        String durationStr = (this.duration > 0) ? ("für " + this.duration + " Ticks ") : "permanent ";

        String nbtString = (this.nbtTag == null) ? null
                : CubeQuest.getInstance().getNmsUtils().getNbtUtils().writeString(this.nbtTag);

        Component locComp = getLocation().getLocationInfo(true);

        return msg
                .append(Component.text("Entity: " + this.entityType + " ").append(Component.text(durationStr))
                        .append(Component.text("NBT: " + nbtString + " ")).append(locComp))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityType", this.entityType.name());
        result.put("duration", this.duration);
        result.put("nbtTag", this.nbtTag == null ? null
                : CubeQuest.getInstance().getNmsUtils().getNbtUtils().writeString(this.nbtTag));
        result.put("nbtTagVersion", this.nbtTag == null ? null
                : CubeQuest.getInstance().getNmsUtils().getNbtUtils().getCurrentDataVersion());
        return result;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public long getDuration() {
        return this.duration;
    }
}
