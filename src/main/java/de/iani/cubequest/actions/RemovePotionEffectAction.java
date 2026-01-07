package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class RemovePotionEffectAction extends DelayableAction {

    private boolean backwardsIncompatible = false;
    private PotionEffectType potionEffectType;
    private String potionEffectTypeString;

    public RemovePotionEffectAction(long delay, PotionEffectType potionEffectType) {
        super(delay);

        this.potionEffectType = Objects.requireNonNull(potionEffectType);
        this.potionEffectTypeString = this.potionEffectType.getKey().asMinimalString();
    }


    @SuppressWarnings("deprecation")
    public RemovePotionEffectAction(Map<String, Object> serialized) {
        super(serialized);

        String potionEffectTypeString =
                DataUpdater.updatePotionEffectTypeName((String) serialized.get("potionEffectType"));
        NamespacedKey key = NamespacedKey.fromString(potionEffectTypeString);
        if (key == null) {
            this.potionEffectType = PotionEffectType.getByName(potionEffectTypeString);
        } else {
            this.potionEffectType = Registry.POTION_EFFECT_TYPE.get(key);
        }

        if (this.potionEffectType == null) {
            this.backwardsIncompatible = true;
            this.potionEffectTypeString = potionEffectTypeString;
            Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "PotionEffectType " + potionEffectTypeString + " is no longer available! Quest-ID: " + questId);
        } else {
            this.potionEffectTypeString = this.potionEffectType.getKey().asMinimalString();
        }
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        if (this.backwardsIncompatible) {
            return (player, data) -> {
            };
        }
        return (player, data) -> player.removePotionEffect(this.potionEffectType);
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        msg = Component
                .textOfChildren(msg, Component.text("Trank-Effekt entfernen: "),
                        Component.text(this.potionEffectTypeString, NamedTextColor.GREEN))
                .color(NamedTextColor.DARK_AQUA);
        if (this.backwardsIncompatible) {
            msg = Component.textOfChildren(msg, Component.text(" (nicht vorhanden)", NamedTextColor.RED));
        }
        return msg;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("potionEffectType", this.potionEffectTypeString);
        return result;
    }

}
