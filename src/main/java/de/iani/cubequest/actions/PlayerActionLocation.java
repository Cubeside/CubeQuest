package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.Locatable;
import de.iani.cubesideutils.bukkit.Locatable.EntityWrapper;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public Component getLocationInfo(boolean includePreposition) {
        Component result = Component.text("Spielerposition", NamedTextColor.GREEN);
        if (includePreposition) {
            result = Component.textOfChildren(Component.text("an "), result);
        }

        if (this.offsetX != 0.0 || this.offsetY != 0.0 || this.offsetZ != 0.0) {
            result = Component.textOfChildren(result, Component.text(" + ("),
                    Component.text(this.offsetX, NamedTextColor.GREEN), Component.text(", "),
                    Component.text(this.offsetY, NamedTextColor.GREEN), Component.text(", "),
                    Component.text(this.offsetZ, NamedTextColor.GREEN), Component.text(")"));
        }

        return result.color(NamedTextColor.DARK_AQUA);
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
