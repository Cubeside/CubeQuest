package de.iani.cubequest.conditions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.StringUtil;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class SpecialPlayerPropertyCondition extends QuestCondition {

    public static enum PropertyType {

        VANISH(p -> CubeQuest.getInstance().getVanishPlugin().getManager().isVanished(p));

        public final Predicate<Player> fulfillment;

        private PropertyType(Predicate<Player> fulfillment) {
            this.fulfillment = fulfillment;
        }
    }

    private final PropertyType type;

    public SpecialPlayerPropertyCondition(boolean visible, PropertyType type) {
        super(visible);

        this.type = Objects.requireNonNull(type);
    }

    public SpecialPlayerPropertyCondition(Map<String, Object> serialized) {
        super(serialized);

        this.type = PropertyType.valueOf((String) serialized.get("type"));
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return this.type.fulfillment.test(player);
    }

    @Override
    public Component getConditionInfo() {
        return Component.text(StringUtil.capitalizeFirstLetter(this.type.name(), true) + " aktiv",
                NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("type", this.type.name());
        return result;
    }

}
