package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.AmountQuest;
import de.iani.cubequest.quests.Quest;

public class SetQuestAmountCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof AmountQuest)) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest hat keine Anzahl, die gesetzt werden könnte.");
            return true;
        }

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib die Anzahl an.");
            return true;
        }

        int amount = args.getNext(-1);
        if (amount < 0) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib die Anzahl als nicht-negative Ganzzahl an.");
        }

        ((AmountQuest) quest).setAmount(amount);
        CubeQuest.sendNormalMessage(sender, "Anzahl für " + QuestType.getQuestType(quest.getClass()) + " [" + quest.getId() + "] ist jetzt " + amount + ".");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
