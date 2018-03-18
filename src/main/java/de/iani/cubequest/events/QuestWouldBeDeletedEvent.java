package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.event.Cancellable;


public class QuestWouldBeDeletedEvent extends QuestEvent implements Cancellable {
    
    private boolean cancelled = false;
    
    public QuestWouldBeDeletedEvent(Quest quest) {
        super(quest);
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public void setCancelled(boolean arg) {
        this.cancelled = arg;
    }
    
}
