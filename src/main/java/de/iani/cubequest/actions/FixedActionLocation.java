package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubesideutils.bukkit.Locatable;
import de.iani.cubesideutils.bukkit.Locatable.LocationWrapper;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FixedActionLocation extends ActionLocation {

    private SafeLocation location;

    public FixedActionLocation(SafeLocation location) {
        this.location = Objects.requireNonNull(location);
    }

    public FixedActionLocation(Location location) {
        this.location = new SafeLocation(Objects.requireNonNull(location));
    }

    public FixedActionLocation(Map<String, Object> serialized) {
        super(serialized);

        this.location = (SafeLocation) serialized.get("location");
    }

    public SafeLocation getLocation() {
        return this.location;
    }

    @Override
    public Locatable getLocatable(Player player, PlayerData data) {
        return new LocationWrapper(getLocation().getLocation());
    }

    @Override
    public Location getLocation(Player player, PlayerData data) {
        return this.location.getLocation();
    }

    @Override
    public Component getLocationInfo(boolean includePreposition) {
        Component prefix = includePreposition ? Component.text("bei ") : Component.empty();
        return prefix.append(Component.text(ChatAndTextUtil.getLocationInfo(getLocation())))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("location", this.location);
        return result;
    }

}
