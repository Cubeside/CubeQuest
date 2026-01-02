package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class RemovePotionEffectAction extends DelayableAction {

    private PotionEffectType potionEffectType;

    public RemovePotionEffectAction(long delay, PotionEffectType potionEffectType) {
        super(delay);

        init(potionEffectType);
    }


    public RemovePotionEffectAction(Map<String, Object> serialized) {
        super(serialized);

        String potionEffectTypeString =
                DataUpdater.updatePotionEffectTypeName((String) serialized.get("potionEffectType"));
        PotionEffectType potionEffectType;
        NamespacedKey key = NamespacedKey.fromString(potionEffectTypeString);
        if (key == null) {
            potionEffectType = PotionEffectType.getByName(potionEffectTypeString);
        } else {
            potionEffectType = Registry.POTION_EFFECT_TYPE.get(key);
        }

        init(potionEffectType);
    }

    private void init(PotionEffectType potionEffectType) {
        this.potionEffectType = Objects.requireNonNull(potionEffectType);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> player.removePotionEffect(this.potionEffectType);
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        return msg.append(Component.text("Trank-Effekt entfernen: " + this.potionEffectType.getKey().asMinimalString()))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("potionEffectType", this.potionEffectType.getKey().asString());
        return result;
    }

}
