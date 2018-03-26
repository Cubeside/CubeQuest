package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetQuestDisplayMessageCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.STRING, "OnDeleteCascade", parsed -> null)};
        
        propertySetter = parsed -> {
            ((Quest) parsed[1])
                    .setDisplayMessage(ChatAndTextUtil.convertColors((String) parsed[2]));
            return null;
        };
        
        successMessageProvider = parsed -> "DisplayMessage für Quest " + ((Quest) parsed[1]).getId()
                + " auf \"" + ChatAndTextUtil.convertColors((String) parsed[2]) + "\" gesetzt.";
    }
    
    public SetQuestDisplayMessageCommand() {
        super("quest setQuestDisplayMessage", ACCEPTING_SENDER_CONSTRAINT, argumentDefiners,
                propertySetter, successMessageProvider);
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
        return "<Anzeigenachricht>";
    }
    
}
