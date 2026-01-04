package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.PotionEffects;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;


public class PotionEffectAction extends DelayableAction {

    private PotionEffect effect;

    public PotionEffectAction(long delay, PotionEffect effect) {
        super(delay);

        init(effect);
    }

    public PotionEffectAction(Map<String, Object> serialized) {
        super(serialized);

        init((PotionEffect) serialized.get("effect"));
    }

    private void init(PotionEffect effect) {
        this.effect = Objects.requireNonNull(effect);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> this.effect.apply(player);
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        return msg.append(Component.text("Trank-Effekt: ").append(PotionEffects.toComponent(this.effect)))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("effect", this.effect);
        return result;
    }

}
