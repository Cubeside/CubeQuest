package de.iani.cubequest;

import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Pair;
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

public class PlayerData {
    
    private static final double LEVEL_FACTOR_A = 5.0 / 2.0;
    private static final double LEVEL_FACTOR_B = -725.0 / 8.0;
    private static final double LEVEL_FACTOR_C = -11.0 / 2.0;
    
    private UUID id;
    private int questPoints;
    private int xp;
    
    private Map<Integer, QuestState> questStates;
    private CopyOnWriteArrayList<QuestState> activeQuests;
    
    public static int getXpRequiredForLevel(int level) {
        return (int) Math
                .ceil(LEVEL_FACTOR_A * Math.pow(level - LEVEL_FACTOR_C, 2) + LEVEL_FACTOR_B);
    }
    
    public PlayerData(UUID id, Map<Integer, QuestState> questStates, int questPoints, int xp) {
        this.id = id;
        this.questPoints = questPoints;
        this.xp = xp;
        this.questStates = questStates == null ? new HashMap<>() : new HashMap<>(questStates);
        this.activeQuests = new CopyOnWriteArrayList<>();
        this.questStates.forEach((questId, state) -> {
            if (state.getStatus() == Status.GIVENTO) {
                this.activeQuests.addIfAbsent(state);
            }
        });
    }
    
    public PlayerData(UUID id) {
        this(id, null, 0, 0);
    }
    
    public void loadInitialData() {
        try {
            Pair<Integer, Integer> intData =
                    CubeQuest.getInstance().getDatabaseFassade().getPlayerData(this.id);
            if (intData != null) {
                this.questPoints = intData.getKey();
                this.xp = intData.getValue();
            }
            
            ArrayList<QuestState> newActive = new ArrayList<>();
            this.questStates = new HashMap<>(
                    CubeQuest.getInstance().getDatabaseFassade().getQuestStates(this.id));
            this.questStates.forEach((questId, state) -> {
                if (state.getStatus() == Status.GIVENTO) {
                    newActive.add(state);
                }
            });
            this.activeQuests = new CopyOnWriteArrayList<>(newActive);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load QuestStates for Player " + this.id.toString() + ":", e);
        }
    }
    
    public UUID getId() {
        return this.id;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }
    
    public int getQuestPoints() {
        return this.questPoints;
    }
    
    public void setQuestPoints(int value) {
        if (this.questPoints != value) {
            this.questPoints = value;
            updateDataInDatabase();
        }
    }
    
    public void changeQuestPoints(int value) {
        changeQuestPoints(value, true);
    }
    
    public void changeQuestPoints(int value, boolean update) {
        if (value != 0) {
            this.questPoints += value;
            if (update) {
                updateDataInDatabase();
            }
        }
    }
    
    public int getXp() {
        return this.xp;
    }
    
    public void setXp(int value) {
        if (this.xp != value) {
            this.xp = value;
            updateDataInDatabase();
        }
    }
    
    public void changeXp(int value) {
        changeXp(value, true);
    }
    
    public void changeXp(int value, boolean update) {
        if (value != 0) {
            int oldLevel = getLevel();
            this.xp += value;
            int newLevel = getLevel();
            if (newLevel > oldLevel) {
                Player player = getPlayer();
                if (player != null && player.isOnline()) {
                    ChatAndTextUtil.sendNormalMessage(getPlayer(),
                            "Du hast Level " + newLevel + " erreicht!");
                }
            }
            if (update) {
                updateDataInDatabase();
            }
        }
    }
    
    public void applyQuestPointsAndXP(Reward reward) {
        changeQuestPoints(reward.getQuestPoints(), false);
        changeXp(reward.getXp(), false);
        updateDataInDatabase();
    }
    
    public int getLevel() {
        return (int) Math.floor(
                Math.sqrt((1 / LEVEL_FACTOR_A) * (this.xp - LEVEL_FACTOR_B)) + LEVEL_FACTOR_C);
    }
    
    public QuestState getPlayerState(int questId) {
        if (this.questStates.containsKey(questId)) {
            return this.questStates.get(questId);
        }
        QuestState result = this.questStates.get(questId);
        if (result == null) {
            try {
                result = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId,
                        this.id);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not load QuestState for Quest " + questId + " and Player "
                                + this.id.toString() + ":",
                        e);
                return null;
            }
        }
        return result;
    }
    
    public List<QuestState> getActiveQuests() {
        return Collections.unmodifiableList(this.activeQuests);
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
        QuestState state = this.questStates.get(questId);
        return state != null && state.getStatus() == Status.GIVENTO;
    }
    
    public void setPlayerState(int questId, QuestState state) {
        QuestState oldState;
        if (state == null) {
            oldState = this.questStates.remove(questId);
        } else {
            oldState = this.questStates.put(questId, state);
        }
        addOrRemoveFromActiveQuests(questId, state);
        updateInDatabase(questId, state);
        if (oldState != null) {
            oldState.invalidate();
        }
    }
    
    public void addLoadedQuestState(int questId, QuestState state) {
        this.questStates.put(questId, state);
        addOrRemoveFromActiveQuests(questId, state);
    }
    
    private void addOrRemoveFromActiveQuests(int questId, QuestState state) {
        if (state == null || state.getStatus() != Status.GIVENTO) {
            int length = this.activeQuests.size();
            for (int i = 0; i < length; i++) {
                if (this.activeQuests.get(i).getQuest().getId() == questId) {
                    this.activeQuests.remove(i);
                    break;
                }
            }
        } else {
            this.activeQuests.addIfAbsent(state);
        }
    }
    
    public void updateDataInDatabase() {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerData(this.id, this.questPoints,
                    this.xp);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save PlayerData.", e);
        }
    }
    
    public void updateInDatabase(int questId, QuestState state) {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, this.id, state);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not set QuestState for Quest " + questId + " and Player "
                            + this.id.toString() + ":",
                    e);
        }
    }
    
}
