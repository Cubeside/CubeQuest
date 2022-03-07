package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.plugin.CubesideUtils;
import de.iani.playerUUIDCache.CachedPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class TransferPlayerCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "transferPlayer";
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        if (args.remaining() < 2) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib Ursprungs- und Zielspieler der Übertragung an.");
            return true;
        }
        
        String oldName = args.getNext();
        CachedPlayer oldPlayer = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(oldName);
        if (oldPlayer == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + oldName + " nicht gefunden.");
            return true;
        } else if (CubesideUtils.getInstance().getGlobalDataHelper().isOnAnyServer(oldPlayer.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, oldPlayer.getName() + " ist online.");
            return true;
        }
        
        String newName = args.getNext();
        CachedPlayer newPlayer = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(newName);
        if (newPlayer == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + newName + " nicht gefunden.");
            return true;
        } else if (newPlayer.getUniqueId().equals(oldPlayer.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Spieler dürfen nicht gleich sein.");
            return true;
        } else if (CubesideUtils.getInstance().getGlobalDataHelper().isOnAnyServer(newPlayer.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, newPlayer.getName() + " ist online.");
            return true;
        }
        
        String confirmation = args.getNext("");
        if (!confirmation.equalsIgnoreCase("TRANSFER")) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bestätige die Übertragung der Daten von " + oldPlayer.getName()
                    + " zu " + newPlayer.getName() + " mit TRANSFER am Ende des Befehls.");
            return true;
        }
        
        CubeQuest.getInstance().transferPlayer(oldPlayer.getUniqueId(), newPlayer.getUniqueId());
        ChatAndTextUtil.sendNormalMessage(sender,
                "Daten von " + oldPlayer.getName() + " auf " + newPlayer.getName() + " übertragen.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.TRANSFER_PLAYERS_PERMISSION;
    }
    
    @Override
    public String getUsage() {
        return "<from> <to> TRANSFER";
    }
    
}
