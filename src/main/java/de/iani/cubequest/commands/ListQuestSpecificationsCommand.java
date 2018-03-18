package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ListQuestSpecificationsCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        ChatAndTextUtil.sendNormalMessage(sender, "Quest-Spezifikationen:");
        ChatAndTextUtil.sendNormalMessage(sender, "");
        ChatAndTextUtil.sendBaseComponent(sender,
                QuestGenerator.getInstance().getSpecificationInfo());
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }
    
}
