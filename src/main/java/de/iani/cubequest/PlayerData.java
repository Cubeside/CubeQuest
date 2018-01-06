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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;

public class PlayerData {
    
    private UUID id;
    private int questPoints;
    private int xp;
    
    private HashMap<Integer, QuestState> questStates;
    private CopyOnWriteArrayList<QuestState> activeQuests;
    
    public PlayerData(UUID id, Map<Integer, QuestState> questStates, int questPoints, int xp) {
        this.id = id;
        this.questPoints = questPoints;
        this.xp = xp;
        this.questStates = questStates == null ? new HashMap<Integer, QuestState>()
                : new HashMap<Integer, QuestState>(questStates);
        this.activeQuests = new CopyOnWriteArrayList<QuestState>();
        this.questStates.forEach((questId, state) -> {
            if (state.getStatus() == Status.GIVENTO) {
                activeQuests.addIfAbsent(state);
            }
        });
    }
    
    public PlayerData(UUID id) {
        this(id, null, 0, 0);
    }
    
    public void loadInitialData() {
        try {
            ArrayList<QuestState> newActive = new ArrayList<QuestState>();
            CubeQuest.getInstance().getDatabaseFassade().getQuestStates(id)
                    .forEach((questId, state) -> {
                        questStates.put(questId, state);
                        if (state.getStatus() == Status.GIVENTO) {
                            newActive.add(state);
                        }
                    });;
            activeQuests.addAllAbsent(newActive);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load QuestStates for Player " + id.toString() + ":", e);
        }
    }
    
    public UUID getId() {
        return id;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }
    
    public int getQuestPoints() {
        return questPoints;
    }
    
    public void setQuestPoints(int value) {
        if (questPoints != value) {
            this.questPoints = value;
            this.updateDataInDatabase();
        }
    }
    
    public void changeQuestPoints(int value) {
        if (value != 0) {
            this.questPoints += value;
            this.updateDataInDatabase();
        }
    }
    
    public int getXP() {
        return xp;
    }
    
    public void setXP(int value) {
        if (xp != value) {
            this.xp = value;
            this.updateDataInDatabase();
        }
    }
    
    public void changeXP(int value) {
        if (value != 0) {
            this.xp += value;
            this.updateDataInDatabase();
        }
    }
    
    public void applyQuestPointsAndXP(Reward reward) {
        this.questPoints += reward.getQuestPoints();
        this.xp = reward.getXp();
        this.updateDataInDatabase();
    }
    
    public QuestState getPlayerState(int questId) {
        if (questStates.containsKey(id)) {
            return questStates.get(id);
        }
        QuestState result = questStates.get(questId);
        if (result == null) {
            try {
                result = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId, id);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not load QuestState for Quest " + questId + " and Player "
                                + id.toString() + ":",
                        e);
                return null;
            }
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
            throw new NullPointerException();
        }
        addOrRemoveFromActiveQuests(state.getQuest().getId(), state);
        updateInDatabase(state.getQuest().getId(), state);
    }
    
    public Status getPlayerStatus(int questId) {
        QuestState state = getPlayerState(questId);
        return state == null ? Status.NOTGIVENTO : state.getStatus();
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
            for (int i = 0; i < length; i++) {
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
    
    public void updateDataInDatabase() {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerData(id, questPoints, xp);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save PlayerData.", e);
        }
    }
    
    public void updateInDatabase(int questId, QuestState state) {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, id, state);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not set QuestState for Quest " + questId + " and Player " + id.toString()
                            + ":",
                    e);
        }
    }
    
}
