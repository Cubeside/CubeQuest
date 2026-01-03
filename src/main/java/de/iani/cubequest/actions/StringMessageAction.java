package de.iani.cubequest.actions;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;

public abstract class StringMessageAction extends DelayableAction {

    static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("\\\\PLAYERNAME");

    private String message;

    public StringMessageAction(long delay, String message) {
        super(delay);

        this.message = Objects.requireNonNull(message);
    }

    public StringMessageAction(Map<String, Object> serialized) {
        super(serialized);

        this.message = Objects.requireNonNull(deserializeMessage(serialized));
    }

    protected String deserializeMessage(Map<String, Object> serialized) {
        return (String) serialized.get("message");
    }

    protected void validateMessage(String message) {}

    public String getMessage() {
        return this.message;
    }

    protected String getMessage(Player player) {
        return PLAYER_NAME_PATTERN.matcher(this.message).replaceAll(player.getName());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("message", this.message);
        return result;
    }
}
