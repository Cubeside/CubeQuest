package de.iani.cubequest.events;

import de.iani.cubequest.Reward;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;


public class QuestRewardDeliveredEvent extends PlayerEvent {
    
    private static final HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    private final Reward reward;
    private final boolean payedDirectly;
    
    public QuestRewardDeliveredEvent(Player player, Reward reward, boolean payedDirectly) {
        super(player);
        
        this.reward = Objects.requireNonNull(reward);
        this.payedDirectly = payedDirectly;
    }
    
    public Reward getReward() {
        return this.reward;
    }
    
    public boolean isPayedDirectly() {
        return this.payedDirectly;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
