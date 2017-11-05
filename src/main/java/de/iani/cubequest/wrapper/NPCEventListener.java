package de.iani.cubequest.wrapper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NPCEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCCRightlickEventMonitor(NPCRightClickEvent event) {
        NPCRightClickEventWrapper wrapper = new NPCRightClickEventWrapper(event);
        CubeQuest.getInstance().getEventListener().onNPCRightClickEventMonitor(wrapper);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onNPCRightClickEvent(NPCRightClickEvent event) {
        NPCRightClickEventWrapper wrapper = new NPCRightClickEventWrapper(event);
        CubeQuest.getInstance().getEventListener().onNPCRightClickEventNormal(wrapper);
    }

}
