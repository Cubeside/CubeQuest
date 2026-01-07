package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class MinimumQuestLevelCondition extends QuestCondition {

    private int minLevel;

    public MinimumQuestLevelCondition(boolean visible, int minLevel) {
        super(visible);
        init(minLevel);
    }

    public MinimumQuestLevelCondition(Map<String, Object> serialized) {
        super(serialized);
        init(((Number) serialized.get("minLevel")).intValue());
    }

    private void init(int minLevel) {
        if (minLevel < 0) {
            throw new IllegalArgumentException("minLevel must not be negative");
        }
        this.minLevel = minLevel;
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return data.getLevel() >= this.minLevel;
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        return Component.text("Min. Quest-Level: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.minLevel), NamedTextColor.GREEN));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("minLevel", this.minLevel);
        return result;
    }

}
