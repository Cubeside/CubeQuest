package de.iani.cubequest.questStates;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState.Status;

public class QuestStateCreator {

    private YamlConfiguration deserialize(String serialized) {
        if (serialized == null) {
            throw new NullPointerException();
        }
        YamlConfiguration yc = new YamlConfiguration();
        if (serialized.equals("")) {
            return yc;
        }
        try {
            yc.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize QuestState:\n" + serialized, e);
            return null;
        }
        QuestStateType type = QuestStateType.valueOf(yc.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Invalid type!");
        }
        return yc;
    }

    public QuestState create(UUID playerId, int questId, Status status, String serialized) {
        if (serialized.equals("")) {
            QuestState result = new QuestState(CubeQuest.getInstance().getPlayerData(playerId), questId, status);
            CubeQuest.getInstance().getPlayerData(playerId).addLoadedQuestState(questId, result);
            return result;
        }
        YamlConfiguration yc = deserialize(serialized);
        QuestStateType type = QuestStateType.valueOf(yc.getString("type"));
        QuestState result;
        try {
            result = type.stateClass.getConstructor(PlayerData.class, int.class).newInstance(CubeQuest.getInstance().getPlayerData(playerId), questId);
            result.deserialize(yc, status);
            CubeQuest.getInstance().getPlayerData(playerId).addLoadedQuestState(questId, result);
        } catch (InvalidConfigurationException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize QuestState for Player " + playerId.toString() + " and Quest " + questId + ":\n" + serialized, e);
            return null;
        }
        return result;
    }

    /*public void refresh(QuestState questState, Status status, String serialized) {
        YamlConfiguration yc = deserialize(serialized);
        try {
            questState.deserialize(yc);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize questState:\n" + serialized, e);
        }
    }*/

    /*public QuestState loadQuestState(UUID playerId, int questId) {
        String serialized;
        try {
            serialized = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId, playerId)
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
        for (int id: serializedQuests.keySet()) {
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                quest = create(id, serializedQuests.get(id));
            } else {
                refresh(quest, serializedQuests.get(id));
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
        String serialized = quest.serialize();

        try {
            CubeQuest.getInstance().getDatabaseFassade().updateQuest(id, serialized);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not update quest with id " + id + ":\n" + serialized, e);
        }
    }

    public void updateQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        updateQuest(quest);
    }*/
}
