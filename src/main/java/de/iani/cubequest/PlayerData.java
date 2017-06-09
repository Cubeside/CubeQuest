package de.iani.cubequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        this.questStates = new HashMap<Integer, QuestState>(CubeQuest.getInstance().getDatabaseFassade().getQuestStates(id));
    }

    public QuestState getPlayerState(int questId) {
        if (questStates.containsKey(id)) {
            return questStates.get(id);
        }
        QuestState result = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId, id);
        questStates.put(questId, result);
        return result;
    }

    public void stateChanged(int questId) {
        QuestState state = getPlayerState(questId);
        if (state == null) {
            throw new IllegalArgumentException("No state found for that questId.");
        }
        CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, state.getStatus());
    }

    public Status getPlayerStatus(int questId) {
        QuestState state = getPlayerState(questId);
        return state == null? Status.NOTGIVENTO : state.getStatus();
    }

    public void setPlayerState(int questId, QuestState status) {
        if (status == null) {
            questStates.remove(questId);
        } else {
            questStates.put(questId, status);
        }
        CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, status);
    }

}
