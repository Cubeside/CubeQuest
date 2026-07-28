package de.iani.cubequest.conditions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class CombinedCondition extends QuestCondition {

    public enum CombinationType {
        AND, OR
    }

    private CombinationType combinationType;
    private List<QuestCondition> conditions;

    public CombinedCondition(boolean visible, CombinationType combinationType, List<QuestCondition> conditions) {
        super(visible);
        this.combinationType = Objects.requireNonNull(combinationType);
        this.conditions = List.copyOf(conditions);
    }

    @SuppressWarnings("unchecked")
    public CombinedCondition(Map<String, Object> serialized) {
        super(serialized);
        this.combinationType = CombinationType.valueOf((String) serialized.get("combinationType"));
        this.conditions = List.copyOf((List<QuestCondition>) serialized.get("conditions"));
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return switch (this.combinationType) {
            case AND -> this.conditions.stream().allMatch(c -> c.fulfills(player, data));
            case OR -> this.conditions.stream().anyMatch(c -> c.fulfills(player, data));
        };
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        Component prefix = switch (this.combinationType) {
            case AND -> Component.text("Alle folgenden: ", NamedTextColor.DARK_AQUA);
            case OR -> Component.text("Eine der folgenden: ", NamedTextColor.DARK_AQUA);
        };

        Component conditionsInfo =
                this.conditions.stream().map(c -> c.getConditionInfo(includeHiddenInfo)).reduce(Component.empty(),
                        (a, b) -> (Component.IS_NOT_EMPTY.test(a)
                                ? Component.textOfChildren(a, Component.text(", ", NamedTextColor.DARK_AQUA), b)
                                : b));

        return Component.textOfChildren(prefix, conditionsInfo);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("combinationType", this.combinationType.name());
        result.put("conditions", this.conditions);
        return result;
    }

    @Override
    public QuestCondition replaceSurvivalCondition() {
        CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                "For some reason, replaceSurvivalCondition() was called on a CombinedCondition. This should not happen.",
                new Exception());
        return this;
    }

}
