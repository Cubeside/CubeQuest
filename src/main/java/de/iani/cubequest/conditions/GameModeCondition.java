package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
    public boolean fullfills(Player player, PlayerData data) {
        return player.getGameMode() == this.gm;
    }
    
    @Override
    public BaseComponent[] getConditionInfo() {
        return new ComponentBuilder(ChatColor.DARK_AQUA + "GameMode: " + ChatColor.GREEN + this.gm)
                .create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("gm", this.gm.name());
        return result;
    }
    
}
