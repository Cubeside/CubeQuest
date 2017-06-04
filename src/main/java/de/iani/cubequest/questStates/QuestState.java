package de.iani.cubequest.questStates;

public class QuestState {

    private Status status;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL;

        private static Status[] values = values();

        public static Status fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }

    public QuestState() {

    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
    }

}
