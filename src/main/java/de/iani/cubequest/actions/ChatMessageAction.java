package de.iani.cubequest.actions;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class ChatMessageAction extends ComponentMessageAction {

    public ChatMessageAction(long delay, String message) {
        super(delay, message);
    }

    public ChatMessageAction(String message) {
        this(0, message);
    }

    public ChatMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            Component msg = CubeQuest.PLUGIN_TAG.append(text(" ")).append(getComponentMessage(player));
            player.sendMessage(msg);
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        msg = msg.append(text("Chat-Nachricht: ", NamedTextColor.DARK_AQUA)).append(getComponentMessage());

        return msg;
    }

}
