package de.iani.cubequest.actions;

import de.iani.cubesideutils.ComponentUtilAdventure;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public abstract class ComponentMessageAction extends DelayableAction {

    private Component message;

    public ComponentMessageAction(long delay, Component message) {
        super(delay);

        this.message = Objects.requireNonNull(message);
    }

    public ComponentMessageAction(Map<String, Object> serialized) {
        super(serialized);

        if (serialized.get("message") instanceof String s) {
            this.message = Objects.requireNonNull(ComponentUtilAdventure.getLegacyComponentSerializer().deserialize(s));
        } else {
            this.message = (Component) serialized.get("message");
        }
    }

    public Component getMessage() {
        return this.message;
    }

    protected Component getMessage(Player player) {
        return ComponentUtilAdventure.replacePattern(this.message, StringMessageAction.PLAYER_NAME_PATTERN,
                Component.text(player.getName()));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("message", this.message);
        return result;
    }

}
