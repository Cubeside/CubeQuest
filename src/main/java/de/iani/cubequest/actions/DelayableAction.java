package de.iani.cubequest.actions;

import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;


public abstract class DelayableAction extends QuestAction {
    
    private long delay;
    
    public DelayableAction(long delay) {
        this.delay = delay;
    }
    
    public DelayableAction(Map<String, Object> serialized) {
        super(serialized);
        
        this.delay = ((Number) serialized.getOrDefault("delay", 0)).longValue();
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
