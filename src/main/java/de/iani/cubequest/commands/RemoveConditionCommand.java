package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ProgressableQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RemoveConditionCommand extends AssistedSubCommand {
    
    public static final String GIVING_COMMAND_PATH = "removeGivingCondition";
    public static final String FULL_GIVING_COMMAND = "quest " + GIVING_COMMAND_PATH;
    
    public static final String PROGRESS_COMMAND_PATH = "removeProgressCondition";
    public static final String FULL_PROGRESS_COMMAND = "quest " + PROGRESS_COMMAND_PATH;
    
    private static ParameterDefiner[] getParameterDefiners(boolean giving) {
        return new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> {
            return !giving && !(parsed[1] instanceof ProgressableQuest) ? "Für diese Quest können keine Fortschrittsbedingungen festgelegt werden."
                    : null;
        }), new ParameterDefiner(ParameterType.POSITIVE_INTEGER, "ConditionIndex",
                parsed -> ((Integer) parsed[2]) <= (giving ? ((Quest) parsed[1]).getQuestGivingConditions().size()
                        : ((ProgressableQuest) parsed[1]).getQuestProgressConditions().size()) ? null : "Index ist zu groß.")};
    }
    
    private static Function<Object[], String> getPropertySetter(boolean giving) {
        return parsed -> {
            if (giving) {
                ((Quest) parsed[1]).removeQuestGivingCondition((Integer) parsed[2] - 1);
            } else {
                ((ProgressableQuest) parsed[1]).removeQuestProgressCondition((Integer) parsed[2] - 1);
            }
            return null;
        };
    }
    
    private static Function<Object[], String> getSuccessMessageProvider(boolean giving) {
        return parsed -> (giving ? "Giving" : "Progress") + "Condition " + parsed[2] + " von Quest " + ((Quest) parsed[1]).getId() + " entfernt.";
    }
    
    public RemoveConditionCommand(boolean giving) {
        super(giving ? FULL_GIVING_COMMAND : FULL_PROGRESS_COMMAND, ACCEPTING_SENDER_CONSTRAINT, getParameterDefiners(giving),
                getPropertySetter(giving), getSuccessMessageProvider(giving));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<ConditionIndex>";
    }
    
}
