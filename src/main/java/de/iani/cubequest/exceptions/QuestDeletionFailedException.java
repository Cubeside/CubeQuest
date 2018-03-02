package de.iani.cubequest.exceptions;

import de.iani.cubequest.quests.Quest;

public class QuestDeletionFailedException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private Quest quest;
    
    public QuestDeletionFailedException(Quest quest, String message, Throwable cause) {
        super(message, cause);
        
        this.quest = quest;
    }
    
    public QuestDeletionFailedException(Quest quest, String message) {
        super(message);
        
        this.quest = quest;
    }
    
    public QuestDeletionFailedException(Quest quest, Throwable cause) {
        super(cause);
        
        this.quest = quest;
    }
    
    public Quest getQuest() {
        return this.quest;
    }
    
}
