package de.iani.cubequest.conditions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
    public boolean fullfills(Player player, PlayerData data) {
        return CubeQuest.getInstance().hasServerFlag(this.flag);
    }
    
    @Override
    public List<BaseComponent[]> getConditionInfoInternal() {
        return Collections.singletonList(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Server mit Flag: " + ChatColor.GREEN + this.flag).create());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("flag", this.flag);
        return result;
    }
    
}
