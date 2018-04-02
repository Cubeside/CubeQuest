package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;

public class WorldSectors {
    
    private Set<BubbleTarget> targets;
    
    private int xLength;
    private int zLength;
    private int lowestX;
    private int lowestZ;
    private Set<BubbleTarget>[][] targetsBySector;
    
    public WorldSectors(Set<BubbleTarget> targets) {
        this.targets = targets;
    }
    
    @SuppressWarnings("unchecked")
    public void updateTargetsBySector() {
        computeSizeOfArray(true);
        computeSizeOfArray(false);
        
        this.targetsBySector = new Set[this.xLength][this.zLength];
        
        for (BubbleTarget target: this.targets) {
            updateSingleTargetSector(target, null, false);
        }
    }
    
    public void updateSingleTargetSector(BubbleTarget target, Location oldLocation,
            boolean remove) {
        
        Set<BubbleTarget> newSet = null;
        if (!remove) {
            Location newLoc = target.getLocation();
            int newX = getSector(newLoc.getBlockX(), true);
            int newZ = getSector(newLoc.getBlockZ(), false);
            
            newSet = this.targetsBySector[newX][newZ];
            if (newSet == null) {
                newSet = new HashSet<>();
                this.targetsBySector[newX][newZ] = newSet;
            }
        }
        
        Set<BubbleTarget> oldSet = null;
        if (oldLocation != null) {
            int oldX = getSector(oldLocation.getBlockX(), true);
            int oldZ = getSector(oldLocation.getBlockZ(), false);
            oldSet = this.targetsBySector[oldX][oldZ];
        }
        
        if (oldSet != null && oldSet != newSet) {
            oldSet.remove(target);
        }
        if (newSet != null) {
            newSet.add(target);
        }
    }
    
    private void computeSizeOfArray(boolean xAxis) {
        int maxPos = 0;
        int minPos = 0;
        for (BubbleTarget target: this.targets) {
            Location loc = target.getLocation();
            int val = xAxis ? loc.getBlockX() : loc.getBlockZ();
            maxPos = val > maxPos ? val : maxPos;
            minPos = val < minPos ? val : minPos;
        }
        
        int rawLength = maxPos - minPos;
        int length = 1;
        for (; rawLength > 0; rawLength -= InteractorBubbleMaker.SECTOR_SIZE) {
            length++;
        }
        
        if (xAxis) {
            this.xLength = length;
            this.lowestX = minPos - (InteractorBubbleMaker.SECTOR_SIZE / 2);
        } else {
            this.zLength = length;
            this.lowestZ = minPos - (InteractorBubbleMaker.SECTOR_SIZE / 2);
        }
    }
    
    private int getSector(int location, boolean xAxis) {
        location -= (xAxis ? this.lowestX : this.lowestZ);
        location = Math.floorDiv(location, InteractorBubbleMaker.SECTOR_SIZE);
        return location;
    }
    
    private Set<BubbleTarget> getSectorTargets(int x, int z) {
        if (!checkBounds(x, z)) {
            return null;
        }
        
        return this.targetsBySector[x][z];
    }
    
    @SuppressWarnings("unchecked")
    public Set<BubbleTarget>[] getLocalTargets(Location loc) {
        int x = getSector(loc.getBlockX(), true);
        int z = getSector(loc.getBlockZ(), false);
        
        int additionalX = (loc.getBlockX() - (x * InteractorBubbleMaker.SECTOR_SIZE
                + (InteractorBubbleMaker.SECTOR_SIZE / 2) + this.lowestX) < 0) ? -1 : 1;
        int additionalZ = (loc.getBlockZ() - (z * InteractorBubbleMaker.SECTOR_SIZE
                + (InteractorBubbleMaker.SECTOR_SIZE / 2) + this.lowestZ) < 0) ? -1 : 1;
        
        Set<BubbleTarget>[] result = new Set[4];
        result[0] = getSectorTargets(x, z);
        result[1] = getSectorTargets(x + additionalX, z);
        result[2] = getSectorTargets(x, z + additionalZ);
        result[3] = getSectorTargets(x + additionalX, z + additionalZ);
        
        return result;
    }
    
    private boolean checkBounds(int x, int z) {
        return !(x < 0 || x >= this.xLength || z < 0 || z >= this.zLength);
    }
    
    private boolean isInSector(Location location) {
        int x = getSector(location.getBlockX(), true);
        int z = getSector(location.getBlockZ(), false);
        
        return checkBounds(x, z);
    }
    
    public void registerBubbleTarget(BubbleTarget target, Location loc) {
        if (isInSector(target.getLocation())) {
            updateSingleTargetSector(target, null, false);
        } else {
            updateTargetsBySector();
        }
    }
    
    public void unregisterBubbleTarget(BubbleTarget target, Location loc) {
        if (isInSector(target.getLocation())) {
            updateSingleTargetSector(target, target.getLocation(), true);
        } else {
            // sollte nicht passieren
            CubeQuest.getInstance().getLogger().log(Level.WARNING,
                    "Unregistering BubbleTarget outside of sectors.");
            updateTargetsBySector();
        }
    }
    
}
