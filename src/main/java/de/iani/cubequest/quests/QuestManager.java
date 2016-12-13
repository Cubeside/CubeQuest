package de.iani.cubequest.quests;

import java.util.HashMap;
import java.util.HashSet;

public class QuestManager {

    private HashMap<String, Quest> allQuests;

    public QuestManager() {

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

    public HashSet<Quest> getQuests() {
        HashSet<Quest> result = new HashSet<Quest>();
        for (String s: allQuests.keySet()) result.add(allQuests.get(s));
        return result;
    }

}
