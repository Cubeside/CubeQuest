package de.iani.cubequest.questStates;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class WaitForTimeQuestState extends QuestState {
    
    private long goal;
    private int taskId = -1;
    
    public WaitForTimeQuestState(PlayerData data, int questId, Status status, long ms) {
        super(data, questId, status);
        this.goal = System.currentTimeMillis() + ms;
    }
    
    public WaitForTimeQuestState(PlayerData data, int questId, long ms) {
        this(data, questId, null, ms);
    }
    
    public WaitForTimeQuestState(PlayerData data, int questId, Status status) {
        this(data, questId, status, 0);
    }
    
    public WaitForTimeQuestState(PlayerData data, int questId) {
        this(data, questId, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc, Status status)
            throws InvalidConfigurationException {
        super.deserialize(yc, status);
        
        this.goal = yc.getInt("goal");
    }
    
    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("goal", this.goal);
        
        return super.serialize(yc);
    }
    
    public long getGoal() {
        return this.goal;
    }
    
    public boolean checkTime() {
        if (this.taskId >= 0) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
        if (this.goal <= System.currentTimeMillis()) {
            return getQuest().onSuccess(getPlayerData().getPlayer());
        }
        this.taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(),
                () -> checkTime(),
                Math.max(1, (this.goal - System.currentTimeMillis()) * 20 / 1000));
        return false;
    }
    
    public void playerLeft() {
        if (this.taskId >= 0) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
    }
    
}
