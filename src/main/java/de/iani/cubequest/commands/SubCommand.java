package de.iani.cubequest.commands;

import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.List;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public abstract class SubCommand {
    
    public boolean requiresPlayer() {
        return false;
    }
    
    public boolean allowsCommandBlock() {
        return false;
    }
    
    public boolean isVisible() {
        return true;
    }
    
    public abstract String getRequiredPermission();
    
    public abstract boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args);
    
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return null;
    }
    
    public String getUsage() {
        return "";
    }
    
    public boolean execute(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        if (allowsCommandBlock()
                || !(sender instanceof BlockCommandSender || sender instanceof CommandMinecart)) {
            if (!requiresPlayer() || sender instanceof Player) {
                if (getRequiredPermission() == null
                        || sender.hasPermission(getRequiredPermission())) {
                    return onCommand(sender, command, alias, commandString, args);
                } else {
                    ChatAndTextUtil.sendNoPermissionMessage(sender);
                }
            } else {
                ChatAndTextUtil.sendErrorMessage(sender,
                        "Diesen Befehl können nur Spieler ausführen!");
            }
        } else {
            ChatAndTextUtil.sendErrorMessage(sender,
                    "This command is not allowed for CommandBlocks!");
        }
        return false;
    }
}
