package de.iani.cubequest;

import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Pair;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerData {
    
    private static final double LEVEL_FACTOR_A = 5.0 / 2.0;
    private static final double LEVEL_FACTOR_B = -725.0 / 8.0;
    private static final double LEVEL_FACTOR_C = -11.0 / 2.0;
    
    private static final long CACHED_STATES_UPDATE_DELAY = 60 * 20; // 1ms in ticks
    
    private UUID id;
    private int questPoints;
    private int xp;
    
    private Map<Integer, QuestState> questStates;
    private CopyOnWriteArrayList<QuestState> activeQuests;
    private Set<Integer> cachedStates;
    private BukkitRunnable updateCachedStatesTask;
    
    private Deque<Reward> delayedRewards;
    private int payRewardsTimerId = -1;
    
    private int pendingRegivings = 0;
    
    public static int getXpRequiredForLevel(int level) {
        return (int) Math.ceil(LEVEL_FACTOR_A * Math.pow(level - LEVEL_FACTOR_C, 2) + LEVEL_FACTOR_B);
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
        
        this.cachedStates = new HashSet<>();
        this.updateCachedStatesTask = null;
    }
    
    public PlayerData(UUID id) {
        this(id, null, 0, 0);
    }
    
    public void loadInitialData() {
        try {
            Pair<Integer, Integer> intData = CubeQuest.getInstance().getDatabaseFassade().getPlayerData(this.id);
            if (intData != null) {
                this.questPoints = intData.getKey();
                this.xp = intData.getValue();
            }
            
            ArrayList<QuestState> newActive = new ArrayList<>();
            this.questStates = new HashMap<>(CubeQuest.getInstance().getDatabaseFassade().getQuestStates(this.id));
            this.questStates.forEach((questId, state) -> {
                if (state.getStatus() == Status.GIVENTO) {
                    newActive.add(state);
                }
            });
            this.activeQuests = new CopyOnWriteArrayList<>(newActive);
            this.cachedStates.clear();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load QuestStates for Player " + this.id.toString() + ":", e);
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
        try {
            this.questPoints = CubeQuest.getInstance().getDatabaseFassade().changePlayerQuestPoints(this.id, true, value);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not change PlayerData.", e);
        }
    }
    
    public void changeQuestPoints(int value) {
        try {
            this.questPoints = CubeQuest.getInstance().getDatabaseFassade().changePlayerQuestPoints(this.id, false, value);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not change PlayerData.", e);
        }
    }
    
    public int getXp() {
        return this.xp;
    }
    
    public void setXp(int value) {
        try {
            this.xp = CubeQuest.getInstance().getDatabaseFassade().changePlayerXp(this.id, true, value);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not change PlayerData.", e);
        }
    }
    
    public void changeXp(int value) {
        int oldLevel = getLevel();
        try {
            this.xp = CubeQuest.getInstance().getDatabaseFassade().changePlayerXp(this.id, false, value);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not change PlayerData.", e);
        }
        int newLevel = getLevel();
        if (newLevel > oldLevel) {
            Player player = getPlayer();
            if (player != null && player.isOnline()) {
                ChatAndTextUtil.sendNormalMessage(getPlayer(), "Du hast Level " + newLevel + " erreicht!");
            }
        }
    }
    
    public void applyQuestPointsAndXP(Reward reward) {
        changeQuestPoints(reward.getQuestPoints());
        changeXp(reward.getXp());
    }
    
    public int getLevel() {
        return (int) Math.floor(Math.sqrt((1 / LEVEL_FACTOR_A) * (this.xp - LEVEL_FACTOR_B)) + LEVEL_FACTOR_C);
    }
    
    public QuestState getPlayerState(int questId) {
        QuestState result = this.questStates.get(questId);
        if (result == null) {
            try {
                result = CubeQuest.getInstance().getDatabaseFassade().getPlayerState(questId, this.id);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not load QuestState for Quest " + questId + " and Player " + this.id.toString() + ":", e);
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
        if (addOrRemoveFromActiveQuests(state.getQuest().getId(), state) || state.getStatus() != Status.GIVENTO
                || !(state instanceof AmountQuestState)) {
            updateInDatabase(state.getQuest().getId(), state);
        }
        
        // May cache and delay database update.
        if (!this.cachedStates.add(state.getQuest().getId())) {
            // Already cached, nothing to do right now.
            return;
        }
        if (this.updateCachedStatesTask != null) {
            // Update task already running, nothing to do right now.
            return;
        }
        // Schedule update task.
        this.updateCachedStatesTask = new BukkitRunnable() {
            
            @Override
            public void run() {
                updateCachedStates();
            }
            
        };
        this.updateCachedStatesTask.runTaskLater(CubeQuest.getInstance(), CACHED_STATES_UPDATE_DELAY);
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
    
    private boolean addOrRemoveFromActiveQuests(int questId, QuestState state) {
        if (state == null || state.getStatus() != Status.GIVENTO) {
            int length = this.activeQuests.size();
            for (int i = 0; i < length; i++) {
                if (this.activeQuests.get(i).getQuest().getId() == questId) {
                    this.activeQuests.remove(i);
                    return true;
                }
            }
            return false;
        } else {
            return this.activeQuests.addIfAbsent(state);
        }
    }
    
    public void delayReward(Reward reward) {
        if (this.delayedRewards == null) {
            this.delayedRewards = new ArrayDeque<>();
        }
        
        this.delayedRewards.addLast(reward);
        
        if (this.payRewardsTimerId == -1) {
            this.payRewardsTimerId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                this.payRewardsTimerId = -1;
                payDelayedRewards();
            });
        }
    }
    
    public void payDelayedRewards() {
        Player player = getPlayer();
        if (player == null) {
            try {
                for (Reward reward; (reward = this.delayedRewards.pollFirst()) != null;) {
                    CubeQuest.getInstance().getDatabaseFassade().addRewardToDeliver(reward, getId());
                }
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Exception trying to save delayed rewards to database after player left.", e);
            }
            return;
        }
        
        if (this.payRewardsTimerId != -1) {
            Bukkit.getScheduler().cancelTask(this.payRewardsTimerId);
            this.payRewardsTimerId = -1;
        }
        
        if (this.delayedRewards == null) {
            return;
        }
        
        for (Reward reward = this.delayedRewards.poll(); reward != null; reward = this.delayedRewards.poll()) {
            reward.pay(player);
        }
    }
    
    public void addPendingRegiving() {
        this.pendingRegivings++;
    }
    
    public void removePendingRegiving() {
        if (this.pendingRegivings == 0) {
            throw new IllegalStateException("Has no pending regivings.");
        }
        this.pendingRegivings--;
    }
    
    public boolean hasPendingRegivings() {
        return this.pendingRegivings == 0;
    }
    
    public void updateCachedStates() {
        if (this.updateCachedStatesTask != null) {
            this.updateCachedStatesTask.cancel();
            this.updateCachedStatesTask = null;
        }
        
        for (Iterator<Integer> it = this.cachedStates.iterator(); it.hasNext();) {
            int questId = it.next();
            it.remove();
            PlayerData.this.updateInDatabase(questId, PlayerData.this.getPlayerState(questId));
        }
    }
    
    public void updateDataInDatabase() {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerData(this.id, this.questPoints, this.xp);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save PlayerData.", e);
        }
    }
    
    public void updateInDatabase(int questId, QuestState state) {
        try {
            CubeQuest.getInstance().getDatabaseFassade().setPlayerState(questId, this.id, state);
            this.cachedStates.remove(questId);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not set QuestState for Quest " + questId + " and Player " + this.id.toString() + ":", e);
        }
    }
    
}
