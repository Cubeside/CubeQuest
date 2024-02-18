package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import java.util.LinkedHashSet;
import java.util.Set;

public class QuestDeleteEvent extends QuestEvent {

    private boolean cascading;
    private Set<Quest> cascadingSet;

    public QuestDeleteEvent(Quest quest, boolean cascading) {
        super(quest);

        this.cascading = cascading;
        this.cascadingSet = cascading ? new LinkedHashSet<>(Set.of(quest)) : null;
    }

    public boolean isCascading() {
        return this.cascading;
    }

    public void addCascadinglyDeleted(Set<Quest> quests) {
        this.cascadingSet.addAll(quests);
    }

    public void addCascadinglyDeleted(Quest quest) {
        this.cascadingSet.add(quest);
    }

    public Set<Quest> getCascadinglyDeleted() {
        return this.cascadingSet;
    }

}
