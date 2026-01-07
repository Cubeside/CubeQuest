package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;


public class GameModeCondition extends QuestCondition {

    private GameMode gm;

    public GameModeCondition(boolean visible, GameMode gm) {
        super(visible);
        init(gm);
    }

    public GameModeCondition(Map<String, Object> serialized) {
        super(serialized);
        init(GameMode.valueOf((String) serialized.get("gm")));
    }

    private void init(GameMode gm) {
        this.gm = Objects.requireNonNull(gm);
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return player.getGameMode() == this.gm;
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        return Component.text("GameMode: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.gm), NamedTextColor.GREEN));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("gm", this.gm.name());
        return result;
    }

}
