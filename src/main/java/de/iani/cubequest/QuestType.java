package de.iani.cubequest;

import java.util.HashMap;
import java.util.Map;

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
import de.iani.cubequest.quests.TameEntitiesQuest;
import de.iani.cubequest.quests.WaitForTimeQuest;

public enum QuestType {

    BLOCK_BREAK_QUEST(BlockBreakQuest.class),
    BLOCK_PLACE_QUEST(BlockPlaceQuest.class),
    COMMAND_QUEST(CommandQuest.class),
    COMPLEX_QUEST(ComplexQuest.class),
    DELIVERY_QUEST(DeliveryQuest.class),
    FISHING_QUEST(FishingQuest.class),
    GOTO_QUEST(GotoQuest.class),
    KILL_ENTITIES_QUEST(KillEntitiesQuest.class),
    TAME_ENTITIES_QUEST(TameEntitiesQuest.class),
    TALK_QUEST(TalkQuest.class),
    WAIT_FOR_DATE_QUEST(TalkQuest.class),
    WAIT_FOR_TIME_QUEST(WaitForTimeQuest.class);

    private static Map<Class<? extends Quest>, QuestType> types = new HashMap<Class<? extends Quest>, QuestType>();

    public final Class<? extends Quest> questClass;

    static {
        for (QuestType type: values()) {
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