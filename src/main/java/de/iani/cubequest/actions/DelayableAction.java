package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public abstract class DelayableAction extends QuestAction {
    
    private long delay;
    
    public DelayableAction(long delay) {
        this.delay = delay;
    }
    
    public DelayableAction(Map<String, Object> serialized) {
        super(serialized);
        
        this.delay = ((Number) serialized.getOrDefault("delay", 0)).longValue();
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        if (this.delay == 0) {
            getActionPerformer().accept(player, data);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                if (!runIfPlayerOffline() && !player.isOnline()) {
                    return;
                }
                getActionPerformer().accept(player, data);
            }, this.delay);
        }
    }
    
    protected abstract BiConsumer<Player, PlayerData> getActionPerformer();
    
    protected boolean runIfPlayerOffline() {
        return false;
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    protected BaseComponent getDelayComponent() {
        if (this.delay == 0) {
            return null;
        }
        
        BaseComponent result = new TextComponent("Nach " + this.delay + " Ticks ");
        result.setColor(ChatColor.DARK_AQUA);
        return result;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("delay", this.delay);
        return result;
    }
    
}
