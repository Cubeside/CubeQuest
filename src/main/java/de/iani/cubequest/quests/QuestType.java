package de.iani.cubequest.quests;

import java.util.HashMap;
import java.util.Map;

public enum QuestType {
    
    BLOCK_BREAK_QUEST(BlockBreakQuest.class),
    BLOCK_PLACE_QUEST(BlockPlaceQuest.class),
    COMMAND_QUEST(CommandQuest.class),
    COMPLEX_QUEST(ComplexQuest.class),
    DELIVERY_QUEST(DeliveryQuest.class),
    FISHING_QUEST(FishingQuest.class),
    GOTO_QUEST(GotoQuest.class),
    KILL_ENTITIES_QUEST(KillEntitiesQuest.class),
    TAKE_DAMAGE_QUEST(TakeDamageQuest.class),
    TAME_ENTITIES_QUEST(TameEntitiesQuest.class),
    INTERACT_INTERACTOR_QUEST(ClickInteractorQuest.class),
    WAIT_FOR_DATE_QUEST(WaitForDateQuest.class),
    WAIT_FOR_TIME_QUEST(WaitForTimeQuest.class);
    
    private static Map<Class<? extends Quest>, QuestType> types = new HashMap<>();
    
    public final Class<? extends Quest> questClass;
    
    static {
        for (QuestType type : values()) {
            types.put(type.questClass, type);
        }
    }
    
    public static QuestType getQuestType(Class<? extends Quest> c) {
        return types.get(c);
    }
    
    private QuestType(Class<? extends Quest> questClass) {
        this.questClass = questClass;
    }
    
}
