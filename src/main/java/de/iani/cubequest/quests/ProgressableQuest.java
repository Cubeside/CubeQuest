package de.iani.cubequest.quests;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.AddConditionCommand;
import de.iani.cubequest.conditions.GameModeCondition;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public abstract class ProgressableQuest extends Quest {

    private List<QuestCondition> questProgressConditions;
    private List<QuestCondition> visibleProgressConditions;

    public ProgressableQuest(int id, String name, Component displayMessage) {
        super(id, name, displayMessage);
        init();
    }

    public ProgressableQuest(int id) {
        super(id);
        init();
    }

    protected boolean usuallyRequiresSurvivalMode() {
        return true;
    }

    private void init() {
        this.questProgressConditions = new ArrayList<>();
        this.visibleProgressConditions = new ArrayList<>();
        if (usuallyRequiresSurvivalMode()) {
            addQuestProgressCondition(new GameModeCondition(false, GameMode.SURVIVAL), false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        this.questProgressConditions =
                (List<QuestCondition>) yc.get("questProgressConditions", this.questProgressConditions);
        this.visibleProgressConditions.clear();
        for (QuestCondition cond : this.questProgressConditions) {
            if (cond.isVisible()) {
                this.visibleProgressConditions.add(cond);
            }
        }
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("questProgressConditions", this.questProgressConditions);
        return super.serializeToString(yc);
    }

    public List<QuestCondition> getQuestProgressConditions() {
        return Collections.unmodifiableList(this.questProgressConditions);
    }

    public boolean fulfillsProgressConditions(Player player, PlayerData data) {
        if (!isReady()) {
            return false;
        }

        if (data.getPlayerStatus(getId()) != Status.GIVENTO) {
            return false;
        }

        return this.questProgressConditions.stream().allMatch(qpc -> qpc.fulfills(player, data));
    }

    public boolean fulfillsProgressConditions(Player player) {
        return this.fulfillsProgressConditions(player, CubeQuest.getInstance().getPlayerData(player));
    }

    public void addQuestProgressCondition(QuestCondition qpc) {
        this.addQuestProgressCondition(qpc, true);
    }

    protected void addQuestProgressCondition(QuestCondition qpc, boolean updateDatabase) {
        if (qpc == null) {
            throw new NullPointerException();
        }
        this.questProgressConditions.add(qpc);

        if (updateDatabase) {
            updateIfReal();
        }

        if (qpc.isVisible()) {
            this.visibleProgressConditions.add(qpc);
        }
    }

    public void removeQuestProgressCondition(int questProgessConditionIndex) {
        QuestCondition cond = this.questProgressConditions.remove(questProgessConditionIndex);
        updateIfReal();
        if (cond.isVisible()) {
            this.visibleProgressConditions.remove(cond);
        }
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                text("Fortschrittsbedingungen:", NamedTextColor.DARK_AQUA)
                        .append(this.questProgressConditions.isEmpty() ? text(" KEINE", NamedTextColor.GOLD) : empty()),
                "/" + AddConditionCommand.FULL_PROGRESS_COMMAND));

        for (int i = 0; i < this.questProgressConditions.size(); i++) {
            QuestCondition qpc = this.questProgressConditions.get(i);
            result.add(text("Bedingung " + (i + 1) + (qpc.isVisible() ? "" : " (unsichtbar)") + ": ",
                    NamedTextColor.DARK_AQUA).append(qpc.getConditionInfo(true)) // MUST be Adventure Component now
            );
        }

        result.add(empty());
        return result;
    }

    @Override
    public List<Component> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        Player player = data.getPlayer();

        // Assumption: internal method migrated too
        List<Component> result = getSpecificStateInfoInternal(data, indentionLevel);
        if (this.visibleProgressConditions.isEmpty()) {
            return result;
        }

        result.add(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1).append(text("Dabei folgende "))
                .append(text(this.visibleProgressConditions.size() == 1 ? "Bedingung" : "Bedingungen"))
                .append(text(" eingehalten:")).color(NamedTextColor.DARK_AQUA));

        for (QuestCondition cond : this.visibleProgressConditions) {
            Boolean ok = (player == null) ? null : cond.fulfills(player, data);

            result.add(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1)
                    .append(ChatAndTextUtil.getTrueFalseToken(ok)).append(text(" "))
                    .append(ChatAndTextUtil.stripEvents(cond.getConditionInfo())));
        }

        return result;
    }

    protected abstract List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel);

}
