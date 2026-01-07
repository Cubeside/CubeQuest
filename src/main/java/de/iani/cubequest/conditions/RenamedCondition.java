package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class RenamedCondition extends QuestCondition {

    private Component text;
    private QuestCondition original;

    public static QuestCondition rename(Component text, QuestCondition original) {
        original = (original instanceof RenamedCondition) ? ((RenamedCondition) original).original : original;
        return Component.empty().equals(text) ? original : new RenamedCondition(text, original);
    }

    private RenamedCondition(Component text, QuestCondition original) {
        super(true);
        init(text, original);
    }

    public RenamedCondition(Map<String, Object> serialized) {
        super(true);

        init(ChatAndTextUtil.getComponentOrConvert(serialized, "text"), (QuestCondition) serialized.get("original"));
    }

    private void init(Component text, QuestCondition original) {
        this.text = Objects.requireNonNull(text);
        this.original = Objects.requireNonNull(original);
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return this.original.fulfills(player, data);
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        Component result = this.text;
        if (!includeHiddenInfo) {
            return result;
        }

        return result.append(Component.text(" (Intern: ").append(this.original.getConditionInfo(true))
                .append(Component.text(")")).color(NamedTextColor.DARK_AQUA));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("text", SerializableAdventureComponent.ofOrNull(this.text));
        result.put("original", this.original);
        return result;
    }

}
