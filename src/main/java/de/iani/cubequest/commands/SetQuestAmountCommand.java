package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.AmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetQuestAmountCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof AmountQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest hat keine Anzahl, die gesetzt werden könnte.");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an.");
            return true;
        }
        
        int amount = args.getNext(-1);
        if (amount < 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Anzahl als nicht-negative Ganzzahl an.");
        }
        
        ((AmountQuest) quest).setAmount(amount);
        ChatAndTextUtil.sendNormalMessage(sender, "Anzahl für " + quest.getTypeName() + " ["
                + quest.getId() + "] ist jetzt " + amount + ".");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
