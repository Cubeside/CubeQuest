package de.iani.cubequest.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.iani.cubequest.interaction.NPCInteractor;
import de.iani.cubequest.interaction.PlayerLeftClickNPCInteractorEvent;
import de.iani.cubequest.interaction.PlayerRightClickNPCInteractorEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NPCEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCCRightClickEventMonitor(NPCRightClickEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerRightClickNPCInteractorEvent(event, new NPCInteractor(event.getNPC())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCCLeftClickEventMonitor(NPCLeftClickEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerLeftClickNPCInteractorEvent(event, new NPCInteractor(event.getNPC())));
    }

}
