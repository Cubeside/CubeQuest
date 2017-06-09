package de.iani.cubequest.questStates;

import de.iani.cubequest.PlayerData;

public class QuestState {

    private Status status;
    private PlayerData data;
    private int questId;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL;

        private static Status[] values = values();

        public static Status fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }

    public QuestState(PlayerData data, int questId) {
        this.data = data;
        this.questId = questId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
        data.stateChanged(questId);
    }

}
