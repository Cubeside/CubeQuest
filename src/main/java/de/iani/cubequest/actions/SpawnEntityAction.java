package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


public class SpawnEntityAction extends LocatedAction {

    private EntityType entityType;

    public SpawnEntityAction(long delay, EntityType entityType, ActionLocation location) {
        super(delay, location);

        this.entityType = Objects.requireNonNull(entityType);
    }

    public SpawnEntityAction(Map<String, Object> serialized) {
        super(serialized);

        this.entityType = EntityType.valueOf((String) serialized.get("entityType"));
    }

    @Override
    public SpawnEntityAction relocate(ActionLocation location) {
        return new SpawnEntityAction(getDelay(), this.entityType, location);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            Location loc = getLocation().getLocation(player, data);
            loc.getWorld().spawnEntity(loc, this.entityType);
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

        TextComponent tagComp = new TextComponent("Entity: " + this.entityType + " ");
        tagComp.setColor(ChatColor.DARK_AQUA);

        TextComponent locComp = new TextComponent(getLocation().getLocationInfo(true));
        tagComp.addExtra(locComp);
        resultMsg[0].addExtra(tagComp);

        return resultMsg;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityType", this.entityType.name());
        return result;
    }

}
