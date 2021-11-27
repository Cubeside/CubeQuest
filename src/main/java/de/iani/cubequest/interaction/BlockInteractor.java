package de.iani.cubequest.interaction;

import de.iani.cubequest.util.BlockLocation;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;

public class BlockInteractor extends Interactor {
    
    private BlockLocation location;
    
    public BlockInteractor(BlockLocation location) {
        if (location == null) {
            throw new NullPointerException();
        }
        this.location = location;
    }
    
    public BlockInteractor(Location location) {
        this.location = new BlockLocation(location);
    }
    
    public BlockInteractor(Block block) {
        this(block.getLocation());
    }
    
    public BlockInteractor(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);
        
        this.location = (BlockLocation) serialized.get("location");
        if (this.location == null) {
            throw new InvalidConfigurationException();
        }
    }
    
    @Override
    public int compareTo(Interactor o) {
        int result = super.compareTo(o);
        if (result != 0) {
            return result;
        }
        
        BlockInteractor other = (BlockInteractor) o;
        return this.location.compareTo(other.location);
    }
    
    @Override
    public BlockLocation getIdentifier() {
        return this.location;
    }
    
    public Block getBlock() {
        return this.getLocation().getBlock();
    }
    
    @Override
    protected String getUncachedName() {
        if (!isForThisServer()) {
            return null;
        }
        
        Block block = this.location.getLocation().getBlock();
        return ItemsAndStrings.toNiceString(block.getType()) + "block";
    }
    
    @Override
    public boolean isLegal() {
        return true;
    }
    
    @Override
    public String getInfo() {
        return "Block bei x=" + this.location.getX() + ", y=" + this.location.getY() + ", z=" + this.location.getZ();
    }
    
    @Override
    public Location getLocation(boolean ignoreCache) {
        return this.location.getBottomCenterLocation();
    }
    
    @Override
    public double getHeight() {
        return 1;
    }
    
    @Override
    public double getWidth() {
        return 1;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("location", this.location);
        return result;
    }
    
}
