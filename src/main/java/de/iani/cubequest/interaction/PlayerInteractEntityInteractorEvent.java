package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;


public class PlayerInteractEntityInteractorEvent
        extends PlayerInteractInteractorEvent<PlayerInteractEntityEvent> {
    
    public PlayerInteractEntityInteractorEvent(PlayerInteractEntityEvent original,
            Interactor interactor) {
        super(original, interactor);
    }
    
    @Override
    public Action getAction() {
        return Action.RIGHT_CLICK_AIR;
    }
    
    @Override
    public Player getPlayer() {
        return this.original.getPlayer();
    }
    
}
