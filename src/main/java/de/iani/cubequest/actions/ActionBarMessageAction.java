package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class ActionBarMessageAction extends MessageAction {
    
    public ActionBarMessageAction(long delay, String message) {
        super(delay, message);
    }
    
    public ActionBarMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        Runnable toRun = () -> {
            if (!player.isOnline()) {
                return;
            }
            player.sendActionBar(getMessageCmp(player));
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
        
        TextComponent tagComp = new TextComponent("Action-Bar: ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(getMessage()));
        resultMsg[0].addExtra(msgComp);
        
        return resultMsg;
    }
    
}
