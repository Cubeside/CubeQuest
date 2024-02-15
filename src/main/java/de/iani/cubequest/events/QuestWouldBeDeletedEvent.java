package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.event.Cancellable;


public class QuestWouldBeDeletedEvent extends QuestEvent implements Cancellable {

    private boolean cancelled = false;
    private boolean cascading;

    public QuestWouldBeDeletedEvent(Quest quest, boolean cascading) {
        super(quest);

        this.cascading = cascading;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean arg) {
        this.cancelled = arg;
    }

    public boolean isCascading() {
        return this.cascading;
    }

}
