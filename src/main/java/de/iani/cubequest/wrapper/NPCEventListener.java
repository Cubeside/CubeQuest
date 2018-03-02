package de.iani.cubequest.wrapper;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.interaction.NPCInteractor;
import de.iani.cubequest.interaction.PlayerLeftClickNPCInteractorEvent;
import de.iani.cubequest.interaction.PlayerRightClickNPCInteractorEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCEventListener implements Listener {
    
    public NPCEventListener() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onNPCCRightClickEventMonitor(NPCRightClickEvent event) {
        PlayerRightClickNPCInteractorEvent newEvent = new PlayerRightClickNPCInteractorEvent(event,
                new NPCInteractor(event.getNPC().getId()));
        Bukkit.getPluginManager().callEvent(newEvent);
        event.setCancelled(event.isCancelled() || newEvent.isCancelled());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onNPCCLeftClickEventMonitor(NPCLeftClickEvent event) {
        PlayerLeftClickNPCInteractorEvent newEvent = new PlayerLeftClickNPCInteractorEvent(event,
                new NPCInteractor(event.getNPC().getId()));
        Bukkit.getPluginManager().callEvent(newEvent);
        event.setCancelled(event.isCancelled() || newEvent.isCancelled());
    }
    
    public boolean onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        return CubeQuest.getInstance().getNPCReg().getNPC(event.getRightClicked()) != null;
    }
    
}
