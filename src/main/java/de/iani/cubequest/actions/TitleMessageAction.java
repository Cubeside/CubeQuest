package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;


public class TitleMessageAction extends DelayableAction {

    private Component title;
    private Component subtitle;

    private int fadeIn;
    private int stay;
    private int fadeOut;

    public TitleMessageAction(long delay, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        super(delay);

        this.title = Objects.requireNonNull(title);
        this.subtitle = Objects.requireNonNull(subtitle);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public TitleMessageAction(Map<String, Object> serialized) {
        super(serialized);

        this.title = ChatAndTextUtil.getComponentOrConvert(serialized, "title");
        this.subtitle = ChatAndTextUtil.getComponentOrConvert(serialized, "subtitle");
        this.fadeIn = (Integer) serialized.get("fadeIn");
        this.stay = (Integer) serialized.get("stay");
        this.fadeOut = (Integer) serialized.get("fadeOut");
    }

    protected Component toIndividualComponent(Component message, Player player) {
        return ComponentUtilAdventure.replacePattern(message, StringMessageAction.PLAYER_NAME_PATTERN,
                Component.text(player.getName()));
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            Component individualTitle = toIndividualComponent(this.title, player);
            Component individualSubtitle = toIndividualComponent(this.subtitle, player);

            player.showTitle(Title.title(individualTitle, individualSubtitle, this.fadeIn, this.stay, this.fadeOut));
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        return Component.textOfChildren(
                Component
                        .textOfChildren(msg, Component.text("Titel ("),
                                Component.text(this.fadeIn, NamedTextColor.GREEN), Component.text(" in, "),
                                Component.text(this.stay, NamedTextColor.GREEN), Component.text(" stay, "),
                                Component.text(this.fadeOut, NamedTextColor.GREEN), Component.text(" out): "))
                        .color(NamedTextColor.DARK_AQUA),
                this.title, Component.text(" | ", NamedTextColor.DARK_AQUA), this.subtitle);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("title", SerializableAdventureComponent.ofOrNull(this.title));
        result.put("subtitle", SerializableAdventureComponent.ofOrNull(this.subtitle));
        result.put("fadeIn", this.fadeIn);
        result.put("stay", this.stay);
        result.put("fadeOut", this.fadeOut);
        return result;
    }

}
