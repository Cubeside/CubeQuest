package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.ComponentUtilAdventure;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;


public class TitleMessageAction extends DelayableAction {

    private String title;
    private String subtitle;

    private int fadeIn;
    private int stay;
    private int fadeOut;

    public TitleMessageAction(long delay, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        super(delay);

        this.title = Objects.requireNonNull(title);
        this.subtitle = Objects.requireNonNull(subtitle);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;

        validateMessage(title);
        validateMessage(subtitle);
    }

    public TitleMessageAction(Map<String, Object> serialized) {
        super(serialized);

        this.title = (String) serialized.get("title");
        this.subtitle = (String) serialized.get("subtitle");
        this.fadeIn = (Integer) serialized.get("fadeIn");
        this.stay = (Integer) serialized.get("stay");
        this.fadeOut = (Integer) serialized.get("fadeOut");

        validateMessage(this.title);
        validateMessage(this.subtitle);
    }

    protected String deserializeMessage(Map<String, Object> serialized, String key) {
        if ((Integer) serialized.getOrDefault("version", 0) > 0) {
            return (String) serialized.get(key);
        }
        return ComponentUtilAdventure.serializeComponent(ComponentUtilAdventure.getLegacyComponentSerializer()
                .deserialize((String) serialized.get(key)).compact());
    }

    protected void validateMessage(String message) {
        try {
            ComponentUtilAdventure.deserializeComponent(message);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Component toIndividualComponent(String message, Player player) {
        try {
            return ComponentUtilAdventure.deserializeComponent(
                    MessageAction.PLAYER_NAME_PATTERN.matcher(message).replaceAll(player.getName()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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

        try {
            return msg
                    .append(Component.text(
                            "Titel (" + this.fadeIn + " in, " + this.stay + " stay, " + this.fadeOut + " out): ",
                            NamedTextColor.DARK_AQUA))
                    .append(ComponentUtilAdventure.deserializeComponent(this.title))
                    .append(Component.text(" | ", NamedTextColor.DARK_AQUA))
                    .append(ComponentUtilAdventure.deserializeComponent(this.subtitle));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("version", 1);
        result.put("title", this.title);
        result.put("subtitle", this.subtitle);
        result.put("fadeIn", this.fadeIn);
        result.put("stay", this.stay);
        result.put("fadeOut", this.fadeOut);
        return result;
    }

}
