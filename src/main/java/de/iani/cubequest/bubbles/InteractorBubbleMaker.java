package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestGiver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class InteractorBubbleMaker {
    
    static final int SPREAD_OVER_TICKS = 20;
    static final int MAX_BUBBLE_DISTANCE = 40;
    static final int SECTOR_SIZE = MAX_BUBBLE_DISTANCE * 2;
    
    private boolean running;
    
    private Set<Player>[] players;
    private Map<String, Set<BubbleTarget>> targets;
    private Map<String, WorldSectors> worldSectors;
    
    @SuppressWarnings("unchecked")
    public InteractorBubbleMaker() {
        this.running = false;
        this.players = new Set[SPREAD_OVER_TICKS];
        for (int i = 0; i < SPREAD_OVER_TICKS; i++) {
            this.players[i] = new HashSet<>();
        }
        
        this.targets = new HashMap<>();
        this.worldSectors = new HashMap<>();
        
        CubeQuest.getInstance().getEventListener().addOnPlayerJoin(player -> playerJoined(player));
        CubeQuest.getInstance().getEventListener().addOnPlayerQuit(player -> playerLeft(player));
    }
    
    private Set<BubbleTarget> getTargets(Location loc) {
        return getTargets(loc.getWorld());
    }
    
    private Set<BubbleTarget> getTargets(World world) {
        return getTargets(world.getName());
    }
    
    private Set<BubbleTarget> getTargets(String worldName) {
        Set<BubbleTarget> result = this.targets.get(worldName);
        if (result == null) {
            result = new HashSet<>();
            this.targets.put(worldName, result);
        }
        return result;
    }
    
    private WorldSectors getWorldSectors(Location loc) {
        return getWorldSectors(loc.getWorld());
    }
    
    private WorldSectors getWorldSectors(World world) {
        return getWorldSectors(world.getName());
    }
    
    private WorldSectors getWorldSectors(String worldName) {
        WorldSectors result = this.worldSectors.get(worldName);
        if (result == null) {
            result = new WorldSectors(getTargets(worldName));
            this.worldSectors.put(worldName, result);
        }
        return result;
    }
    
    public void setup() {
        if (this.running) {
            throw new IllegalStateException("Already running!");
        }
        this.running = true;
        
        for (QuestGiver giver : CubeQuest.getInstance().getQuestGivers()) {
            Location giverLoc = giver.getInteractor().getLocation();
            if (giverLoc == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "QuestGiver " + giver.getName() + " has no location! Won't bubble.");
                continue;
            }
            
            getTargets(giverLoc).add(new QuestGiverBubbleTarget(giver));
        }
        
        for (String worldName : this.targets.keySet()) {
            getWorldSectors(worldName).updateTargetsBySector();
        }
    }
    
    public void playerJoined(Player player) {
        UUID id = player.getUniqueId();
        this.players[Math.abs(id.hashCode() % SPREAD_OVER_TICKS)].add(player);
    }
    
    public void playerLeft(Player player) {
        UUID id = player.getUniqueId();
        this.players[Math.abs(id.hashCode() % SPREAD_OVER_TICKS)].remove(player);
    }
    
    public boolean registerBubbleTarget(BubbleTarget target) {
        Location targetLoc = target.getLocation();
        if (targetLoc == null) {
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "No Location found for BubbleTarget " + target + " (registering).");
            return false;
        }
        
        Set<BubbleTarget> set = getTargets(targetLoc);
        boolean result = set.add(target);
        if (result && this.running) {
            getWorldSectors(targetLoc).registerBubbleTarget(target, targetLoc);
        }
        return result;
    }
    
    public boolean unregisterBubbleTarget(BubbleTarget target) {
        Location targetLoc = target.getLocation();
        if (targetLoc == null) {
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "No Location found for BubbleTarget " + target + " (unregistering).");
            return false;
        }
        
        Set<BubbleTarget> set = getTargets(targetLoc);
        boolean result = set.remove(target);
        if (result && this.running) {
            getWorldSectors(targetLoc).unregisterBubbleTarget(target, targetLoc);
        }
        return result;
    }
    
    public void updateBubbleTarget(BubbleTarget target, Location oldLocation) {
        String oldWorldName = oldLocation == null ? null : oldLocation.getWorld().getName();
        Set<BubbleTarget> oldSet = oldWorldName == null ? null : this.targets.get(oldWorldName);
        if (oldSet != null) {
            boolean result = oldSet.remove(target);
            if (result && this.running) {
                getWorldSectors(oldLocation).unregisterBubbleTarget(target, oldLocation);
            }
        }
        
        Location targetLoc = target.getLocation();
        if (targetLoc == null) {
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "No Location found for BubbleTarget " + target + " (updating).");
            return;
        }
        Set<BubbleTarget> set = getTargets(targetLoc);
        set.add(target); // must return true
        
        if (this.running) {
            getWorldSectors(targetLoc).registerBubbleTarget(target, targetLoc);
        }
    }
    
    public void tick(long tick) {
        int playerIndex = (int) tick % SPREAD_OVER_TICKS;
        Set<Player> tickPlayers = this.players[playerIndex];
        
        for (Player player : tickPlayers) {
            Location playerLoc = player.getLocation();
            PlayerData playerData = null;
            
            Set<BubbleTarget>[] localTargets =
                    getWorldSectors(playerLoc).getLocalTargets(playerLoc);
            
            for (Set<BubbleTarget> set : localTargets) {
                if (set == null) {
                    continue;
                }
                for (BubbleTarget target : set) {
                    Location targetLoc = target.getLocation(true);
                    if (targetLoc == null || targetLoc.getWorld() != playerLoc.getWorld()) {
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
