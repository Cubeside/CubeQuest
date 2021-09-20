package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;


public class BossBarMessageAction extends MessageAction {
    
    private BarColor color;
    private BarStyle style;
    private long duration;
    
    public BossBarMessageAction(long delay, String message, BarColor color, BarStyle style, long duration) {
        super(delay, message);
        
        this.color = Objects.requireNonNull(color);
        this.style = Objects.requireNonNull(style);
        this.duration = duration;
    }
    
    public BossBarMessageAction(Map<String, Object> serialized) {
        super(serialized);
        
        this.color = BarColor.valueOf((String) serialized.get("color"));
        this.style = BarStyle.valueOf((String) serialized.get("style"));
        this.duration = ((Number) serialized.get("duration")).longValue();
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        Runnable toRun = () -> {
            if (!player.isOnline()) {
                return;
            }
            BossBar bar = Bukkit.createBossBar(getMessage(player), this.color, this.style);
            bar.addPlayer(player);
            bar.setVisible(true);
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                bar.setVisible(false);
            }, this.duration);
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
        
        TextComponent tagComp =
                new TextComponent("Boss-Bar (" + this.color + ", " + this.style + ", " + this.duration + " Ticks): ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        TextComponent msgComp = new TextComponent(TextComponent.fromLegacyText(getMessage()));
        resultMsg[0].addExtra(msgComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("color", this.color.name());
        result.put("style", this.style.name());
        result.put("duration", this.duration);
        return result;
    }
    
}
