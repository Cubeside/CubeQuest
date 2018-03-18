package de.iani.cubequest.interaction;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.event.block.Action;

public class PlayerLeftClickNPCInteractorEvent extends PlayerInteractNPCInteractorEvent {
    
    public PlayerLeftClickNPCInteractorEvent(NPCLeftClickEvent original, NPCInteractor interactor) {
        super(original, interactor);
    }
    
    @Override
    public Action getAction() {
        return Action.LEFT_CLICK_AIR;
    }
    
}
