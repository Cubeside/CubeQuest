package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.SelectionType;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetSelectionTypeCommand extends SubCommand {

    public static final String COMMAND_PATH = "setSelectionType";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof ComplexQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest hat keinen Auswahltyp.");
            return true;
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gibt einen Auswahltyp an.");
            return true;
        }

        String typeString = args.getNext();
        SelectionType selectionType = SelectionType.match(typeString);
        if (selectionType == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Auswahltyp " + typeString + " nicht gefunden.");
            return true;
        }

        ((ComplexQuest) quest).setSelectionType(selectionType);
        ChatAndTextUtil.sendNormalMessage(sender, "Auswahltyp auf " + selectionType + " gesetzt.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String arg = args.getNext("").toLowerCase(Locale.ENGLISH);
        List<String> result = new ArrayList<>();
        for (SelectionType s : SelectionType.values()) {
            if (s.toString().toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                result.add(s.toString());
            }
        }
        return result;
    }

    @Override
    public String getUsage() {
        String usage = "<";
        for (SelectionType option : SelectionType.values()) {
            usage += option.name() + " | ";
        }
        usage = usage.substring(0, usage.length() - " | ".length()) + ">";
        return usage;
    }

}
