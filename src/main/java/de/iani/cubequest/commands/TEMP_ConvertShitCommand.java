package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TEMP_ConvertShitCommand extends SubCommand {
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!sender.isOp()) {
            ChatAndTextUtil.sendErrorMessage(sender, "DARFST DU NICHT!");
            return true;
        }
        
        for (Quest q : QuestManager.getInstance().getQuests()) {
            q.updateIfReal();
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, "FEDDICH.");
        return true;
    }
    
}
