package de.iani.cubequest.interaction;

import net.citizensnpcs.api.event.NPCClickEvent;
import org.bukkit.entity.Player;

public abstract class PlayerInteractNPCInteractorEvent extends PlayerInteractInteractorEvent<NPCClickEvent> {
    
    public PlayerInteractNPCInteractorEvent(NPCClickEvent original, NPCInteractor interactor) {
        super(original, interactor);
    }
    
    @Override
    public NPCInteractor getOriginalInteractor() {
        return (NPCInteractor) super.getOriginalInteractor();
    }
    
    @Override
    public Player getPlayer() {
        return this.original.getClicker();
    }
    
}
