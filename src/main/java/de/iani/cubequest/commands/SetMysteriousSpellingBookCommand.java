package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class SetMysteriousSpellingBookCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "setMysteriousSpellingBook";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte nimm ein Item in die Hand.");
            return true;
        }
        
        QuestGenerator.getInstance().setMysteriousSpellingBook(item);
        ChatAndTextUtil.sendNormalMessage(sender, "Mysteriöses Zauberbuch gesetzt.");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_SERVER_PROPERTIES_PERMISSION;
    }
    
}
