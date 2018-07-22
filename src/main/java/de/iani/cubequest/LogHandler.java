package de.iani.cubequest;

import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class LogHandler extends Handler {
    
    private static final int MAX_LOGS_PER_TIME = 3;
    private static final long TIME_IN_TICKS = 1200; // 1 Minute
    
    private CubeQuest plugin;
    
    private Map<Player, Boolean> listeners;
    
    private int count;
    
    public LogHandler() {
        this.plugin = CubeQuest.getInstance();
        this.listeners = new HashMap<>();
        setLevel(Level.SEVERE);
        
        this.plugin.getEventListener().addOnPlayerJoin(p -> playerJoined(p));
        this.plugin.getEventListener().addOnPlayerQuit(p -> playerQuit(p));
        
        this.plugin.getLogger().addHandler(this);
    }
    
    public void playerJoined(Player player) {
        if (player.hasPermission(CubeQuest.SEE_EXCEPTIONS_PERMISSION)) {
            this.listeners.put(player, false);
        }
    }
    
    public void playerQuit(Player player) {
        this.listeners.remove(player);
    }
    
    public void notifyPersonalLog(Player player) {
        this.listeners.put(player, true);
    }
    
    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() < Level.WARNING.intValue()) {
            return;
        }
        if (this.count >= MAX_LOGS_PER_TIME) {
            return;
        }
        this.count++;
        
        Iterator<Player> it = this.listeners.keySet().iterator();
        while (it.hasNext()) {
            Player player = it.next();
            if (!player.hasPermission(CubeQuest.SEE_EXCEPTIONS_PERMISSION)) {
                it.remove();
                continue;
            }
            
            if (this.listeners.get(player)) {
                this.listeners.put(player, false);
                continue;
            }
            
            ChatAndTextUtil.sendErrorMessage(player, "A servere log was made:");
            ChatAndTextUtil.sendWarningMessage(player, record.getMessage());
            if (record.getThrown() != null) {
                ChatAndTextUtil.sendWarningMessage(player,
                        ChatAndTextUtil.exceptionToString(record.getThrown()));
            }
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.count--,
                TIME_IN_TICKS);
    }
    
    @Override
    public void flush() {
        // nothing
    }
    
    @Override
    public void close() throws SecurityException {
        // nothing
    }
    
}
