package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

public abstract class PlayerInteractInteractorEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    protected final Event original;
    private final Interactor interactor;
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public PlayerInteractInteractorEvent(Event original, Interactor interactor) {
        if (!(original instanceof Cancellable)) {
            throw new IllegalArgumentException("original must be cancellable.");
        }
        
        this.original = original;
        this.interactor = interactor;
    }
    
    public Interactor getInteractor() {
        return this.interactor;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.original.equals(obj);
    }
    
    public abstract Action getAction();
    
    public abstract Player getPlayer();
    
    @Override
    public int hashCode() {
        return this.original.hashCode();
    }
    
    @Override
    public boolean isCancelled() {
        return ((Cancellable) this.original).isCancelled();
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        ((Cancellable) this.original).setCancelled(cancel);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @Override
    public String toString() {
        return this.original.toString();
    }
    
}
