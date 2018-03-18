package de.iani.cubequest.interaction;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.block.Action;

public class PlayerRightClickNPCInteractorEvent extends PlayerInteractNPCInteractorEvent {
    
    public PlayerRightClickNPCInteractorEvent(NPCRightClickEvent original,
            NPCInteractor interactor) {
        super(original, interactor);
    }
    
    @Override
    public Action getAction() {
        return Action.RIGHT_CLICK_AIR;
    }
    
}
