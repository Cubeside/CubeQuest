package de.iani.cubequest;

import java.util.EnumMap;
import java.util.HashMap;

import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.WaitForTimeQuestState;

public enum QuestStateType {
    STANDARD_QUEST_STATE, AMOUNT_QUEST_STATE, WAIT_FOR_TIME_QUEST_STATE;

    private static EnumMap<QuestStateType, Class<? extends QuestState>> classes;
    private static HashMap<Class<? extends QuestState>, QuestStateType> types;

    static {
        classes = new EnumMap<QuestStateType, Class<? extends QuestState>>(QuestStateType.class);
        classes.put(STANDARD_QUEST_STATE, QuestState.class);
        classes.put(AMOUNT_QUEST_STATE, AmountQuestState.class);
        classes.put(WAIT_FOR_TIME_QUEST_STATE, WaitForTimeQuestState.class);

        types = new HashMap<Class<? extends QuestState>, QuestStateType>();
        types.put(QuestState.class, STANDARD_QUEST_STATE);
        types.put(AmountQuestState.class, AMOUNT_QUEST_STATE);
        types.put(WaitForTimeQuestState.class, WAIT_FOR_TIME_QUEST_STATE);
    }

    public static QuestStateType getQuestStateType(Class<? extends QuestState> c) {
        return types.get(c);
    }

    public Class<? extends QuestState> getQuestStateClass() {
        return classes.get(this);
    }

}