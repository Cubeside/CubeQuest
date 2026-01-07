package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;


public class EffectAction extends LocatedAction {

    public static class EffectData implements ConfigurationSerializable {

        public enum Type {

            MATERIAL(Material.class), BLOCK_FACE(BlockFace.class), INTEGER(Integer.class);

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

        public EffectData(Object data) {
            this.data = data;

            if (data != null) {
                boolean valid = false;
                for (Effect effect : Effect.values()) {
                    if (effect.getData() == null) {
                        continue;
                    }
                    if (effect.getData().getAnnotation(Deprecated.class) != null) {
                        continue;
                    }
                    if (effect.getData().isAssignableFrom(data.getClass())) {
                        valid = true;
                        this.type = Type.fromDataType(effect.getData());
                        if (this.type == null) {
                            throw new AssertionError("Unkown effect dataType " + effect.getData().getName() + "!");
                        }
                        break;
                    }
                }

                if (!valid) {
                    throw new IllegalArgumentException(
                            "The given data is invalid for all effect types (excluding POTION_BREAK).");
                }
            }
        }

        public EffectData(Map<String, Object> serialized) {
            String typeString = (String) serialized.get("type");
            if (typeString == null) {
                this.type = null;
                this.data = null;
                return;
            }

            this.type = Type.valueOf(typeString);
            switch (this.type) {
                case MATERIAL:
                    Material mat = Material.valueOf((String) serialized.get("material"));
                    this.data = mat;
                    break;
                case BLOCK_FACE:
                    BlockFace bf = BlockFace.valueOf((String) serialized.get("blockFace"));
                    this.data = bf;
                    break;
                case INTEGER:
                    Integer i = ((Number) serialized.get("integer")).intValue();
                    this.data = i;
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
                case MATERIAL:
                    Material mat = (Material) this.data;
                    result.put("material", mat.name());
                    return result;
                case BLOCK_FACE:
                    BlockFace bf = (BlockFace) this.data;
                    result.put("blockFace", bf.name());
                    return result;
                case INTEGER:
                    Integer i = (Integer) this.data;
                    result.put("integer", i);
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
                case MATERIAL:
                    Material mat = (Material) this.data;
                    return mat.toString();
                case BLOCK_FACE:
                    BlockFace bf = (BlockFace) this.data;
                    return bf.toString();
                case INTEGER:
                    Integer i = (Integer) this.data;
                    return i.toString();
            }

            throw new AssertionError("Unknown Type " + this.type + "!");
        }
    }

    private boolean backwardsIncompatible = false;
    private Effect effect;
    private String effectString;
    private EffectData effectData;

    public EffectAction(long delay, Effect effect, ActionLocation location, EffectData effectData) {
        super(delay, location);

        init(effect, effectData);
    }

    public EffectAction(Map<String, Object> serialized) {
        super(serialized);

        String effectString = (String) serialized.get("effect");
        Effect effect;
        try {
            effect = Effect.valueOf(effectString);
        } catch (IllegalArgumentException e) {
            this.backwardsIncompatible = true;
            this.effectString = effectString;
            effect = null;
            Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Effect " + effectString + " is no longer available! Quest-ID: " + questId);
        }
        init(effect, (EffectData) serialized.get("effectData"));
    }

    private void init(Effect effect, EffectData effectData) {
        if (!this.backwardsIncompatible) {
            this.effect = Objects.requireNonNull(effect);
            this.effectString = effect.name();
        }
        this.effectData = Objects.requireNonNull(effectData);
    }

    @Override
    public EffectAction relocate(ActionLocation location) {
        return new EffectAction(getDelay(), this.effect, location, this.effectData);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        if (this.backwardsIncompatible) {
            return (player, data) -> {
            };
        }
        return (player, data) -> player.playEffect(getLocation().getLocation(player, data), this.effect,
                this.effectData.getData());
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        Component effectComp = Component.text(this.effectString, NamedTextColor.GREEN);
        if (this.backwardsIncompatible) {
            effectComp = Component.textOfChildren(effectComp, Component.text(" (nicht vorhanden)", NamedTextColor.RED));
        }

        Component locComp = getLocation().getLocationInfo(true);

        return Component.textOfChildren(msg, Component.text("Effekt: "), effectComp, Component.space(), locComp,
                Component.text(", Daten: "), Component.text(String.valueOf(this.effectData), NamedTextColor.GREEN))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("effect", this.effectString);
        result.put("effectData", this.effectData);

        return result;
    }

}
