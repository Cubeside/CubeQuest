package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;

public abstract class WaitingQuest extends Quest {

    public WaitingQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);
    }

    public WaitingQuest(int id, String name, String giveMessage, String successMessage, Reward successReward) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    public WaitingQuest(int id) {
        this(id, null, null, null, null);
    }

}
