package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class EditQuestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest edit ", "", "Quest ",
                " editieren");
        if (quest == null) {
            return true;
        }
        
        if (quest.isReady()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest ist bereits auf \"fertig\" gesetzt. Sie zu bearbeiten kann unbekannte Nebenwirkungen haben, es wird davon abgeraten.");
        }
        
        CubeQuest.getInstance().getQuestEditor().startEdit(sender, quest);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
