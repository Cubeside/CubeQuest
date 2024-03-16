package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class TimeOfDayCondition extends QuestCondition {

    private int min;
    private int max;

    public TimeOfDayCondition(boolean visible, int minTime, int maxTime) {
        super(visible);

        init(minTime, maxTime);
    }

    public TimeOfDayCondition(Map<String, Object> serialized) {
        super(serialized);

        init(((Number) serialized.get("min")).intValue(), ((Number) serialized.get("max")).intValue());
    }

    private void init(int minTime, int maxTime) {
        this.min = minTime;
        this.max = maxTime;

        if (this.min > this.max || this.max > 24000) {
            throw new IllegalArgumentException("invalid times: [" + this.min + ", " + this.max + "]");
        }
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        long time = player.getWorld().getTime();
        return this.min <= time && time <= this.max;
    }

    @Override
    public BaseComponent[] getConditionInfo() {
        return new ComponentBuilder("Tageszeit zwischen " + this.min + " und " + this.max).color(ChatColor.DARK_AQUA)
                .create();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("min", this.min);
        result.put("max", this.max);
        return result;
    }

}
