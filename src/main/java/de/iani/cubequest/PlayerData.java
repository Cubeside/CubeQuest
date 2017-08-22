package de.iani.cubequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;

public class PlayerData {

    private UUID id;

    private HashMap<Integer, QuestState> questStates;
    private CopyOnWriteArrayList<QuestState> activeQuests;

    public PlayerData(UUID id, Map<Integer, QuestState> questStates) {
        this.id = id;
        this.questStates = new HashMap<Integer, QuestState>(questStates);
        this.activeQuests = new CopyOnWriteArrayList<QuestState>();
    }

    public PlayerData(UUID id) {
        this.id = id;
        this.questStates = new HashMap<Integer, QuestState>();

    }

    public void loadInitialData() {
        try {
            ArrayList<QuestState> newActive = new ArrayList<QuestState>();
            CubeQuest.getInstance().getDatabaseFassade().getQuestStates(id).forEach((questId, state) -> {
                questStates.put(questId, state);
                if (state.getStatus() == Status.GIVENTO) {
                    newActive.add(state);
                }
                activeQuests.addAll(newActive);
            });;
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

    public List<QuestState> getActiveQuests() {
        return Collections.unmodifiableList(activeQuests);
    }

    public void stateChanged(int questId) {
        QuestState state = getPlayerState(questId);
        stateChanged(state);
    }

    public void stateChanged(QuestState state) {
        if (state == null) {
            throw new IllegalArgumentException("No state found for that questId.");
        }
        updateInDatabase(state.getQuest().getId(), state);
    }

    public Status getPlayerStatus(int questId) {
        QuestState state = getPlayerState(questId);
        return state == null? Status.NOTGIVENTO : state.getStatus();
    }

    public boolean isGivenTo(int questId) {
        QuestState state = questStates.get(questId);
        return state != null && state.getStatus() == Status.GIVENTO;
    }

    public void setPlayerState(int questId, QuestState state) {
        if (state == null) {
            questStates.remove(questId);
        } else {
            questStates.put(questId, state);
        }
        addOrRemoveFromActiveQuests(questId, state);
        updateInDatabase(questId, state);
    }

    public void addLoadedQuestState(int questId, QuestState state) {
        questStates.put(questId, state);
        addOrRemoveFromActiveQuests(questId, state);
    }

    private void addOrRemoveFromActiveQuests(int questId, QuestState state) {
        if (state == null) {
            int length = activeQuests.size();
            for (int i=0; i<length; i++) {
                if (activeQuests.get(i).getQuest().getId() == questId) {
                    activeQuests.remove(i);
                    break;
                }
            }
        } else {
            if (state.getStatus() == Status.GIVENTO) {
                activeQuests.addIfAbsent(state);
            } else {
                activeQuests.remove(state);
            }
        }
    }

    public void updateInDatabase(int questId, QuestState state) {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, state);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not set QuestState for Quest " + questId + " and Player " + id.toString() + ":", e);
        }

    }

}
