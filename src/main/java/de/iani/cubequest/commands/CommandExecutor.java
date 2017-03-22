package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {

    private CubeQuest plugin;
    private QuestEditor editor;

    public CommandExecutor(CubeQuest plugin) {
        this.plugin = plugin;
        this.editor = new QuestEditor(plugin);
    }

    public QuestEditor getQuestEditor() {
        return editor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equals("quest")) {
            if (args.length < 1) {
                return argHelp(sender, args);
            }
            if (args[0].equalsIgnoreCase("edit")) {
                return argEdit(sender, args);
            }
        }
        return false;
    }

    private boolean argHelp(CommandSender sender, String[] args) {
        //TODO
        sender.sendMessage("Help!");
        return true;
    }

    private boolean argEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cubequest.admin")) {
            CubeQuest.sendNoPermissionMessage(sender);
            return true;
        }
        editor.startEdit(sender, args);
        return true;
    }

}
