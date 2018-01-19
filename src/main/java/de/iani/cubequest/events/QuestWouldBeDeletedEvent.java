package de.iani.cubequest.events;

import org.bukkit.event.Cancellable;
import de.iani.cubequest.quests.Quest;


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
