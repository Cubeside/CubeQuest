package de.iani.cubequest;

import java.util.EnumMap;
import java.util.HashMap;

import de.iani.cubequest.quests.BlockBreakQuest;
import de.iani.cubequest.quests.BlockPlaceQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.quests.FishingQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.KillEntitiesQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.TalkQuest;
import de.iani.cubequest.quests.WaitForTimeQuest;

public enum QuestType {
    BLOCK_BREAK_QUEST, BLOCK_PLACE_QUEST, COMMAND_QUEST, COMPLEX_QUEST, DELIVERY_QUEST, FISHING_QUEST, GOTO_QUEST,
    KILL_ENTITIES_QUEST, TALK_QUEST, WAIT_FOR_DATE_QUEST, WAIT_FOR_TIME_QUEST;

    private static EnumMap<QuestType, Class<? extends Quest>> classes;
    private static HashMap<Class<? extends Quest>, QuestType> types;

    static {
        classes = new EnumMap<QuestType, Class<? extends Quest>>(QuestType.class);
        classes.put(BLOCK_BREAK_QUEST, BlockBreakQuest.class);
        classes.put(BLOCK_PLACE_QUEST, BlockPlaceQuest.class);
        classes.put(COMMAND_QUEST, CommandQuest.class);
        classes.put(COMPLEX_QUEST, ComplexQuest.class);
        classes.put(DELIVERY_QUEST, DeliveryQuest.class);
        classes.put(FISHING_QUEST, FishingQuest.class);
        classes.put(GOTO_QUEST, GotoQuest.class);
        classes.put(KILL_ENTITIES_QUEST, KillEntitiesQuest.class);
        classes.put(TALK_QUEST, TalkQuest.class);
        classes.put(WAIT_FOR_DATE_QUEST, WaitForTimeQuest.class);
        classes.put(WAIT_FOR_TIME_QUEST, WaitForTimeQuest.class);

        types = new HashMap<Class<? extends Quest>, QuestType>();
        types.put(BlockBreakQuest.class, BLOCK_BREAK_QUEST);
        types.put(BlockPlaceQuest.class, BLOCK_PLACE_QUEST);
        types.put(CommandQuest.class, COMMAND_QUEST);
        types.put(ComplexQuest.class, COMPLEX_QUEST);
        types.put(DeliveryQuest.class, DELIVERY_QUEST);
        types.put(FishingQuest.class, FISHING_QUEST);
        types.put(GotoQuest.class, GOTO_QUEST);
        types.put(KillEntitiesQuest.class, KILL_ENTITIES_QUEST);
        types.put(TalkQuest.class, TALK_QUEST);
        types.put(WaitForTimeQuest.class, WAIT_FOR_DATE_QUEST);
        types.put(WaitForTimeQuest.class, WAIT_FOR_TIME_QUEST);
    }

    public static QuestType getQuestType(Class<? extends Quest> c) {
        return types.get(c);
    }

    public Class<? extends Quest> getQuestClass() {
        return classes.get(this);
    }

}