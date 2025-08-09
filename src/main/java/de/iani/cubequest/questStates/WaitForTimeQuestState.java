package de.iani.cubequest.questStates;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class WaitForTimeQuestState extends QuestState {

    private long goal;
    private int taskId = -1;

    public WaitForTimeQuestState(PlayerData data, int questId, Status status, long lastAction, boolean hidden,
            long ms) {
        super(data, questId, status, lastAction, hidden);
        this.goal = System.currentTimeMillis() + ms;
        checkTime();
    }

    public WaitForTimeQuestState(PlayerData data, int questId, long ms) {
        this(data, questId, Status.NOTGIVENTO, System.currentTimeMillis(), false, ms);
    }

    @Override
    public void deserialize(YamlConfiguration yc, Status status) throws InvalidConfigurationException {
        super.deserialize(yc, status);

        this.goal = yc.getLong("goal");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("goal", this.goal);

        return super.serialize(yc);
    }

    @Override
    public void setStatus(Status status, boolean updatePlayerData) {
        super.setStatus(status, updatePlayerData);
        if (status == Status.GIVENTO) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                if (CubeQuest.getInstance().getPlayerData(getPlayerData().getId())
                        .getPlayerState(getQuest().getId()) == this) {
                    checkTime();
                }
            });
        } else {
            cancelTask();
        }
    }

    @Override
    public void invalidate() {
        cancelTask();
    }

    public long getGoal() {
        return this.goal;
    }

    public boolean checkTime() {
        cancelTask();
        if (getStatus() != Status.GIVENTO) {
            return false;
        }
        if (this.goal <= System.currentTimeMillis()) {
            return getQuest().onSuccess(getPlayerData().getPlayer());
        }
        this.taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> checkTime(),
                Math.max(1, (this.goal - System.currentTimeMillis()) * 19 / 1000));
        return false;
    }

    public void playerLeft() {
        cancelTask();
    }

    private void cancelTask() {
        if (this.taskId >= 0) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
        }
    }

}
