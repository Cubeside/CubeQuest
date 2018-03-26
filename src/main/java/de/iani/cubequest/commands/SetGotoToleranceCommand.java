package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.GotoQuest;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetGotoToleranceCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (!(parsed[1] instanceof GotoQuest)
                                ? "Nur GotoQuests haben diese Eigenschaft!"
                                : null)),
                new ParameterDefiner(ParameterType.AT_LEAST_ZERO_DOUBLE, "Tolerance",
                        parsed -> null)};
        
        propertySetter = parsed -> {
            ((GotoQuest) parsed[1]).setTolarance((Double) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "Tolerance für Quest " + ((GotoQuest) parsed[1]).getId()
                + " auf " + parsed[2] + " gesetzt.";
    }
    
    public SetGotoToleranceCommand() {
        super("quest setGotoTolerance", AssistedSubCommand.ACCEPTING_SENDER_CONSTRAINT,
                parameterDefiners, propertySetter, successMessageProvider);
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
        return "<Toleranz>";
    }
    
}
