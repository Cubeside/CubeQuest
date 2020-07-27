package de.iani.cubequest;

import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.CommandExceptionHandler;
import de.iani.cubesideutils.bukkit.commands.exceptions.InternalCommandException;
import de.iani.cubesideutils.bukkit.commands.exceptions.NoPermissionException;
import de.iani.cubesideutils.bukkit.commands.exceptions.NoPermissionForPathException;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CubeQuestCommandExceptionHandler implements CommandExceptionHandler {
    
    @Override
    public boolean handleNoPermission(NoPermissionException thrown) {
        ChatAndTextUtil.sendNoPermissionMessage(thrown.getSender());
        return true;
    }
    
    @Override
    public boolean handleNoPermissionForPath(NoPermissionForPathException thrown) {
        ChatAndTextUtil.sendNoPermissionMessage(thrown.getSender());
        return true;
    }
    
    @Override
    public boolean handleInternalException(InternalCommandException thrown) {
        CommandSender sender = thrown.getSender();
        Throwable cause = thrown.getCause();
        
        ChatAndTextUtil.sendErrorMessage(sender, "Ein interner Fehler ist aufgetreten.");
        
        if (sender instanceof Player) {
            CubeQuest.getInstance().getLogHandler().notifyPersonalLog((Player) sender);
            if (sender.hasPermission(CubeQuest.SEE_EXCEPTIONS_PERMISSION)) {
                ChatAndTextUtil.sendWarningMessage(sender, ChatAndTextUtil.exceptionToString(cause));
            }
        }
        
        CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Beim Ausführen eines CubeQuest-Commands ist ein interner Fehler aufgetreten.", cause);
        return true;
    }
    
}
