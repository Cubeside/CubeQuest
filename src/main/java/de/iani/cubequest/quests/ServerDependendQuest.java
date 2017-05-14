package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;

public abstract class ServerDependendQuest extends Quest {

    private int serverId;

    public ServerDependendQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId) {
        super(name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = serverId;
    }

    public ServerDependendQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = CubeQuest.getInstance().getServerId();
    }

    public ServerDependendQuest(String name, String giveMessage, String successMessage, Reward successReward, int serverId) {
        this(name, giveMessage, successMessage, null, successReward, null);
    }

    public ServerDependendQuest(String name, String giveMessage, String successMessage,Reward successReward) {
        this(name, giveMessage, successMessage, null, successReward, null);
    }

    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == serverId;
    }

    public int getServerId() {
        return serverId;
    }

    public void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }

}
