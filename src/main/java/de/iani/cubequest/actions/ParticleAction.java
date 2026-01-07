package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.Locatable;
import de.iani.cubesideutils.bukkit.Particles;
import de.iani.cubesideutils.bukkit.StringUtilBukkit;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class ParticleAction extends LocatedAction {

    public static class ParticleData implements ConfigurationSerializable {

        public enum Type {

            DUST_OPTIONS(DustOptions.class),
            DUST_TRANSITION(DustTransition.class),
            ITEM_STACK(ItemStack.class),
            BLOCK_DATA(BlockData.class),
            COLOR(Color.class),
            INTEGER(Integer.class),
            FLOAT(Float.class);

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
                case DUST_TRANSITION:
                    Color from = (Color) serialized.get("from");
                    Color to = (Color) serialized.get("to");
                    size = ((Number) serialized.get("size")).floatValue();
                    this.data = new DustTransition(from, to, size);
                    break;
                case ITEM_STACK:
                    Object stackSerialized = serialized.get("stack");
                    this.data = stackSerialized instanceof ItemStack stack ? stack
                            : stackSerialized instanceof byte[] bytes ? ItemStack.deserializeBytes(bytes) : null;
                    break;
                case BLOCK_DATA:
                    String blockDataString = (String) serialized.get("blockData");
                    BlockData blockData = Bukkit.getServer().createBlockData(blockDataString);
                    this.data = blockData;
                    break;
                case COLOR:
                    Color cValue = (Color) serialized.get("value");
                    this.data = cValue;
                    break;
                case INTEGER:
                    Integer iValue = ((Number) serialized.get("value")).intValue();
                    this.data = iValue;
                    break;
                case FLOAT:
                    Float fValue = ((Number) serialized.get("value")).floatValue();
                    this.data = fValue;
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
                case DUST_TRANSITION:
                    DustTransition dt = (DustTransition) this.data;
                    result.put("from", dt.getColor());
                    result.put("to", dt.getToColor());
                    result.put("size", dt.getSize());
                    return result;
                case ITEM_STACK:
                    ItemStack stack = (ItemStack) this.data;
                    result.put("stack", stack == null ? null : stack.serializeAsBytes());
                    return result;
                case BLOCK_DATA:
                    BlockData bd = (BlockData) this.data;
                    result.put("blockData", bd.getAsString());
                    return result;
                case COLOR:
                case INTEGER:
                case FLOAT:
                    result.put("value", this.data);
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
                case DUST_TRANSITION:
                    DustTransition dt = (DustTransition) this.data;
                    return StringUtilBukkit.toNiceString(dt.getColor()) + " -> "
                            + StringUtilBukkit.toNiceString(dt.getToColor()) + ", " + dt.getSize();
                case ITEM_STACK:
                    ItemStack stack = (ItemStack) this.data;
                    return ComponentUtilAdventure.plainText(ItemStacks.toComponent(stack));
                case BLOCK_DATA:
                    BlockData bd = (BlockData) this.data;
                    return bd.getAsString();
                case COLOR:
                    Color cValue = (Color) this.data;
                    return StringUtilBukkit.toNiceString(cValue);
                case INTEGER:
                case FLOAT:
                    return String.valueOf(this.data);
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

        String particleString = DataUpdater.updateParticleName((String) serialized.get("particle"));
        Particle particle = null;
        try {
            particle = Particle.valueOf(particleString);
        } catch (IllegalArgumentException e) {
            this.backwardsIncompatible = true;
            particle = null;
            Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Particle " + particleString + " is no longer available! QuestID: " + questId);
        }

        init(particle, ((Number) serialized.get("amountPerTick")).doubleValue(),
                ((Number) serialized.get("numberOfTicks")).intValue(),
                ((Number) serialized.get("offsetX")).doubleValue(), ((Number) serialized.get("offsetY")).doubleValue(),
                ((Number) serialized.get("offsetZ")).doubleValue(), ((Number) serialized.get("extra")).doubleValue(),
                (ParticleData) serialized.get("particleData"));
    }

    private void init(Particle particle, double amountPerTick, int numberOfTicks, double offsetX, double offsetY,
            double offsetZ, double extra, ParticleData particleData) {
        this.particle = this.backwardsIncompatible ? null : Objects.requireNonNull(particle);
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
    public ParticleAction relocate(ActionLocation location) {
        return new ParticleAction(getDelay(), this.particle, this.amountPerTick, this.numberOfTicks, location,
                this.offsetX, this.offsetY, this.offsetZ, this.extra, this.particleData);
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
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        Component locComp = getLocation().getLocationInfo(true);

        return msg.append(Component.text("Partikel: " + this.amountPerTick + " " + this.particle + " ").append(locComp)
                .append(Component.text(" ± (" + this.offsetX + ", " + this.offsetY + ", " + this.offsetZ + ") für "
                        + this.numberOfTicks + " Ticks. Extra: " + this.extra + ", Daten: " + this.particleData)))
                .color(NamedTextColor.DARK_AQUA);
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
