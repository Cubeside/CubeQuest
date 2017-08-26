package de.iani.cubequest.wrapper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCClickEvent;

public class NPCEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCClickEvent(NPCClickEvent event) {
        NPCClickEventWrapper wrapper = new NPCClickEventWrapper(event);
        CubeQuest.getInstance().getEventListener().onNPCClickEvent(wrapper);
    }

}
