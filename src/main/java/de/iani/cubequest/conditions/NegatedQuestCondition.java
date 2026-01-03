package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class NegatedQuestCondition extends QuestCondition {

    private QuestCondition original;

    public static QuestCondition negate(QuestCondition original) {
        return (original instanceof NegatedQuestCondition negated) ? negated.original
                : new NegatedQuestCondition(original);
    }

    private NegatedQuestCondition(QuestCondition original) {
        super(original.isVisible());
        if (original instanceof NegatedQuestCondition) {
            throw new IllegalArgumentException("original is already negated");
        }
        this.original = Objects.requireNonNull(original);
    }

    public NegatedQuestCondition(Map<String, Object> serialized) {
        this((QuestCondition) serialized.get("original"));
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return !this.original.fulfills(player, data);
    }

    @Override
    public Component getConditionInfo() {
        return Component.text("Nicht: ").append(this.original.getConditionInfo()).color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("original", this.original);
        return result;
    }
}
