package de.iani.cubequest.questStates;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;

public class WaitForTimeQuestState extends QuestState {

    private long goal;
    private int taskId = -1;

    public WaitForTimeQuestState(PlayerData data, int questId, long ms) {
        super(data, questId);
        this.goal = System.currentTimeMillis() + ms;
    }

    public WaitForTimeQuestState(PlayerData data, int questId) {
        this(data, questId, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        goal = yc.getInt("goal");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("goal", goal);

        return super.serialize(yc);
    }

    public boolean checkTime() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        if (goal <= System.currentTimeMillis()) {
            return getQuest().onSuccess(getPlayerData().getPlayer());
        }
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(), Math.max(1, (goal-System.currentTimeMillis())*20/1000));
        return false;
    }

    public void playerLeft() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

}
