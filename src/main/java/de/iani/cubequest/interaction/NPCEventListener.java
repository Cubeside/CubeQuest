package de.iani.cubequest.interaction;

import de.cubeside.npcs.data.SpawnedNPCData;
import de.iani.cubequest.CubeQuest;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCEventListener implements Listener {

    public NPCEventListener() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }

    public PlayerInteractNPCInteractorEvent onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        SpawnedNPCData npc = CubeQuest.getInstance().getNPCReg().getByEntity(event.getRightClicked());
        if (npc == null) {
            return null;
        }

        return new PlayerInteractNPCInteractorEvent(event, new NPCInteractor(npc.getUUID()));
    }

}
