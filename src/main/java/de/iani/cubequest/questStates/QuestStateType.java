package de.iani.cubequest.questStates;

import java.util.HashMap;
import java.util.Map;

public enum QuestStateType {
    STANDARD_QUEST_STATE(QuestState.class),
    AMOUNT_QUEST_STATE(AmountQuestState.class),
    WAIT_FOR_TIME_QUEST_STATE(WaitForTimeQuestState.class);

    private static Map<Class<? extends QuestState>, QuestStateType> types = new HashMap<Class<? extends QuestState>, QuestStateType>();

    public final Class<? extends QuestState> stateClass;

    static {
        for (QuestStateType type: values()) {
            types.put(type.stateClass, type);
        }
    }

    public static QuestStateType getQuestStateType(Class<? extends QuestState> c) {
        return types.get(c);
    }

    private QuestStateType(Class<? extends QuestState> stateClass) {
        this.stateClass = stateClass;
    }

}