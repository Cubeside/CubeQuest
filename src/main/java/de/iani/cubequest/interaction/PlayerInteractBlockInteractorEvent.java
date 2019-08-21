package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class PlayerInteractBlockInteractorEvent extends PlayerInteractInteractorEvent<PlayerInteractEvent> {
    
    public PlayerInteractBlockInteractorEvent(PlayerInteractEvent event, BlockInteractor interactor) {
        super(event, interactor);
    }
    
    @Override
    public BlockInteractor getOriginalInteractor() {
        return (BlockInteractor) super.getOriginalInteractor();
    }
    
    @Override
    public Action getAction() {
        return this.original.getAction();
    }
    
    @Override
    public Player getPlayer() {
        return this.original.getPlayer();
    }
    
}
