package de.iani.cubequest.commands;

import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class TestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (!sender.isOp()) {
            ChatAndTextUtil.sendNoPermissionMessage(sender);
            return true;
        }
        
        throw new IllegalStateException("Testen ist verboten!");
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
