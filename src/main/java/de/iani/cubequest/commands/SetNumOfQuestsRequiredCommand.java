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

public class SetNumOfQuestsRequiredCommand extends SubCommand {

    public static final String COMMAND_PATH = "setNumOfQuestsRequired";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof ComplexQuest cQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest hat keine Anzahl zu erfüllender Quests.");
            return true;
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an.");
            return true;
        }

        int amount = args.getNext(-1);
        if (amount < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl als nicht-negative Ganzzahl an.");
        }

        cQuest.setNumOfQuestsRequired(amount);
        ChatAndTextUtil.sendNormalMessage(sender,
                "Anzahl für " + quest.getTypeName() + " [" + quest.getId() + "] ist jetzt " + amount + ".");
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
        return "<Anzahl>";
    }

}
