package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questGiving.HaveQuestStatusCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddHaveQuestStatusGivingConditionCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new EnumParameterDefiner<>(Status.class, "Status", parsed -> null),
                new ParameterDefiner(ParameterType.QUEST, "OtherQuest", parsed -> null)};
        
        propertySetter = parsed -> {
            ((Quest) parsed[1]).addQuestGivingCondition(
                    new HaveQuestStatusCondition((Quest) parsed[3], (Status) parsed[2]));
            return null;
        };
        
        successMessageProvider = parsed -> "QuestStatusGivingCondition (" + parsed[3] + ": "
                + parsed[2] + ") für Quest " + ((Quest) parsed[1]).getId() + " eingefügt.";
    }
    
    public AddHaveQuestStatusGivingConditionCommand() {
        super("quest addQuestStatusGivingCondition", ACCEPTING_SENDER_CONSTRAINT, parameterDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Status> <OtherQuest (Name oder Id)>";
    }
    
}
