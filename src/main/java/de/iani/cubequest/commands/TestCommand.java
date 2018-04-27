package de.iani.cubequest.commands;

import de.iani.cubequest.util.ChatAndTextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class TestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        if (!sender.isOp()) {
            ChatAndTextUtil.sendNoPermissionMessage(sender);
            return true;
        }
        
        try {
            throw new ArrayIndexOutOfBoundsException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String getRequiredPermission() {
        return "cubequest.*";
    }
    
    @Override
    public boolean requiresPlayer() {
        return false;
    }
    
}
