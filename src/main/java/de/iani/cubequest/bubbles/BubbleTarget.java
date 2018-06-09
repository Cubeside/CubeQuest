package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.interaction.BlockInteractor;
import de.iani.cubequest.interaction.EntityInteractor;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorType;
import de.iani.cubequest.util.Util;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class BubbleTarget {
    
    private static double AMOUNT_PER_BLOCK = 3;
    
    private double cachedHalfHeight;
    private double cachedHalfWidth;
    private double cachedAmount;
    private long clearCache;
    private Random ran;
    
    protected static double getStrechingFactor(Interactor interactor, boolean height) {
        InteractorType type = InteractorType.fromClass(interactor.getClass());
        switch (type) {
            case NPC:
                return 1.1;
            case ENTITY:
                EntityInteractor eInt = (EntityInteractor) interactor;
                switch (eInt.getEntity().getType()) {
                    case ARMOR_STAND:
                        return height ? 1.2 : 1.4;
                    default:
                        return 1.1;
                }
            case BLOCK:
                BlockInteractor bInt = (BlockInteractor) interactor;
                Block block = bInt.getBlock();
                switch (block.getType()) {
                    case SKULL:
                        return 0.8;
                    default:
                        return block.getType().isTransparent() ? 1.0 : 1.25;
                }
        }
        throw new NullPointerException();
    }
    
    public BubbleTarget() {
        this.clearCache = 0;
        this.ran = new Random(Double.doubleToLongBits(Math.random()));
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
            clearCacheIfOutdated();
            Util.spawnColoredDust(player, this.cachedAmount,
                    InteractorBubbleMaker.SPREAD_OVER_TICKS, cachedTargetLocation.getX(),
                    cachedTargetLocation.getY() + this.cachedHalfHeight,
                    cachedTargetLocation.getZ(), this.cachedHalfWidth, this.cachedHalfHeight,
                    this.cachedHalfWidth, getBubbleColors());
        }
    }
    
    private void clearCacheIfOutdated() {
        long tick = CubeQuest.getInstance().getTickCount();
        if (tick >= this.clearCache) {
            this.cachedHalfHeight = getHeight() / 2.0;
            this.cachedHalfWidth = getWidth() / 2.0;
            this.cachedAmount =
                    2.0 * this.cachedHalfHeight * this.cachedHalfWidth * AMOUNT_PER_BLOCK;
            this.clearCache = tick + 200L + this.ran.nextInt(20);
        }
    }
    
    protected abstract Color[] getBubbleColors();
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getName();
    }
    
}
