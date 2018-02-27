package de.iani.cubequest;

import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.bukkit.Bukkit;

public class QuestManager {
    
    private static QuestManager instance;
    
    private Map<String, HashSet<Quest>> questsByNames;
    private Map<Integer, Quest> questsByIds;
    private Map<Integer, HashSet<ComplexQuest>> waitingForQuest;
    
    public static QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
        return instance;
    }
    
    private QuestManager() {
        this.questsByNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.questsByIds = new HashMap<>();
        this.waitingForQuest = new HashMap<>();
    }
    
    public void addQuest(Quest quest) {
        this.questsByIds.put(quest.getId(), quest);
        addByName(quest);
        
        HashSet<ComplexQuest> waiting = this.waitingForQuest.get(quest.getId());
        if (waiting != null) {
            for (ComplexQuest cq: waiting.toArray(new ComplexQuest[0])) {
                cq.informQuestNowThere(quest);
                waiting.remove(cq);
                if (waiting.isEmpty()) {
                    this.waitingForQuest.remove(quest.getId());
                }
            }
        }
    }
    
    public void removeQuest(int id) {
        Quest quest = this.questsByIds.get(id);
        if (quest == null) {
            return;
        }
        this.questsByIds.remove(id);
        removeByName(quest);
    }
    
    public void removeQuest(Quest quest) {
        removeQuest(quest.getId());
    }
    
    public boolean deleteQuest(int id) {
        Quest quest = this.questsByIds.get(id);
        if (quest == null) {
            throw new IllegalArgumentException("no quest with id " + id);
        }
        
        if (QuestGenerator.getInstance().getGeneratedDailyQuests() != null) {
            for (Quest q: QuestGenerator.getInstance().getGeneratedDailyQuests()) {
                if (quest == q) {
                    throw new RuntimeException("DailyQuest " + q + " cannot be deleted manually!");
                }
            }
        }
        
        QuestWouldBeDeletedEvent event = new QuestWouldBeDeletedEvent(quest);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return false;
        }
        
        quest.onDeletion();
        questDeleted(quest);
        
        try {
            CubeQuest.getInstance().getDatabaseFassade().deleteQuest(id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete quest " + id + " from database!", e);
        }
        
        CubeQuest.getInstance().addStoredMessage("Deleted Quest " + quest + ".");
        return true;
    }
    
    public boolean deleteQuest(Quest quest) {
        return deleteQuest(quest.getId());
    }
    
    public void questDeleted(Quest quest) {
        removeQuest(quest);
        
        for (QuestGiver giver: CubeQuest.getInstance().getQuestGivers()) {
            giver.removeQuest(quest);
        }
    }
    
    public void onQuestRenameEvent(QuestRenameEvent event) {
        removeByName(event.getQuest());
        addByName(event.getQuest(), event.getNewName());
    }
    
    public Quest getQuest(int id) {
        return this.questsByIds.get(id);
    }
    
    /**
     * Gibt alle Quests mit einem Namen zurück.
     * 
     * @param name Quests mit diesem Namen sollen zurückgegeben werden.
     * @return leeres HashSet wenn es keine Quests mit diesem Namen gibt, ein unmodifizierbares
     *         HashSet (live-Objekt) mit den Quests sonst.
     */
    public Set<Quest> getQuests(String name) {
        if (this.questsByNames.get(name) == null) {
            return new HashSet<>();
        }
        return Collections.unmodifiableSet(this.questsByNames.get(name));
    }
    
    /**
     * @return alle Quests als unmodifiableCollection (live-Object der values der HashMap, keine
     *         Kopie)
     */
    public Collection<Quest> getQuests() {
        return Collections.unmodifiableCollection(this.questsByIds.values());
    }
    
    private void addByName(Quest quest) {
        addByName(quest, quest.getName());
    }
    
    private void addByName(Quest quest, String name) {
        HashSet<Quest> hs = this.questsByNames.get(quest.getName());
        if (hs == null) {
            hs = new HashSet<>();
            this.questsByNames.put(quest.getName(), hs);
        }
        hs.add(quest);
    }
    
    private void removeByName(Quest quest) {
        HashSet<Quest> hs = this.questsByNames.get(quest.getName());
        if (hs == null) {
            return;
        }
        hs.remove(quest);
        if (hs.isEmpty()) {
            this.questsByNames.remove(quest.getName());
        }
    }
    
    public void registerWaitingForQuest(ComplexQuest waiting, int waitingForId) {
        HashSet<ComplexQuest> hs = this.waitingForQuest.get(waitingForId);
        if (hs == null) {
            hs = new HashSet<>();
            this.waitingForQuest.put(waitingForId, hs);
        }
        hs.add(waiting);
    }
    
}
