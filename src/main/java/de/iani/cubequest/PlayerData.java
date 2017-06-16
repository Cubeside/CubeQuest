package de.iani.cubequest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;

public class PlayerData {

    private UUID id;

    private HashMap<Integer, QuestState> questStates;

    public PlayerData(UUID id, Map<Integer, QuestState> questStates) {
        this.id = id;
        this.questStates = new HashMap<Integer, QuestState>(questStates);
    }

    public PlayerData(UUID id) {
        this.id = id;
        this.questStates = new HashMap<Integer, QuestState>();

    }

    public void loadInitialData() {
        try {
            CubeQuest.getInstance().getDatabaseFassade().getQuestStates(id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load QuestStates for Player " + id.toString() + ":", e);
        }
    }

    public QuestState getPlayerState(int questId) {
        if (questStates.containsKey(id)) {
            return questStates.get(id);
        }
        QuestState result;
        try {
            result = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId, id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load QuestState for Quest " + questId + " and Player " + id.toString() + ":", e);
            return null;
        }
        return result;
    }

    public void stateChanged(int questId) {
        QuestState state = getPlayerState(questId);
        if (state == null) {
            throw new IllegalArgumentException("No state found for that questId.");
        }
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, state);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not set changed QuestState for Quest " + questId + " and Player " + id.toString() + ":", e);
        }
    }

    public Status getPlayerStatus(int questId) {
        QuestState state = getPlayerState(questId);
        return state == null? Status.NOTGIVENTO : state.getStatus();
    }

    public void setPlayerState(int questId, QuestState state) {
        if (state == null) {
            questStates.remove(questId);
        } else {
            questStates.put(questId, state);
        }
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, state);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not set QuestState for Quest " + questId + " and Player " + id.toString() + ":", e);
        }
    }

    public void addLoadedPlayerState(int questId, QuestState state) {
        questStates.put(questId, state);
    }

}
