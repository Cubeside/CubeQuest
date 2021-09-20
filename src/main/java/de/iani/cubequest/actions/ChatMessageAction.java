package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class ChatMessageAction extends MessageAction {
    
    public ChatMessageAction(long delay, String message) {
        super(delay, message);
    }
    
    public ChatMessageAction(String message) {
        this(0, message);
    }
    
    public ChatMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        Runnable toRun = () -> {
            if (!player.isOnline()) {
                return;
            }
            
            TextComponent[] resultMsg = new TextComponent[1];
            resultMsg[0] = new TextComponent();
            
            TextComponent tagComp = new TextComponent(TextComponent.fromLegacyText(CubeQuest.PLUGIN_TAG));
            tagComp.addExtra(" ");
            resultMsg[0].addExtra(tagComp);
            
            TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(getMessage(player)));
            resultMsg[0].addExtra(msgComp);
            
            player.sendMessage(resultMsg);
        };
        
        if (getDelay() == 0) {
            toRun.run();
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), toRun, getDelay());
        }
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent tagComp = new TextComponent("Chat-Nachricht: ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(getMessage()));
        resultMsg[0].addExtra(msgComp);
        
        return resultMsg;
    }
    
}
