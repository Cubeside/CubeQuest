package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.event.Cancellable;


public class QuestSetReadyEvent extends QuestEvent implements Cancellable {
    
    private boolean setReady;
    private boolean cancelled;
    
    public QuestSetReadyEvent(Quest quest, boolean setReady) {
        super(quest);
        
        this.setReady = setReady;
        this.cancelled = false;
    }
    
    public boolean getSetReady() {
        return this.setReady;
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
}
