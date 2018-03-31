package de.iani.cubequest.commands;

import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        if (!sender.isOp()) {
            ChatAndTextUtil.sendNoPermissionMessage(sender);
            return true;
        }
        
        sender.sendMessage(ItemStackUtil.toNiceString(
                ((Player) sender).getInventory().getContents(), ChatColor.GOLD.toString()));
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return "cubequest.admin";
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
}
