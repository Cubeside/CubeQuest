package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;


public class MessageAction extends QuestAction {
    
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("\\\\PLAYERNAME");
    
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
        String individualMessage = PLAYER_NAME_PATTERN.matcher(this.message).replaceAll(player.getName());
        
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        TextComponent tagComp = new TextComponent(TextComponent.fromLegacyText(CubeQuest.PLUGIN_TAG));
        tagComp.addExtra(" ");
        resultMsg[0].addExtra(tagComp);
        
        TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(individualMessage));
        resultMsg[0].addExtra(msgComp);
        
        player.sendMessage(resultMsg);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        TextComponent tagComp = new TextComponent("Nachricht: ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(this.message));
        resultMsg[0].addExtra(msgComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("message", this.message);
        return result;
    }
    
}
