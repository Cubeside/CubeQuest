package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.Util;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BubbleTarget {
    
    public abstract String getName();
    
    public abstract Location getLocation();
    
    protected abstract boolean conditionMet(Player player, PlayerData data);
    
    public void bubbleIfConditionsMet(Player player, PlayerData data,
            Location cachedTargetLocation) {
        if (conditionMet(player, data)) {
            Util.spawnColoredDust(player, 5, InteractorBubbleMaker.SPREAD_OVER_TICKS,
                    cachedTargetLocation.getX(), cachedTargetLocation.getY() + 1,
                    cachedTargetLocation.getZ(), 0.5, 1, 0.5, getBubbleColors());
        }
    }
    
    protected abstract Color[] getBubbleColors();
    
}
