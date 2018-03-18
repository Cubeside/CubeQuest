package de.iani.cubequest.interaction;

import net.citizensnpcs.api.event.NPCClickEvent;
import org.bukkit.entity.Player;

public abstract class PlayerInteractNPCInteractorEvent extends PlayerInteractInteractorEvent {
    
    public PlayerInteractNPCInteractorEvent(NPCClickEvent original, NPCInteractor interactor) {
        super(original, interactor);
    }
    
    @Override
    public Player getPlayer() {
        return ((NPCClickEvent) original).getClicker();
    }
    
}
