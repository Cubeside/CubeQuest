package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class VersionCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        // sender.sendMessage("");
        ChatAndTextUtil.sendNormalMessage(sender, ChatColor.GREEN + "--- " + ChatColor.BLUE
                + "[CubeQuest]" + ChatColor.GREEN + " ---");
        ChatAndTextUtil.sendNormalMessage(sender,
                "Version " + CubeQuest.getInstance().getDescription().getVersion());
        ChatAndTextUtil.sendNormalMessage(sender, "Entwickelt von Jonas \"Starjon\" Becker");
        ChatAndTextUtil.sendNormalMessage(sender, "Exklusiv auf Cubeside ;)");
        // sender.sendMessage("");
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return null;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
}
