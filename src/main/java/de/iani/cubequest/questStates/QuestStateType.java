package de.iani.cubequest.questStates;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;

public enum QuestStateType {
    
    STANDARD_QUEST_STATE(QuestState.class) {
        
        @Override
        public QuestState createState(PlayerData data, int questId, long lastAction, boolean hidden) {
            return new QuestState(data, questId, lastAction, hidden);
        }
    },
    AMOUNT_QUEST_STATE(AmountQuestState.class) {
        
        @Override
        public QuestState createState(PlayerData data, int questId, long lastAction, boolean hidden) {
            return new AmountQuestState(data, questId, lastAction, hidden);
        }
    },
    WAIT_FOR_TIME_QUEST_STATE(WaitForTimeQuestState.class) {
        
        @Override
        public QuestState createState(PlayerData data, int questId, long lastAction, boolean hidden) {
            return new WaitForTimeQuestState(data, questId, lastAction, hidden);
        }
    };
    
    private static Map<Class<? extends QuestState>, QuestStateType> types =
            new HashMap<>();
    
    public final Class<? extends QuestState> stateClass;
    
    static {
        for (QuestStateType type : values()) {
            types.put(type.stateClass, type);
        }
    }
    
    public static QuestStateType getQuestStateType(Class<? extends QuestState> c) {
        return types.get(c);
    }
    
    private QuestStateType(Class<? extends QuestState> stateClass) {
        this.stateClass = stateClass;
    }
    
    public abstract QuestState createState(PlayerData data, int questId, long lastAction, boolean hidden);
    
}
