package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public Component getActionInfo() {
        return Component.textOfChildren(Component.text("Belohnung: "), this.reward.toComponent())
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("reward", this.reward);
        return result;
    }

    @Override
    public RewardAction performDataUpdate() {
        Reward updated = this.reward.performDataUpdate();
        return updated == this.reward ? this : new RewardAction(updated);
    }

}
