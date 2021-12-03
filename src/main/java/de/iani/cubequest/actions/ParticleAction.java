package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.Locatable;
import de.iani.cubesideutils.bukkit.Particles;
import de.iani.cubesideutils.bukkit.StringUtilBukkit;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class ParticleAction extends LocatedAction {
    
    public static void main(String[] args) {
        for (Particle p : Particle.values()) {
            if (p.getDataType() != Void.class) {
                System.out.println(p + " " + p.getDataType());
            }
        }
    }
    
    public static class ParticleData implements ConfigurationSerializable {
        
        public enum Type {
            
            DUST_OPTIONS(DustOptions.class), ITEM_STACK(ItemStack.class), BLOCK_DATA(BlockData.class);
            
            public static Type fromDataType(Class<?> dataType) {
                for (Type type : values()) {
                    if (type.dataType == dataType) {
                        return type;
                    }
                }
                return null;
            }
            
            public final Class<?> dataType;
            
            private Type(Class<?> dataType) {
                this.dataType = dataType;
            }
        }
        
        private Type type;
        private Object data;
        
        public ParticleData(Object data) {
            this.data = data;
            
            if (data != null) {
                boolean valid = false;
                for (Particle particle : Particle.values()) {
                    if (particle.name().startsWith("LEGACY")) {
                        continue;
                    }
                    if (particle.getDataType().isAssignableFrom(data.getClass())) {
                        valid = true;
                        this.type = Type.fromDataType(particle.getDataType());
                        if (this.type == null) {
                            throw new AssertionError(
                                    "Unkown particle dataType " + particle.getDataType().getName() + "!");
                        }
                        break;
                    }
                }
                
                if (!valid) {
                    throw new IllegalArgumentException("The given data is invalid for all non-legacy particle types.");
                }
            }
        }
        
        public ParticleData(Map<String, Object> serialized) {
            String typeString = (String) serialized.get("type");
            if (typeString == null) {
                this.type = null;
                this.data = null;
                return;
            }
            
            this.type = Type.valueOf(typeString);
            
            switch (this.type) {
                case DUST_OPTIONS:
                    Color color = (Color) serialized.get("color");
                    float size = ((Number) serialized.get("size")).floatValue();
                    this.data = new DustOptions(color, size);
                    break;
                case ITEM_STACK:
                    ItemStack stack = (ItemStack) serialized.get("stack");
                    this.data = stack;
                    break;
                case BLOCK_DATA:
                    String blockDataString = (String) serialized.get("blockData");
                    BlockData blockData = Bukkit.getServer().createBlockData(blockDataString);
                    this.data = blockData;
                    break;
                default:
                    throw new AssertionError("Unknown Type " + this.type + "!");
            }
        }
        
        public Object getData() {
            return this.data;
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            
            result.put("type", this.type == null ? null : this.type.name());
            if (this.type == null) {
                return result;
            }
            
            switch (this.type) {
                case DUST_OPTIONS:
                    DustOptions du = (DustOptions) this.data;
                    result.put("color", du.getColor());
                    result.put("size", du.getSize());
                    return result;
                case ITEM_STACK:
                    ItemStack stack = (ItemStack) this.data;
                    result.put("stack", stack);
                    return result;
                case BLOCK_DATA:
                    BlockData bd = (BlockData) this.data;
                    result.put("blockData", bd.getAsString());
                    return result;
            }
            
            throw new AssertionError("Unknown Type " + this.type + "!");
        }
        
        @Override
        public String toString() {
            if (this.type == null) {
                return "null";
            }
            
            switch (this.type) {
                case DUST_OPTIONS:
                    DustOptions du = (DustOptions) this.data;
                    return StringUtilBukkit.toNiceString(du.getColor()) + ", " + du.getSize();
                case ITEM_STACK:
                    ItemStack stack = (ItemStack) this.data;
                    return ItemsAndStrings.toNiceString(stack);
                case BLOCK_DATA:
                    BlockData bd = (BlockData) this.data;
                    return bd.getAsString();
            }
            
            throw new AssertionError("Unknown Type " + this.type + "!");
        }
        
    }
    
    private boolean backwardsIncompatible = false;
    private Particle particle;
    private double amountPerTick;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private int numberOfTicks;
    private double extra;
    private ParticleData particleData;
    
    public ParticleAction(long delay, Particle particle, double amountPerTick, int numberOfTicks,
            ActionLocation location, double offsetX, double offsetY, double offsetZ, double extra,
            ParticleData particleData) {
        super(delay, location);
        
        init(particle, amountPerTick, numberOfTicks, offsetX, offsetY, offsetZ, extra, particleData);
    }
    
    public ParticleAction(Map<String, Object> serialized) {
        super(serialized);
        
        String particleString = (String) serialized.get("particle");
        Particle particle = null;
        try {
            particle = Particle.valueOf(particleString);
        } catch (IllegalArgumentException e) {
            this.backwardsIncompatible = true;
            particle = null;
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Particle " + particleString + " is no longer available!");
        }
        
        init(particle, ((Number) serialized.get("amountPerTick")).doubleValue(),
                ((Number) serialized.get("numberOfTicks")).intValue(),
                ((Number) serialized.get("offsetX")).doubleValue(), ((Number) serialized.get("offsetY")).doubleValue(),
                ((Number) serialized.get("offsetZ")).doubleValue(), ((Number) serialized.get("extra")).doubleValue(),
                (ParticleData) serialized.get("particleData"));
    }
    
    private void init(Particle particle, double amountPerTick, int numberOfTicks, double offsetX, double offsetY,
            double offsetZ, double extra, ParticleData particleData) {
        this.particle = Objects.requireNonNull(particle);
        this.amountPerTick = amountPerTick;
        this.numberOfTicks = numberOfTicks;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
        this.particleData = Objects.requireNonNull(particleData);
        
        if (amountPerTick <= 0) {
            throw new IllegalArgumentException("amountPerTick must be positive");
        }
        if (numberOfTicks <= 0) {
            throw new IllegalArgumentException("numberOfTicks must be positive");
        }
        if (offsetX < 0 || offsetY < 0 || offsetZ < 0) {
            throw new IllegalArgumentException("offestes may not be negatibe");
        }
    }
    
    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        if (this.backwardsIncompatible) {
            return (player, data) -> {
            };
        }
        
        return (player, data) -> {
            if (this.numberOfTicks == 1) {
                Particles.spawnParticles(player, this.particle, this.amountPerTick,
                        getLocation().getLocation(player, data), this.offsetX, this.offsetY, this.offsetZ, this.extra,
                        this.particleData.getData());
            } else {
                Locatable loc = getLocation().getLocatable(player, data);
                Particles.spawnParticles(CubeQuest.getInstance(), player, this.particle, this.amountPerTick,
                        this.numberOfTicks, loc, this.offsetX, this.offsetY, this.offsetZ, this.extra,
                        this.particleData.getData());
            }
        };
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent tagComp = new TextComponent("Partikel: " + this.amountPerTick + " " + this.particle + " ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        
        TextComponent locComp = new TextComponent(getLocation().getLocationInfo(true));
        tagComp.addExtra(locComp);
        resultMsg[0].addExtra(tagComp);
        
        tagComp.addExtra(" ± (" + this.offsetX + ", " + this.offsetY + ", " + this.offsetZ + ") für "
                + this.numberOfTicks + " Ticks. Extra: " + this.extra + ", Daten: " + this.particleData);
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("particle", this.particle.name());
        result.put("amountPerTick", this.amountPerTick);
        result.put("offsetX", this.offsetX);
        result.put("offsetY", this.offsetY);
        result.put("offsetZ", this.offsetZ);
        result.put("numberOfTicks", this.numberOfTicks);
        result.put("extra", this.extra);
        result.put("particleData", this.particleData);
        
        return result;
    }
    
}
