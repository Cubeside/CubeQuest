package de.iani.cubequest.quests;

import java.util.UUID;

import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.WaitForTimeQuestState;

public class WaitForTimeQuest extends WaitingQuest {

    private long ms;

    public WaitForTimeQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            long msToWait) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);
        this.ms = msToWait;
    }

    public WaitForTimeQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            long msToWait) {
        this(id, name, giveMessage, successMessage, null, successReward, null, msToWait);
    }

    public WaitForTimeQuest(int id) {
        this(id, null, null, null, null, 0);
    }

    @Override
    public boolean isLegal() {
        return ms > 0;
    }

    @Override
    public WaitForTimeQuestState createQuestState(UUID player) {
        return new WaitForTimeQuestState(CubeQuest.getInstance().getPlayerData(player), this.getId(), ms);
    }

    @Override public void checkPlayer(Player player) {
        if (!CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.getId())) {
            return;
        }
        if (((WaitForTimeQuestState) CubeQuest.getInstance().getPlayerData(player).getPlayerState(this.getId())).isDue()) {
            onSuccess(player);
        }
    }

    public long getTime() {
        return ms;
    }

    /**
     * Setzt die zu wartende Zeit.
     * Betrifft keine Spieler, an die die Quest bereits vergeben wurde!
     * Deprecated, wenn isReady() true ist!
     * @param ms
     */
    public void setTime(long ms) {
        this.ms = ms;
    }

}
