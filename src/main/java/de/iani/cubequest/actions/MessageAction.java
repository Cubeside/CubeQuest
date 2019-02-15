package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class MessageAction extends QuestAction {
    
    private String message;
    
    public MessageAction(String message) {
        init(message);
    }
    
    public MessageAction(Map<String, Object> serialized) {
        init((String) serialized.get("message"));
    }
    
    private void init(String message) {
        this.message = Objects.requireNonNull(message);
    }
    
    public String getMessage() {
        return this.message;
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        player.sendMessage(CubeQuest.PLUGIN_TAG + " " + this.message);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(ChatColor.DARK_AQUA + "Nachricht: ").append(this.message)
                .reset().create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("message", this.message);
        return result;
    }
    
}