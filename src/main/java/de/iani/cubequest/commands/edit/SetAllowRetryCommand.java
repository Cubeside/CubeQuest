package de.iani.cubequest.commands.edit;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.ArgsParser;
import de.iani.cubequest.commands.AssistedSubCommand;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.Quest.RetryOption;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetAllowRetryCommand extends AssistedSubCommand {
    
    public static String SUCCESS_COMMAND_PATH = "setAllowRetryOnSuccess";
    public static String FULL_SUCCESS_COMMAND = "quest " + SUCCESS_COMMAND_PATH;
    public static String FAIL_COMMAND_PATH = "setAllowRetryOnFail";
    public static String FULL_FAIL_COMMAND = "quest " + FAIL_COMMAND_PATH;
    
    private static ParameterDefiner[] argumentDefiners(boolean success) {
        return new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new EnumParameterDefiner<>(RetryOption.class,
                        "AllowRetryOn" + (success ? "Success" : "Fail"), parsed -> null)};
    }
    
    private static Function<Object[], String> propertySetter(boolean success) {
        return parsed -> {
            if (success) {
                ((Quest) parsed[1]).setAllowRetryOnSuccess((RetryOption) parsed[2]);
            } else {
                ((Quest) parsed[1]).setAllowRetryOnFail((RetryOption) parsed[2]);
            }
            return null;
        };
    }
    
    private static Function<Object[], String> successMessageProvider(boolean success) {
        return parsed -> "AllowRetryOn " + (success ? "Success" : "Fail") + " für Quest "
                + ((Quest) parsed[1]).getId() + " auf " + parsed[2] + " gesetzt.";
    }
    
    public SetAllowRetryCommand(boolean success) {
        super(success ? FULL_SUCCESS_COMMAND : FULL_FAIL_COMMAND, ACCEPTING_SENDER_CONSTRAINT,
                argumentDefiners(success), propertySetter(success),
                successMessageProvider(success));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (RetryOption option : RetryOption.values()) {
            result.add(option.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        String usage = "<";
        for (RetryOption option : RetryOption.values()) {
            usage += option.name() + " | ";
        }
        usage = usage.substring(0, usage.length() - " | ".length()) + ">";
        return usage;
    }
    
}
