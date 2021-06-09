package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetAutoRemoveCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "setAutoRemove";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.WORD, "AutoRemove", parsed -> {
                    String s = (String) parsed[2];
                    if (s.equals("-1")) {
                        return null;
                    }
                    try {
                        StringUtil.parseTimespan(s);
                        return null;
                    } catch (IllegalArgumentException e) {
                        return "AutoRemove muss -1 oder eine Zeitspanne (z.B. 4d3h15s) sein.";
                    }
                })};
        
        propertySetter = parsed -> {
            String s = (String) parsed[2];
            ((Quest) parsed[1]).setAutoRemoveMs(s.equals("-1") ? -1 : StringUtil.parseTimespan(s));
            return null;
        };
        
        successMessageProvider = parsed -> "Quest " + ((Quest) parsed[1]).getId() + " wird "
                + (((String) parsed[2]).equals("-1") ? "nicht mehr" : ("nach " + parsed[2])) + " automatisch entfernt.";
    }
    
    public SetAutoRemoveCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
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
        return "<-1 | autoRemoveDuration>";
    }
    
}
