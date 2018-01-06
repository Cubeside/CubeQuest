package de.iani.cubequest.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import de.iani.cubequest.quests.Quest;

public abstract class QuestEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private Quest quest;
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public QuestEvent(Quest quest) {
        this.quest = quest;
    }
    
    public Quest getQuest() {
        return quest;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
