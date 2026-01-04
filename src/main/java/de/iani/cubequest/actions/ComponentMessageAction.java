package de.iani.cubequest.actions;

import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
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

        this.message = ChatAndTextUtil.getComponentOrConvert(serialized, "message");
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
        result.put("message", SerializableAdventureComponent.ofOrNull(this.message));
        return result;
    }

}
