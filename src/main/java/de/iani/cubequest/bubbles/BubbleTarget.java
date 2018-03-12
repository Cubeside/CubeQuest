package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.Util;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BubbleTarget {
    
    private static double AMOUNT_PER_BLOCK = 3;
    
    private double cachedHalfHeight;
    private double cachedHalfWidth;
    private double cachedAmount;
    private boolean clearCache;
    
    public BubbleTarget() {
        this.clearCache = true;
        
        final Random ran = new Random(Double.doubleToLongBits(Math.random()));
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CubeQuest.getInstance(), () -> {
            this.clearCache = true;
        }, 0 + ran.nextInt(20), 0 + ran.nextInt(20));
    }
    
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
            if (this.clearCache) {
                this.cachedHalfHeight = 1.1 * getHeight() / 2;
                this.cachedHalfWidth = 1.1 * getWidth() / 2;
                this.cachedAmount =
                        2 * this.cachedHalfHeight * this.cachedHalfWidth * AMOUNT_PER_BLOCK;
                this.clearCache = false;
            }
            Util.spawnColoredDust(player, this.cachedAmount,
                    InteractorBubbleMaker.SPREAD_OVER_TICKS, cachedTargetLocation.getX(),
                    cachedTargetLocation.getY() + this.cachedHalfHeight,
                    cachedTargetLocation.getZ(), this.cachedHalfWidth, this.cachedHalfHeight,
                    this.cachedHalfWidth, getBubbleColors());
        }
    }
    
    protected abstract Color[] getBubbleColors();
    
}
