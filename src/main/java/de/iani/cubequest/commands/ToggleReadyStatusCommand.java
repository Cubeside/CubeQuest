package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ToggleReadyStatusCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "setReady";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.BOOLEAN, "Ready", parsed -> null)};
        
        propertySetter = parsed -> {
            if (((Quest) parsed[1]).isReady() == ((Boolean) parsed[2]).booleanValue()) {
                return "Diese Quest ist bereits auf " + ((Boolean) parsed[2] ? "" : " nicht") + " fertig gesetzt.";
            }
            if (!((Quest) parsed[1]).isLegal()) {
                return "Diese Quest erfüllt noch nicht alle Voraussetzungen.";
            }
            ((Quest) parsed[1]).setReady((Boolean) parsed[2]);
            
            if (!(Boolean) parsed[2]) {
                CubeQuest.getInstance().getQuestEditor().terminateNonPermittedEdits((Quest) parsed[1]);
            }
            return null;
        };
        
        successMessageProvider =
                parsed -> "Quest " + ((Quest) parsed[1]).getId() + " auf" + ((Boolean) parsed[2] ? "" : " nicht") + " fertig gesetzt.";
    }
    
    public ToggleReadyStatusCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.CONFIRM_QUESTS_PERMISSION;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
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
