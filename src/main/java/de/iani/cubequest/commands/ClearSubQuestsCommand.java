package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatUtil;

public class ClearSubQuestsCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof ComplexQuest)) {
            ChatUtil.sendWarningMessage(sender, "Diese Quest unterstützt keine Unterquests.");
            return true;
        }

        ((ComplexQuest) quest).clearPartQuests();
        ChatUtil.sendNormalMessage(sender, "SubQuests entfernt.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
