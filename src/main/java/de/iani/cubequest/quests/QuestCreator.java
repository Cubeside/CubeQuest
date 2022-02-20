package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestManager;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestCreator {
    
    private YamlConfiguration deserialize(String serialized) {
        if (serialized == null) {
            throw new NullPointerException();
        }
        YamlConfiguration yc = new YamlConfiguration();
        try {
            yc.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest:\n" + serialized, e);
            return null;
        }
        QuestType type = QuestType.valueOf(yc.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Invalid type!");
        }
        return yc;
    }
    
    public Quest create(int id, String serialized) {
        YamlConfiguration yc = deserialize(serialized);
        QuestType type = QuestType.valueOf(yc.getString("type"));
        Quest result;
        try {
            result = type.questClass.getConstructor(int.class).newInstance(id);
            result.deserialize(yc);
        } catch (InvalidConfigurationException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not deserialize quest with id " + id + ":\n" + serialized, e);
            return null;
        }
        if (result.isReal()) {
            CubeQuest.getInstance().getQuestManager().addQuest(result);
        }
        return result;
    }
    
    private void refresh(Quest quest, String serialized) {
        YamlConfiguration yc = deserialize(serialized);
        try {
            quest.deserialize(yc);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not deserialize quest with id " + quest.getId() + ":\n" + serialized, e);
        }
    }
    
    public <T extends Quest> T createQuest(Class<T> type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("Cannot instantiate abstract QuestClasses!");
        }
        int id;
        try {
            id = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not reserve new QuestId!", e);
            return null;
        }
        T result;
        try {
            result = type.getConstructor(int.class).newInstance(id);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create new Quest of type " + type.getName() + "!", e);
            try {
                CubeQuest.getInstance().getDatabaseFassade().deleteQuest(id);
            } catch (SQLException f) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not free reserved questId " + id + " after QuestCreation failed:", f);
            }
            return null;
        }
        QuestManager.getInstance().addQuest(result);
        updateQuest(result);
        return result;
    }
    
    public Quest loadQuest(int id) {
        if (CubeQuest.getInstance().getQuestManager().getQuest(id) != null) {
            throw new IllegalStateException("Quest already loaded!");
        }
        
        String serialized;
        try {
            serialized = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuest(id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quest with id " + id, e);
            return null;
        }
        
        return create(id, serialized);
    }
    
    
    public void loadQuests() {
        Map<Integer, String> serializedQuests;
        try {
            serializedQuests = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuests();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quests!", e);
            return;
        }
        for (int id : serializedQuests.keySet()) {
            Quest quest = QuestManager.getInstance().getQuest(id);
            try {
                if (quest == null) {
                    quest = create(id, serializedQuests.get(id));
                } else {
                    refresh(quest, serializedQuests.get(id));
                }
            } catch (Exception e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load Quest with id " + id, e);
            }
        }
    }
    
    public void refreshQuest(Quest quest) {
        if (quest == null) {
            throw new NullPointerException();
        }
        
        int id = quest.getId();
        
        String serialized;
        try {
            serialized = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuest(id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quest with id " + id, e);
            return;
        }
        if (serialized == null) {
            CubeQuest.getInstance().getQuestManager().removeQuest(id);
        }
        
        refresh(quest, serialized);
        
    }
    
    public void refreshQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        refreshQuest(quest);
    }
    
    public void updateQuest(Quest quest) {
        if (quest == null) {
            throw new NullPointerException();
        }
        
        int id = quest.getId();
        String serialized = quest.serializeToString();
        
        try {
            CubeQuest.getInstance().getDatabaseFassade().updateQuest(id, serialized);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not update quest with id " + id + ":\n" + serialized, e);
            return;
        }
        
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeInt(GlobalChatMsgType.QUEST_UPDATED.ordinal());
            msgout.writeInt(id);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
            return;
        }
        
        byte[] msgarry = msgbytes.toByteArray();
        CubeQuest.getInstance().sendToGlobalDataChannel(msgarry);
    }
    
    public void updateQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        updateQuest(quest);
    }
    
}
