package de.iani.cubequest.interaction;

import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

public abstract class PlayerInteractInteractorEvent implements Cancellable {

    protected final PlayerEvent original;
    private Interactor interactor;

    public PlayerInteractInteractorEvent(PlayerEvent original, Interactor interactor) {
        if (!(original instanceof Cancellable)) {
            throw new IllegalArgumentException("original must be cancellable.");
        }

        this.original = original;
        this.interactor = interactor;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    @Override
    public boolean equals(Object obj) {
        return original.equals(obj);
    }

    public abstract Action getAction();

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public boolean isCancelled() {
        return ((Cancellable) original).isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        ((Cancellable) original).setCancelled(cancel);
    }

    @Override
    public String toString() {
        return original.toString();
    }

}
