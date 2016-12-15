package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class QuestManager {

    private HashMap<String, Quest> allQuests;

    public QuestManager() {
        allQuests = new HashMap<String, Quest>();
    }

    public boolean isQuestName(String name) {
        return allQuests.containsKey(name);
    }

    public void addQuest(Quest quest) {
        if (allQuests.containsKey(quest.getName())) throw new IllegalArgumentException("Quest-name already given.");
        allQuests.put(quest.getName(), quest);
    }

    public Quest getQuest(String name) {
        return allQuests.get(name);
    }

    /**
     * @return alle Quests als unmodifiableCollection (live-Object der values der HashMap, keine Kopie)
     */
    public Collection<Quest> getQuests() {
        return Collections.unmodifiableCollection(allQuests.values());
    }

}
