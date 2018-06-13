package de.iani.cubequest.interaction;

import de.iani.cubequest.CubeQuest;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

public abstract class PlayerInteractInteractorEvent<T extends Event & Cancellable> extends Event
        implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    protected final T original;
    private final Interactor interactor;
    private final UUID playerId;
    
    private final long tick;
    private boolean cancelledInternal;
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public PlayerInteractInteractorEvent(T original, Interactor interactor) {
        this.original = original;
        this.interactor = interactor;
        this.playerId = getPlayer().getUniqueId();
        this.tick = CubeQuest.getInstance().getTickCount();
        this.cancelledInternal = false;
    }
    
    public Interactor getInteractor() {
        return this.interactor;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerInteractInteractorEvent<?>)) {
            return false;
        }
        
        PlayerInteractInteractorEvent<?> other = (PlayerInteractInteractorEvent<?>) obj;
        if (this.tick != other.tick) {
            return false;
        }
        if (!this.playerId.equals(other.playerId)) {
            return false;
        }
        if (!this.interactor.equals(other.interactor)) {
            return false;
        }
        
        return true;
    }
    
    public abstract Action getAction();
    
    public abstract Player getPlayer();
    
    @Override
    public int hashCode() {
        int result = Long.hashCode(this.tick);
        result = 31 * result + this.interactor.hashCode();
        result = 31 * result + this.playerId.hashCode();
        return result;
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelledInternal;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.original.setCancelled(cancel);
        this.cancelledInternal = cancel;
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
