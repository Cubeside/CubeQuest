package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;

public class QuestDeleteEvent extends QuestEvent {
    
    public QuestDeleteEvent(Quest quest) {
        super(quest);
    }
    
}
