package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.commands.SubCommand;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class SaveOrReloadGeneratorCommand extends SubCommand {
    
    public static final String SAVE_COMMAND_PATH = "saveGeneratorConfig";
    public static final String FULL_SAVE_COMMAND = "quest " + SAVE_COMMAND_PATH;
    
    public static final String RELOAD_COMMAND_PATH = "reloadGeneratorConfig";
    public static final String FULL_RELOAD_COMMAND = "quest " + SAVE_COMMAND_PATH;
    
    private boolean save;
    
    public SaveOrReloadGeneratorCommand(boolean save) {
        this.save = save;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (this.save) {
            QuestGenerator.getInstance().saveConfig();
            ChatAndTextUtil.sendNormalMessage(sender, "QuestGenerator-Config gespeichert.");
        } else {
            QuestGenerator.reloadConfig();
            ChatAndTextUtil.sendNormalMessage(sender, "QuestGenerator-Config neugeladen");
        }
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
}
