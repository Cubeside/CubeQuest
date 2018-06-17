package de.iani.cubequest;

import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestType;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public class QuestManager {
    
    private static QuestManager instance;
    
    private Map<Integer, Quest> questsByIds;
    private Map<String, Set<Quest>> questsByNames;
    private Map<QuestType, Set<Quest>> questsByType;
    private Map<Integer, Set<ComplexQuest>> waitingForQuest;
    
    public static QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
        return instance;
    }
    
    private QuestManager() {
        this.questsByIds = new HashMap<>();
        this.questsByNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.questsByType = new EnumMap<>(QuestType.class);
        for (QuestType type: QuestType.values()) {
            this.questsByType.put(type, new HashSet<>());
        }
        
        this.waitingForQuest = new HashMap<>();
    }
    
    public void addQuest(Quest quest) {
        this.questsByIds.put(quest.getId(), quest);
        this.questsByType.get(QuestType.getQuestType(quest.getClass())).add(quest);
        addByName(quest);
        
        if (quest instanceof InteractorProtecting) {
            CubeQuest.getInstance().addProtecting((InteractorProtecting) quest);
        }
        
        Set<ComplexQuest> waiting = this.waitingForQuest.get(quest.getId());
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
        this.questsByType.get(QuestType.getQuestType(quest.getClass())).remove(quest);
        removeByName(quest, quest.getName());
        
        if (quest instanceof InteractorProtecting) {
            CubeQuest.getInstance().removeProtecting((InteractorProtecting) quest);
        }
    }
    
    public void removeQuest(Quest quest) {
        removeQuest(quest.getId());
    }
    
    public void deleteQuest(int id) throws QuestDeletionFailedException {
        Quest quest = this.questsByIds.get(id);
        if (quest == null) {
            throw new IllegalArgumentException("no quest with id " + id);
        }
        
        if (QuestGenerator.getInstance().getAllDailyQuests().contains(quest)) {
            throw new QuestDeletionFailedException(quest,
                    "DailyQuest " + quest + " cannot be deleted manually!");
        }
        
        QuestWouldBeDeletedEvent event = new QuestWouldBeDeletedEvent(quest);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            String[] msges = CubeQuest.getInstance().popStoredMessages();
            String msg = Arrays.stream(msges).collect(Collectors.joining("\n",
                    "The following issues prevent the deletion of this quest:\n", ""));
            throw new QuestDeletionFailedException(quest, msg);
        }
        
        try {
            quest.onDeletion();
        } catch (QuestDeletionFailedException e) {
            throw new QuestDeletionFailedException(quest,
                    "Could not delete quest " + quest + " because onDeletion failed:", e);
        }
        
        try {
            CubeQuest.getInstance().getDatabaseFassade().deleteQuest(id);
        } catch (SQLException e) {
            throw new QuestDeletionFailedException(quest,
                    "Could not delete quest " + id + " from database!", e);
        }
        
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeInt(GlobalChatMsgType.QUEST_DELETED.ordinal());
            msgout.writeInt(id);
            
            byte[] msgarry = msgbytes.toByteArray();
            CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "IOException trying to send PluginMessage!", e);
        }
        
        questDeleted(quest);
    }
    
    public void deleteQuest(Quest quest) throws QuestDeletionFailedException {
        deleteQuest(quest.getId());
    }
    
    public void questDeleted(Quest quest) {
        for (QuestGiver giver: CubeQuest.getInstance().getQuestGivers()) {
            giver.removeQuest(quest);
        }
        
        CubeQuest.getInstance().removeAutoGivenQuest(quest);
        
        for (PlayerData data: CubeQuest.getInstance().getLoadedPlayerData()) {
            data.setPlayerState(quest.getId(), null);
        }
        
        removeQuest(quest);
    }
    
    public void onQuestRenameEvent(QuestRenameEvent event) {
        removeByName(event.getQuest(), event.getQuest().getName());
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
        name = ChatAndTextUtil.stripColors(name);
        Set<Quest> result = this.questsByNames.get(name);
        if (result == null) {
            result = this.questsByNames.get(ChatAndTextUtil.convertColors(name));
            if (result == null) {
                return Collections.emptySet();
            }
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * @return alle Quests als unmodifiableCollection (live-Object der values der HashMap, keine
     *         Kopie)
     */
    public Collection<Quest> getQuests() {
        return Collections.unmodifiableCollection(this.questsByIds.values());
    }
    
    public Set<Quest> getQuests(QuestType type) {
        return Collections.unmodifiableSet(this.questsByType.get(type));
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Quest> Set<T> getQuests(Class<T> questClass) {
        return (Set<T>) getQuests(QuestType.getQuestType(questClass));
    }
    
    private void addByName(Quest quest) {
        addByName(quest, quest.getName());
    }
    
    private void addByName(Quest quest, String name) {
        name = ChatAndTextUtil.stripColors(name);
        Set<Quest> hs = this.questsByNames.get(name);
        if (hs == null) {
            hs = new HashSet<>();
            this.questsByNames.put(name, hs);
        }
        hs.add(quest);
    }
    
    private void removeByName(Quest quest, String name) {
        name = ChatAndTextUtil.stripColors(name);
        Set<Quest> hs = this.questsByNames.get(name);
        if (hs == null) {
            return;
        }
        if (!hs.remove(quest)) {
            return;
        }
        if (hs.isEmpty()) {
            this.questsByNames.remove(name);
        }
    }
    
    public void registerWaitingForQuest(ComplexQuest waiting, int waitingForId) {
        Set<ComplexQuest> hs = this.waitingForQuest.get(waitingForId);
        if (hs == null) {
            hs = new HashSet<>();
            this.waitingForQuest.put(waitingForId, hs);
        }
        hs.add(waiting);
    }
    
}
