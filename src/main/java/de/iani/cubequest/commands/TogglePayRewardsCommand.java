package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TogglePayRewardsCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.BOOLEAN, "PayRewards", parsed -> null)};
        
        propertySetter = parsed -> {
            if (((Boolean) parsed[1]).booleanValue() == CubeQuest.getInstance().isPayRewards()) {
                return "Dieser Server zahlte bereits" + ((Boolean) parsed[1] ? "" : " keine") + " Belohnungen aus.";
            }
            CubeQuest.getInstance().setPayRewards((Boolean) parsed[1]);
            return null;
        };
        
        successMessageProvider = parsed -> "Der Server zahlt nun" + ((Boolean) parsed[1] ? "" : " keine") + " Belohnungen"
                + ((Boolean) parsed[1] ? "" : " mehr") + " aus.";
    }
    
    public TogglePayRewardsCommand() {
        super("quest setPayRewards", ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
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
