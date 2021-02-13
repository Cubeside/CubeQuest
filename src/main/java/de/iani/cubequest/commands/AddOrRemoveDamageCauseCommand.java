package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.TakeDamageQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class AddOrRemoveDamageCauseCommand extends AssistedSubCommand {
    
    public static final String ADD_COMMAND_PATH = "addDamageCause";
    public static final String ADD_FULL_COMMAND = "quest " + ADD_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeDamageCause";
    public static final String REMOVE_FULL_COMMAND = "quest " + REMOVE_COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> addPropertySetter;
    private static Function<Object[], String> removePropertySetter;
    private static Function<Object[], String> addSuccessMessageProvider;
    private static Function<Object[], String> removeSuccessMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (parsed[1] instanceof TakeDamageQuest) ? null
                                : "Diese Quest erfordert keinen DamageCause."),
                new EnumParameterDefiner<>(DamageCause.class, "DamageCause",
                        parsed -> parsed[2] == null ? "Unbekannter DamageCause." : null)};
        
        addPropertySetter = parsed -> {
            if (!((TakeDamageQuest) parsed[1]).addCause((DamageCause) parsed[2])) {
                return "Dieser DamageCause war bereits eingetragen.";
            }
            return null;
        };
        removePropertySetter = parsed -> {
            if (!((TakeDamageQuest) parsed[1]).removeCause((DamageCause) parsed[2])) {
                return "Dieser DamageCause war nicht eingetragen.";
            }
            return null;
        };
        
        addSuccessMessageProvider = parsed -> "DamageCause " + parsed[2] + " zu Quest " + parsed[1] + " hinzugefügt.";
        removeSuccessMessageProvider = parsed -> "DamageCause " + parsed[2] + " von Quest " + parsed[1] + " entfernt.";
    }
    
    public AddOrRemoveDamageCauseCommand(boolean add) throws IllegalArgumentException {
        super(add ? ADD_COMMAND_PATH : REMOVE_COMMAND_PATH, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners,
                add ? addPropertySetter : removePropertySetter,
                add ? addSuccessMessageProvider : removeSuccessMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = Arrays.stream(DamageCause.values()).map(DamageCause::name).collect(Collectors.toList());
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<DamageCause>";
    }
    
}
