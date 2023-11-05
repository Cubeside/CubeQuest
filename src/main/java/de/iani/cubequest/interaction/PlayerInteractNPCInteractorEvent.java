package de.iani.cubequest.interaction;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractNPCInteractorEvent extends PlayerInteractInteractorEvent<PlayerInteractEntityEvent> {

    public PlayerInteractNPCInteractorEvent(PlayerInteractEntityEvent original, NPCInteractor interactor) {
        super(original, interactor);
    }

    @Override
    public NPCInteractor getOriginalInteractor() {
        return (NPCInteractor) super.getOriginalInteractor();
    }

    @Override
    public Player getPlayer() {
        return this.original.getPlayer();
    }

    @Override
    public Action getAction() {
        return Action.RIGHT_CLICK_AIR;
    }

}
