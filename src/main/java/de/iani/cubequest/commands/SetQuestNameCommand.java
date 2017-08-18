package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.Quest;

public class SetQuestNameCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib den neuen Namen der Quest an.");
            return true;
        }
        String name = args.getNext();

        quest.setName(name);
        CubeQuest.sendNormalMessage(sender, QuestType.getQuestType(quest.getClass()) + " [" + quest.getId() + "] heißt jetzt " + name + ".");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
