package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ToggleGenerateDailyQuestsCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.BOOLEAN, "GenerateDailyQuests", parsed -> null)};
        
        propertySetter = parsed -> {
            if (((Boolean) parsed[1]).booleanValue() == CubeQuest.getInstance().isGeneratingDailyQuests()) {
                return "Dieser Server generierte bereits" + ((Boolean) parsed[1] ? "" : " keine") + " DailyQuests.";
            }
            CubeQuest.getInstance().setGenerateDailyQuests((Boolean) parsed[1]);
            return null;
        };
        
        successMessageProvider = parsed -> "Der Server generiert nun" + ((Boolean) parsed[1] ? "" : " keine") + " DailyQuests"
                + ((Boolean) parsed[1] ? "" : " mehr") + ".";
    }
    
    public ToggleGenerateDailyQuestsCommand() {
        super("quest setGenerateDailyQuests", ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_SERVER_PROPERTIES_PERMISSION;
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
