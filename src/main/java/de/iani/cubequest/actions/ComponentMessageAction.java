package de.iani.cubequest.actions;



import de.iani.cubesideutils.ComponentUtilAdventure;
import java.text.ParseException;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public abstract class ComponentMessageAction extends MessageAction {

    public ComponentMessageAction(long delay, String message) {
        super(delay, message);
    }

    public ComponentMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }

    @Override
    protected String deserializeMessage(Map<String, Object> serialized) {
        if ((Integer) serialized.getOrDefault("version", 0) > 0) {
            return super.deserializeMessage(serialized);
        }
        return ComponentUtilAdventure.serializeComponent(ComponentUtilAdventure.getLegacyComponentSerializer()
                .deserialize(super.deserializeMessage(serialized)).compact());
    }

    @Override
    protected void validateMessage(String message) {
        try {
            ComponentUtilAdventure.deserializeComponent(message);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Component getComponentMessage() {
        try {
            return ComponentUtilAdventure.deserializeComponent(getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Component getComponentMessage(Player player) {
        try {
            return ComponentUtilAdventure.deserializeComponent(getMessage(player));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("version", 1);
        return result;
    }

}
