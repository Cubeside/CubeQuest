package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.Util;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BubbleTarget {
    
    public abstract String getName();
    
    public Location getLocation() {
        return getLocation(false);
    }
    
    public abstract Location getLocation(boolean ignoreCache);
    
    public abstract double getHeight();
    
    public abstract double getWidth();
    
    protected abstract boolean conditionMet(Player player, PlayerData data);
    
    public void bubbleIfConditionsMet(Player player, PlayerData data,
            Location cachedTargetLocation) {
        if (conditionMet(player, data)) {
            double halfHeight = getHeight() / 2;
            double halfWidth = getWidth() / 2;
            Util.spawnColoredDust(player, 5, InteractorBubbleMaker.SPREAD_OVER_TICKS,
                    cachedTargetLocation.getX(), cachedTargetLocation.getY() + halfHeight,
                    cachedTargetLocation.getZ(), halfWidth, halfHeight, halfWidth,
                    getBubbleColors());
        }
    }
    
    protected abstract Color[] getBubbleColors();
    
}
