package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class PlayerInteractBlockInteractorEvent extends PlayerInteractInteractorEvent {
    
    public PlayerInteractBlockInteractorEvent(PlayerInteractEvent event,
            BlockInteractor interactor) {
        super(event, interactor);
    }
    
    @Override
    public Action getAction() {
        return ((PlayerInteractEvent) this.original).getAction();
    }
    
    @Override
    public Player getPlayer() {
        return ((PlayerInteractEvent) this.original).getPlayer();
    }
    
}
