package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EditQuestCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "edit";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest edit ", "", "Quest ", " editieren");
        if (quest == null) {
            return true;
        }
        
        if (quest.isReady()) {
            if (!sender.hasPermission(CubeQuest.CONFIRM_QUESTS_PERMISSION)) {
                ChatAndTextUtil.sendErrorMessage(sender,
                        "Diese Quest ist bereits auf \"fertig\" gesetzt. Du hast nicht die Berechtigung, sie zu bearbeiten.");
                return true;
            }
            
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Quest (Id oder Name)>";
    }
    
}
