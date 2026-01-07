package de.iani.cubequest.conditions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class ServerFlagCondition extends QuestCondition {

    private String flag;

    public ServerFlagCondition(boolean visible, String flag) {
        super(visible);
        init(flag);
    }

    public ServerFlagCondition(Map<String, Object> serialized) {
        super(serialized);
        init((String) serialized.get("flag"));
    }

    private void init(String flag) {
        this.flag = Objects.requireNonNull(flag);
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return CubeQuest.getInstance().hasServerFlag(this.flag);
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        return Component.text("Server mit Flag: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(this.flag, NamedTextColor.GREEN));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("flag", this.flag);
        return result;
    }

}
