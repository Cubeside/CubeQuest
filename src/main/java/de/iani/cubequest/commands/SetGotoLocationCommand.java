package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetGotoLocationCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof GotoQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen Ort.");
            return true;
        }
        
        Location location = ChatAndTextUtil.getLocation(sender, args, true, false);
        
        if (location == null) {
            return true;
        }
        
        ((GotoQuest) quest).setLocation(location);
        ChatAndTextUtil.sendNormalMessage(sender, "Ort gesetzt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
