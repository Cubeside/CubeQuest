package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.ComponentUtilAdventure;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;


public class BossBarMessageAction extends StringMessageAction {

    private BarColor color;
    private BarStyle style;
    private long duration;

    public BossBarMessageAction(long delay, String message, BarColor color, BarStyle style, long duration) {
        super(delay, message);

        this.color = Objects.requireNonNull(color);
        this.style = Objects.requireNonNull(style);
        this.duration = duration;
    }

    public BossBarMessageAction(Map<String, Object> serialized) {
        super(serialized);

        this.color = BarColor.valueOf((String) serialized.get("color"));
        this.style = BarStyle.valueOf((String) serialized.get("style"));
        this.duration = ((Number) serialized.get("duration")).longValue();
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            BossBar bar = Bukkit.createBossBar(getMessage(player), this.color, this.style);
            bar.addPlayer(player);
            bar.setVisible(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                bar.setVisible(false);
            }, this.duration);
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        return Component
                .textOfChildren(msg, Component.text("Boss-Bar ("),
                        Component.text(this.color.toString(), NamedTextColor.GREEN), Component.text(", "),
                        Component.text(this.style.toString(), NamedTextColor.GREEN), Component.text(", "),
                        Component.text(this.duration + " Ticks", NamedTextColor.GREEN), Component.text("): "),
                        ComponentUtilAdventure.getLegacyComponentSerializer().deserialize(getMessage()))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("color", this.color.name());
        result.put("style", this.style.name());
        result.put("duration", this.duration);
        return result;
    }

}
