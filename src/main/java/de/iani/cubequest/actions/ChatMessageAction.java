package de.iani.cubequest.actions;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class ChatMessageAction extends ComponentMessageAction {

    public ChatMessageAction(long delay, Component message) {
        super(delay, message);
    }

    public ChatMessageAction(Component message) {
        this(0, message);
    }

    public ChatMessageAction(Map<String, Object> serialized) {
        super(serialized);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            ChatAndTextUtil.sendMessage(player, getMessage(player));
        };
    }

    @Override
    public Component getActionInfo() {
        Component delayComp = getDelayComponent();
        if (delayComp == null) {
            delayComp = empty();
        }

        return Component.textOfChildren(delayComp, text("Chat-Nachricht: ", NamedTextColor.DARK_AQUA), getMessage());
    }

}
