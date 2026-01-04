package de.iani.cubequest.actions;

import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class ActionBarMessageAction extends ComponentMessageAction {

    public ActionBarMessageAction(long delay, Component message) {
        super(delay, message);
    }

    public ActionBarMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            player.sendActionBar(getMessage(player));
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        return Component.textOfChildren(msg, text("Action-Bar: ", NamedTextColor.DARK_AQUA), getMessage());
    }

}
