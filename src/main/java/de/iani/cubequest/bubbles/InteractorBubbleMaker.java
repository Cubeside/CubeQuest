package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questGiving.QuestGiver;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InteractorBubbleMaker {
    
    private static final int SPREAD_OVER_TICKS = 10;
    private static final int MAX_BUBBLE_DISTANCE = 100;
    private static final int SECTOR_SIZE = MAX_BUBBLE_DISTANCE * 2;
    
    private Set<Player>[] players;
    private Set<BubbleTarget> targets;
    
    int xLength;
    int zLength;
    int lowestX;
    int lowestZ;
    private Set<BubbleTarget>[][] targetsBySector;
    
    @SuppressWarnings("unchecked")
    public InteractorBubbleMaker() {
        this.players = new Set[SPREAD_OVER_TICKS];
        for (int i = 0; i < SPREAD_OVER_TICKS; i++) {
            this.players[i] = new HashSet<>();
        }
        
        this.targets = new HashSet<>();
        updateTargets();
    }
    
    public void updateTargets() {
        for (QuestGiver giver: CubeQuest.getInstance().getQuestGivers()) {
            this.targets.add(new QuestGiverBubbleTarget(giver));
        }
        
        // TODO: InteractorTargets
        
        updateTargetsBySector();
    }
    
    @SuppressWarnings("unchecked")
    private void updateTargetsBySector() {
        if (this.targetsBySector != null) {
            // TODO, ggf return
        }
        
        computeSizeOfArray(true);
        computeSizeOfArray(false);
        this.targetsBySector = new Set[this.xLength][this.zLength];
        
        for (BubbleTarget target: this.targets) {
            Location loc = target.getLocation();
            int x = getSector(loc.getBlockX(), true);
            int z = getSector(loc.getBlockZ(), false);
            
            Set<BubbleTarget> set = this.targetsBySector[x][z];
            if (set == null) {
                set = new HashSet<>();
                this.targetsBySector[x][z] = set;
            }
            set.add(target);
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
        
        maxPos = (int) Math.ceil(maxPos / (double) SECTOR_SIZE) + 1;
        minPos = (int) Math.floor(minPos / (double) SECTOR_SIZE) - 1;
        
        int length = Math.abs(maxPos - minPos);
        
        if (xAxis) {
            this.xLength = length;
            this.lowestX = minPos;
        } else {
            this.zLength = length;
            this.lowestZ = minPos;
        }
    }
    
    private int getSector(int location, boolean xAxis) {
        location -= (xAxis ? this.lowestX : this.lowestZ);
        location /= 100;
        return location;
    }
    
    private Set<BubbleTarget> getSectorTargets(int x, int z) {
        if (!checkBounds(x, z)) {
            return null;
        }
        
        return this.targetsBySector[x][z];
    }
    
    private Set<BubbleTarget>[] getLocalTargets(Location loc) {
        int x = getSector(loc.getBlockX(), true);
        int z = getSector(loc.getBlockZ(), false);
        
        int additionalX = (x * SECTOR_SIZE + this.lowestX); // TODO: hier ermitteln, ob noch weiter
                                                            // in +/- x-Richtung gesucht werden muss
                                                            // (1 für +, -1 für -, 0 für nein)
        int additionalY; // analog
    }
    
    private boolean checkBounds(int x, int z) {
        return !(x < 0 || x >= this.xLength || z < 0 || z >= this.zLength);
    }
    
    public void tick(long tick) {
        int playerIndex = (int) tick % SPREAD_OVER_TICKS;
        Set<Player> tickPlayers = this.players[playerIndex];
        
        for (Player player: tickPlayers) {
            Location playerLoc = player.getLocation();
            
            Set<BubbleTarget>[] localTargets = getLocalTargets(playerLoc);
            
            for (Set<BubbleTarget> set: localTargets) {
                for (BubbleTarget target: set) {
                    if (target.getLocation().distance(playerLoc) <= MAX_BUBBLE_DISTANCE) {
                        // TODO: spawn bubbles
                    }
                }
            }
        }
        
    }
    
}
