package de.iani.cubequest.interaction;

import de.iani.cubequest.CubeQuest;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockInteractorDamagedEvent<T extends Event & Cancellable>
        extends InteractorDamagedEvent<T> {
    
    private enum Type {
        BREAK, PLACE, OTHER;
    }
    
    private final Player player;
    private final Type type;
    
    public BlockInteractorDamagedEvent(T original, BlockInteractor interactor) {
        super(original, interactor);
        
        if (original instanceof BlockBreakEvent) {
            this.player = ((BlockBreakEvent) original).getPlayer();
            this.type = Type.BREAK;
        } else if (original instanceof BlockPlaceEvent) {
            this.player = ((BlockPlaceEvent) original).getPlayer();
            this.type = Type.PLACE;
        } else {
            this.player = null;
            this.type = Type.OTHER;
        }
        
    }
    
    @Override
    public BlockInteractor getInteractor() {
        return (BlockInteractor) super.getInteractor();
    }
    
    @Override
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public String getNoPermissionMessage() {
        switch (this.type) {
            case BREAK:
                return "Du kannst diesen Block nicht abbauen!";
            case PLACE:
                return "Du kannst diesen Block nicht platzieren!";
            default:
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "Unexpected call to BlockInteractorDamaged#getNoPermissionMessage() with type = "
                                + this.type + ".");
                return "Aktion nicht möglich.";
        }
    }
    
}
