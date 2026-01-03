package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.commands.QuestInfoCommand;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;


public class HaveQuestStatusCondition extends QuestCondition {

    private int questId;
    private Status status;

    public HaveQuestStatusCondition(boolean visible, Quest quest, Status status) {
        super(visible);
        init(quest.getId(), status);
    }

    public HaveQuestStatusCondition(Map<String, Object> serialized) {
        super(serialized);
        init(((Number) serialized.get("questId")).intValue(), Status.valueOf((String) serialized.get("status")));
    }

    private void init(int questId, Status status) {
        this.questId = questId;
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return data.getPlayerStatus(this.questId) == this.status;
    }

    @Override
    public Component getConditionInfo() {
        Quest quest = QuestManager.getInstance().getQuest(this.questId);

        TextColor mainColor = (quest == null) ? NamedTextColor.RED : NamedTextColor.DARK_AQUA;

        Component idPart = Component.text(String.valueOf(this.questId)).hoverEvent(HoverEvent.showText(Component
                .text(quest == null ? "Quest existiert nicht" : ("Info zu " + quest.toString() + " anzeigen"))));

        if (quest != null) {
            idPart = idPart.clickEvent(ClickEvent.runCommand("/" + QuestInfoCommand.FULL_COMMAND + " " + this.questId));
        }

        return Component.text("Quest ").append(idPart).append(Component.text(": " + this.status)).color(mainColor);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("questId", this.questId);
        result.put("status", this.status.name());
        return result;
    }

}
