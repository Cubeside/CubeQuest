package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatUtil;

public class StopEditingQuestCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!CubeQuest.getInstance().getQuestEditor().stopEdit(sender)) {
            ChatUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest.");
        }
        return true;
    }

}
