package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import java.util.function.Function;

public class RemoveGivingConditionCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners =
                new ParameterDefiner[] {
                        new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                                parsed -> null),
                        new ParameterDefiner(ParameterType.POSITIVE_INTEGER, "ConditionIndex",
                                parsed -> ((Integer) parsed[2]) <= ((Quest) parsed[1])
                                        .getQuestGivingConditions().size() ? null
                                                : "Index ist zu groß.")};
        
        propertySetter = parsed -> {
            ((Quest) parsed[1]).removeQuestGivingCondition((Integer) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "GivingCondition " + parsed[2] + " von Quest "
                + ((Quest) parsed[1]).getId() + " entfernt.";
    }
    
    public RemoveGivingConditionCommand() {
        super("quest removeGivingCondition", ACCEPTING_SENDER_CONSTRAINT, parameterDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
