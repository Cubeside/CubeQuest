package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questGiving.QuestGiver;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InteractorBubbleMaker {
    
    public static final int SPREAD_OVER_TICKS = 10;
    private static final int MAX_BUBBLE_DISTANCE = 40;
    private static final int SECTOR_SIZE = MAX_BUBBLE_DISTANCE * 2;
    
    private boolean running;
    
    private Set<Player>[] players;
    private Set<BubbleTarget> targets;
    
    int xLength;
    int zLength;
    int lowestX;
    int lowestZ;
    private Set<BubbleTarget>[][] targetsBySector;
    
    @SuppressWarnings("unchecked")
    public InteractorBubbleMaker() {
        this.running = false;
        this.players = new Set[SPREAD_OVER_TICKS];
        for (int i = 0; i < SPREAD_OVER_TICKS; i++) {
            this.players[i] = new HashSet<>();
        }
        
        this.targets = new HashSet<>();
    }
    
    public void updateTargets() {
        this.running = true;
        
        for (QuestGiver giver: CubeQuest.getInstance().getQuestGivers()) {
            if (giver.getInteractor().getLocation() == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "QuestGiver " + giver.getName() + " has no location! Won't bubble.");
                continue;
            }
            this.targets.add(new QuestGiverBubbleTarget(giver));
        }
        
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
        
        int rawLength = maxPos - minPos;
        int length = 1;
        for (; rawLength > 0; rawLength -= SECTOR_SIZE) {
            length++;
        }
        
        if (xAxis) {
            this.xLength = length;
            this.lowestX = minPos - (SECTOR_SIZE / 2);
        } else {
            this.zLength = length;
            this.lowestZ = minPos - (SECTOR_SIZE / 2);
        }
    }
    
    public void playerJoined(Player player) {
        UUID id = player.getUniqueId();
        this.players[id.hashCode() % SPREAD_OVER_TICKS].add(player);
    }
    
    public void playerLeft(Player player) {
        UUID id = player.getUniqueId();
        this.players[id.hashCode() % SPREAD_OVER_TICKS].remove(player);
    }
    
    public boolean registerQuestTargetBubbleMaker(QuestTargetBubbleTarget target) {
        boolean result = this.targets.add(target);
        if (result && this.running) {
            updateTargetsBySector();
        }
        return result;
    }
    
    public boolean unregisterQuestTargetBubbleMaker(QuestTargetBubbleTarget target) {
        boolean result = this.targets.remove(target);
        if (result && this.running) {
            updateTargetsBySector();
        }
        return result;
    }
    
    private int getSector(int location, boolean xAxis) {
        location -= (xAxis ? this.lowestX : this.lowestZ);
        location = Math.floorDiv(location, SECTOR_SIZE);
        return location;
    }
    
    private Set<BubbleTarget> getSectorTargets(int x, int z) {
        if (!checkBounds(x, z)) {
            return null;
        }
        
        return this.targetsBySector[x][z];
    }
    
    @SuppressWarnings("unchecked")
    private Set<BubbleTarget>[] getLocalTargets(Location loc) {
        int x = getSector(loc.getBlockX(), true);
        int z = getSector(loc.getBlockZ(), false);
        
        int additionalX =
                (loc.getBlockX() - (x * SECTOR_SIZE + (SECTOR_SIZE / 2) + this.lowestX) < 0) ? -1
                        : 1;
        int additionalZ =
                (loc.getBlockZ() - (z * SECTOR_SIZE + (SECTOR_SIZE / 2) + this.lowestZ) < 0) ? -1
                        : 1;
        
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
    
    public void tick(long tick) {
        int playerIndex = (int) tick % SPREAD_OVER_TICKS;
        Set<Player> tickPlayers = this.players[playerIndex];
        
        for (Player player: tickPlayers) {
            Location playerLoc = player.getLocation();
            PlayerData playerData = null;
            
            Set<BubbleTarget>[] localTargets = getLocalTargets(playerLoc);
            
            for (Set<BubbleTarget> set: localTargets) {
                if (set == null) {
                    continue;
                }
                for (BubbleTarget target: set) {
                    Location targetLoc = target.getLocation(true);
                    if (targetLoc == null) {
                        continue;
                    }
                    if (targetLoc.distance(playerLoc) <= MAX_BUBBLE_DISTANCE) {
                        if (playerData == null) {
                            playerData = CubeQuest.getInstance().getPlayerData(player);
                        }
                        target.bubbleIfConditionsMet(player, playerData, targetLoc);
                    }
                }
            }
        }
        
    }
    
}
