package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ClearSubQuestsCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "clearSubQuests";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof ComplexQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest unterstützt keine Unterquests.");
            return true;
        }
        
        if (((ComplexQuest) quest).isAchievementQuest() && ((ComplexQuest) quest).getSubQuests().size() == 1) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Unterquest einer Achievement-Quest kann nicht entfernt werden.");
            return true;
        }
        
        if (quest.isReady()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest ist bereits auf fertig gesetzt. Es können daher nicht all ihre Unterquests entfernt werden.");
            return true;
        }
        
        ((ComplexQuest) quest).clearSubQuests();
        ChatAndTextUtil.sendNormalMessage(sender, "SubQuests entfernt.");
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
    
}
