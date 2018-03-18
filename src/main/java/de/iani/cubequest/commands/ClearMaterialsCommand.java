package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.AmountQuest;
import de.iani.cubequest.quests.MaterialsAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ClearMaterialsCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof AmountQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keine Materialien.");
            return true;
        }
        
        ((MaterialsAndAmountQuest) quest).clearTypes();
        ChatAndTextUtil.sendNormalMessage(sender, "Alle Materialien für " + quest.getTypeName()
                + " [" + quest.getId() + "] " + " entfernt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
