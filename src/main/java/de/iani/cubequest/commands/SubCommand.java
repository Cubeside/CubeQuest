package de.iani.cubequest.commands;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {
    
    public boolean requiresPlayer() {
        return false;
    }
    
    public boolean allowsCommandBlock() {
        return false;
    }
    
    public String getRequiredPermission() {
        return null;
    }
    
    public abstract boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args);
    
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return null;
    }
    
    public String getUsage() {
        return "";
    }
}
