package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;


public class CloneQuestCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "clone";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        Quest toClone = ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND + " ", "", "Quest ", " klonen.");
        if (toClone == null) {
            return true;
        }
        
        Quest newQuest = CubeQuest.getInstance().getQuestCreator().createQuest(toClone.getClass());
        newQuest.setDelayDatabaseUpdate(true);
        try {
            newQuest.deserialize(toClone.serializeToString());
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        newQuest.setReady(false);
        newQuest.setDelayDatabaseUpdate(false);
        
        ChatAndTextUtil.sendNormalMessage(sender,
                "Quest " + toClone.getId() + " geklont, neue ID: " + newQuest.getId());
        CubeQuest.getInstance().getQuestEditor().startEdit(sender, newQuest);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public String getUsage() {
        return "<Quest (Id oder Name)>";
    }
    
}
