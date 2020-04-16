package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddOrRemoveServerFlagCommand extends AssistedSubCommand {
    
    public static final String ADD_SERVER_FLAG_COMMAND = "addServerFlag";
    public static final String REMOVE_SERVER_FLAG_COMMAND = "removeServerFlag";
    
    private static ParameterDefiner[] getParameterDefiners() {
        return new ParameterDefiner[] {new ParameterDefiner(ParameterType.WORD, "Flag", parsed -> null)};
    }
    
    private static Function<Object[], String> getPropertySetter(boolean add) {
        return parsed -> {
            String flag = (String) parsed[1];
            boolean result;
            if (add) {
                result = CubeQuest.getInstance().addServerFlag(flag);
            } else {
                result = CubeQuest.getInstance().removeServerFlag(flag);
            }
            return result ? null : ("Dieser Server hatte diese Flag " + (add ? "bereits" : "nicht") + ".");
        };
    }
    
    private static Function<Object[], String> getSuccessMessageProvider(boolean add) {
        if (add) {
            return parsed -> "Dieser Server hat nun die Flag \"" + parsed[1] + "\".";
        } else {
            return parsed -> "Dieser Server hat nun nicht mehr die Flag \"" + parsed[1] + "\".";
        }
    }
    
    private boolean add;
    
    public AddOrRemoveServerFlagCommand(boolean add) {
        super("quest " + (add ? ADD_SERVER_FLAG_COMMAND : REMOVE_SERVER_FLAG_COMMAND), ACCEPTING_SENDER_CONSTRAINT, getParameterDefiners(),
                getPropertySetter(add), getSuccessMessageProvider(add));
        this.add = add;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_SERVER_PROPERTIES_PERMISSION;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (this.add) {
            return Collections.emptyList();
        }
        
        return ChatAndTextUtil.polishTabCompleteList(CubeQuest.getInstance().getServerFlags(), args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<flag>";
    }
    
}
