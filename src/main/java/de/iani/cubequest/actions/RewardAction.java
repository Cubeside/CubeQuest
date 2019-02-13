package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;


public class RewardAction extends QuestAction {
    
    private Reward reward;
    
    public RewardAction(Reward reward) {
        init(reward);
    }
    
    public RewardAction(Map<String, Object> serialized) {
        init((Reward) serialized.get("reward"));
    }
    
    private void init(Reward reward) {
        this.reward = Objects.requireNonNull(reward);
        if (reward.isEmpty()) {
            throw new IllegalArgumentException("reward may not be empty");
        }
    }
    
    public Reward getReward() {
        return this.reward;
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        data.delayReward(this.reward);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(
                ChatColor.DARK_AQUA + "Belohnung: " + this.reward.toNiceString()).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("reward", this.reward);
        return result;
    }
    
}
