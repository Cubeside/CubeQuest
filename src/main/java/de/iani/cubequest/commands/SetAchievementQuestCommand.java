package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetAchievementQuestCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "setAchievement";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.BOOLEAN, "Achievement", parsed -> null)};
        
        propertySetter = parsed -> {
            if ((Boolean) parsed[2]) {
                if (!Util.isLegalAchievementQuest((Quest) parsed[1])) {
                    return "Diese Quest kann keine AchievementQuest sein"
                            + " (sie muss eine ComplexQuest mit genau einer AmountQuest als Unterquest sein," + " beide Quests müssen legal sein).";
                }
            }
            ((ComplexQuest) parsed[1]).setAchievementQuest((Boolean) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "Quest " + ((Quest) parsed[1]).getId() + " ist jetzt " + ((Boolean) parsed[2] ? "eine" : "keine")
                + " AchievementQuest" + ((Boolean) parsed[2] ? "" : " mehr") + ".";
    }
    
    public SetAchievementQuestCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
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
