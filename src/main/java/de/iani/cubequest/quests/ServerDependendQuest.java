package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;

public abstract class ServerDependendQuest extends Quest {

    private int serverId;

    public ServerDependendQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId) {
        super(name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = serverId;
    }

    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == serverId;
    }

    public int getServerId() {
        return serverId;
    }

}
