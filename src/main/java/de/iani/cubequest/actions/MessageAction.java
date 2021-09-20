package de.iani.cubequest.actions;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public abstract class MessageAction extends DelayableAction {
    
    static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("\\\\PLAYERNAME");
    
    private String message;
    
    public MessageAction(long delay, String message) {
        super(delay);
        
        init(message);
    }
    
    public MessageAction(Map<String, Object> serialized) {
        super(serialized);
        
        init((String) serialized.get("message"));
    }
    
    private void init(String message) {
        this.message = Objects.requireNonNull(message);
    }
    
    public String getMessage() {
        return this.message;
    }
    
    protected String getMessage(Player player) {
        return PLAYER_NAME_PATTERN.matcher(this.message).replaceAll(player.getName());
    }
    
    protected BaseComponent[] getMessageCmp(Player player) {
        return TextComponent.fromLegacyText(getMessage(player));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("message", this.message);
        return result;
    }
}
