package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


public class QuestWouldFreezeEvent extends QuestEvent implements Cancellable {
    
    private final Player player;
    private boolean cancelled = false;
    
    public QuestWouldFreezeEvent(Quest quest, Player player) {
        super(quest);
        this.player = player;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public void setCancelled(boolean value) {
        this.cancelled = value;
    }
    
}
