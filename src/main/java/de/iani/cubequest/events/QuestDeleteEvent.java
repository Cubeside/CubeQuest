package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;

public class QuestDeleteEvent extends QuestEvent {

    private boolean cascading;

    public QuestDeleteEvent(Quest quest, boolean cascading) {
        super(quest);

        this.cascading = cascading;
    }

    public boolean isCascading() {
        return this.cascading;
    }

}
