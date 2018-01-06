package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import net.citizensnpcs.api.event.NPCClickEvent;

public abstract class PlayerInteractNPCInteractorEvent extends PlayerInteractInteractorEvent {
    
    public PlayerInteractNPCInteractorEvent(NPCClickEvent original, NPCInteractor interactor) {
        super(original, interactor);
    }
    
    @Override
    public Player getPlayer() {
        return ((NPCClickEvent) original).getClicker();
    }
    
}
