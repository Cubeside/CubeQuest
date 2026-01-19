package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public abstract class DelayableAction extends QuestAction {

    private long delay;

    public DelayableAction(long delay) {
        this.delay = delay;

        if (delay < 0) {
            throw new IllegalArgumentException("delay must be non negative");
        }
    }

    public DelayableAction(Map<String, Object> serialized) {
        super(serialized);

        long delay = ((Number) serialized.getOrDefault("delay", 0)).longValue();
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be non negative");
        }
        this.delay = delay;
    }

    @Override
    public void perform(Player player, PlayerData data) {
        if (this.delay == 0) {
            getActionPerformer().accept(player, data);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                if (!runIfPlayerOffline() && !player.isOnline()) {
                    return;
                }
                try {
                    getActionPerformer().accept(player, data);
                } catch (Exception e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                            "Exception with action for player " + player.getName() + " " + player.getUniqueId(), e);
                }
            }, this.delay);
        }
    }

    protected abstract BiConsumer<Player, PlayerData> getActionPerformer();

    protected boolean runIfPlayerOffline() {
        return false;
    }

    public long getDelay() {
        return this.delay;
    }

    protected Component getDelayComponent() {
        if (this.delay == 0) {
            return null;
        }

        return Component.textOfChildren(Component.text("Nach "), Component.text(this.delay, NamedTextColor.GREEN),
                Component.text(" Ticks ")).color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("delay", this.delay);
        return result;
    }

}
