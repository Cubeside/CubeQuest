package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetOrAppendDisplayMessageCommand extends SubCommand {

    public static final String SET_COMMAND_PATH = "setDisplayMessage";
    public static final String FULL_SET_COMMAND = "quest " + SET_COMMAND_PATH;

    public static final String APPEND_COMMAND_PATH = "appendDisplayMessage";
    public static final String APPEND_PATH_ALIAS = "addDisplayMessage";
    public static final String FULL_APPEND_COMMAND = "quest " + APPEND_COMMAND_PATH;

    private boolean set;

    public SetOrAppendDisplayMessageCommand(boolean set) {
        this.set = set;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        Component msg;
        try {
            msg = args.hasNext() ? ComponentUtilAdventure.convertEscaped(args.getAll(null)) : null;
        } catch (ParseException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Ungültige Nachricht: ", e.getMessage());
            return true;
        }

        if (this.set) {
            quest.setDisplayMessage(msg);
        } else {
            if (msg == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Text an, den du zur Nachricht hinzufügen möchtest.");
                return true;
            }
            quest.addDisplayMessage(msg);
            msg = quest.getDisplayMessage();
        }

        if (msg == null) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "DisplayMessage für " + quest.getTypeName() + " [" + quest.getId() + "] gelöscht.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "DisplayMessage für " + quest.getTypeName() + " [" + quest.getId() + "] lautet jetzt:");
            sender.sendMessage(msg);
        }

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
        return "<Nachricht>";
    }

}
