package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.logging.Level;
import org.bukkit.Bukkit;
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
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            try {
                CubeQuest.getInstance().getLogger().log(Level.WARNING, "test0");
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "test1");
                throw new ArrayIndexOutOfBoundsException();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 5L);
        
        return true;
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
