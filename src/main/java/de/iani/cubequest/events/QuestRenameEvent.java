package de.iani.cubequest.events;

import org.bukkit.event.Cancellable;
import de.iani.cubequest.quests.Quest;

public class QuestRenameEvent extends QuestEvent implements Cancellable {
    
    private String oldName, newName;
    private boolean cancelled = false;;
    
    public QuestRenameEvent(Quest quest, String oldName, String newName) {
        super(quest);
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
    
    public void setNewName(String newVal) {
        this.newName = newVal;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean val) {
        this.cancelled = val;
    }
    
}
