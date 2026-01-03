package de.iani.cubequest.interaction;

import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityInteractor extends Interactor {

    private UUID entityId;
    private SafeLocation cachedLocation;

    public EntityInteractor(Entity entity) {
        this.entityId = entity.getUniqueId();
    }

    public EntityInteractor(UUID entityId) {
        this.entityId = entityId;
    }

    public EntityInteractor(Map<String, Object> serialized) {
        super(serialized);

        String idString = (String) serialized.get("entityId");
        this.entityId = idString == null ? null : UUID.fromString(idString);
        this.cachedLocation = (SafeLocation) serialized.get("cachedLocation");
    }

    @Override
    public UUID getIdentifier() {
        return this.entityId;
    }

    public Entity getEntity() {
        return Bukkit.getEntity(this.entityId);
    }

    @Override
    protected String getUncachedName() {
        if (!isForThisServer()) {
            return null;
        }

        Entity entity = getEntity();
        return entity == null ? null : entity.getName();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityId", this.entityId == null ? null : this.entityId.toString());
        result.put("cachedLocation", this.cachedLocation);
        return result;
    }

    @Override
    public boolean isLegal() {
        return this.entityId != null;
    }

    @Override
    public Component getInfo() {
        Component result = ChatAndTextUtil.getEntityInfoComponent(getServerId(), this.entityId);

        if (isLegal() && Bukkit.getEntity(this.entityId) == null && this.cachedLocation != null) {
            result = result.append(Component.text(" (cachedLocation: ", NamedTextColor.DARK_AQUA))
                    .append(ChatAndTextUtil.getLocationInfo(ChatAndTextUtil.roundLocation(this.cachedLocation, 0)))
                    .append(Component.text(")", NamedTextColor.DARK_AQUA));
        }

        return result.color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Location getLocation(boolean ignoreCache) {
        Entity entity = getEntity();
        Location loc = entity == null ? null : entity.getLocation();

        if (loc != null) {
            SafeLocation oldCachedLocation = this.cachedLocation;
            this.cachedLocation = new SafeLocation(loc);

            if (oldCachedLocation == null || !oldCachedLocation.isSimilar(loc)) {
                cacheChanged();
            }
        } else if (!ignoreCache && this.cachedLocation != null) {
            loc = this.cachedLocation.getLocation();
        }

        return loc;
    }

    @Override
    public double getHeight() {
        Entity entity = getEntity();
        return entity != null ? entity.getHeight() : 2;
    }

    @Override
    public double getWidth() {
        Entity entity = getEntity();
        return entity != null ? entity.getWidth() : 1;
    }

    @Override
    public int compareTo(Interactor o) {
        int result = this.getClass().getName().compareTo(o.getClass().getName());

        if (result != 0) {
            return result;
        }
        assert (this.getClass() == o.getClass());

        boolean idNull = getIdentifier() == null;
        if (idNull) {
            result--;
        } else if (o.getIdentifier() == null) {
            result++;
        }

        if (result != 0 || idNull) {
            return result;
        }

        return getIdentifier().compareTo((UUID) o.getIdentifier());
    }

}
