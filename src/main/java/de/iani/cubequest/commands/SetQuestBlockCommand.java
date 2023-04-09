package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.TriggerBlockReceiveGameEventQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class SetQuestBlockCommand extends SubCommand {

    public static final String COMMAND_PATH = "setQuestBlock";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof TriggerBlockReceiveGameEventQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen Block.");
            return true;
        }

        Location location = ChatAndTextUtil.getLocation(sender, args, true, true);

        if (location == null) {
            return true;
        }

        ((TriggerBlockReceiveGameEventQuest) quest).setBlock(location);
        ChatAndTextUtil.sendNormalMessage(sender, "Ort gesetzt: " + ChatAndTextUtil.getLocationInfo(location));
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
