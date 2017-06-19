package de.iani.cubequest.questStates;

import de.iani.cubequest.PlayerData;

public class WaitForTimeQuestState extends QuestState {

    private long goal;

    public WaitForTimeQuestState(PlayerData data, int questId, long ms) {
        super(data, questId);
        this.goal = System.currentTimeMillis() + ms;
    }

    public boolean isDue() {
        return goal <= System.currentTimeMillis();
    }

}
