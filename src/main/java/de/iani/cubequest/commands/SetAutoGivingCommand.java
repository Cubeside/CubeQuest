package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetAutoGivingCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "setAutoGiving";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.BOOLEAN, "AutoGiving", parsed -> null)};
        
        propertySetter = parsed -> {
            if ((Boolean) parsed[2]) {
                if (!CubeQuest.getInstance().addAutoGivenQuest((Quest) parsed[1])) {
                    return "Diese Quest wird bereits automatisch vergeben.";
                }
            } else {
                if (!CubeQuest.getInstance().removeAutoGivenQuest((Quest) parsed[1])) {
                    return "Diese Quest wird bereits nicht automatisch vergeben.";
                }
            }
            return null;
        };
        
        successMessageProvider = parsed -> "Quest " + ((Quest) parsed[1]).getId() + " wird jetzt "
                + ((Boolean) parsed[2] ? "" : "nicht mehr ") + "automatisch vergeben.";
    }
    
    public SetAutoGivingCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter,
                successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (String s : AssistedSubCommand.TRUE_STRINGS) {
            result.add(s);
        }
        for (String s : AssistedSubCommand.FALSE_STRINGS) {
            result.add(s);
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<true | false>";
    }
    
}
