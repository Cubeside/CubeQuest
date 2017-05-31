package de.iani.cubequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.iani.cubequest.quests.Quest.Status;

public class PlayerData {

    private UUID id;

    private HashMap<Integer, Status> questStates;

    public PlayerData(UUID id, Map<Integer, Status> questStates) {
        this.id = id;
        this.questStates = new HashMap<Integer, Status>(questStates);
    }

    public PlayerData(UUID id) {
        this.id = id;
        this.questStates = new HashMap<Integer, Status>(CubeQuest.getInstance().getDatabaseFassade().getQuestStates(id));
    }

    public Status getPlayerStatus(int questId) {
        if (questStates.containsKey(id)) {
            return questStates.get(id);
        }
        Status result = CubeQuest.getInstance().getDatabaseFassade().getPlayerStatus(questId, id);
        questStates.put(questId, result);
        return result != null? result : Status.NOTGIVENTO;
    }

    public void setPlayerStatus(int questId, Status status) {
        if (status == null) {
            questStates.remove(questId);
        } else {
            questStates.put(questId, status);
        }
        CubeQuest.getInstance().getDatabaseFassade().setPlayerStatus(questId, id, status);
    }
}
