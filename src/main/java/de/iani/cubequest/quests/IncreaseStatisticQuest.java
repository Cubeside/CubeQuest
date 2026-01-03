package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.AddOrRemoveStatisticCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesidestats.api.event.PlayerStatisticUpdatedEvent;
import de.iani.cubesideutils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class IncreaseStatisticQuest extends AmountQuest {

    private Set<StatisticKey> statisticKeys;
    private Component overwrittenStatisticsMessage;

    public IncreaseStatisticQuest(int id, Component name, Component displayMessage, Set<StatisticKey> statisticKeys,
            int amount) {
        super(id, name, displayMessage, amount);

        this.statisticKeys = new LinkedHashSet<>(statisticKeys);
    }

    public IncreaseStatisticQuest(int id) {
        this(id, null, null, Collections.emptySet(), 0);
    }

    @Override
    protected boolean usuallyRequiresSurvivalMode() {
        return false;
    }

    @Override
    public boolean onPlayerStatisticUpdatedEvent(PlayerStatisticUpdatedEvent event, QuestState state) {
        if (!getStatisticKeys().contains(event.getStatistic())) {
            return false;
        }

        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if (!this.fulfillsProgressConditions(player, state.getPlayerData())) {
            return false;
        }

        int previous = event.hasPreviousValueAllTime() ? event.getPreviousValueAllTime() : 0;
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(event.getValueAllTime() - previous);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(player);
        }
        return true;
    }

    @Override
    protected List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = (state == null) ? Status.NOTGIVENTO : state.getStatus();

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            result.add(baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "))
                    .append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD)).color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "));
        }

        int current = (state == null) ? 0 : state.getAmount();

        result.add(prefix.append(getStatisticsMessage()).append(Component.text(": "))
                .append(Component.text(String.valueOf(current)).color(status.color))
                .append(Component.text(" / " + getAmount())).color(NamedTextColor.DARK_AQUA));

        return result;
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        List<String> keyList = yc.getStringList("statisticKeys");
        this.statisticKeys.clear();
        for (String s : keyList) {
            StatisticKey key = CubeQuest.getInstance().getCubesideStatistics().getStatisticKey(s, false);
            if (key == null) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "StatisticKey with name \"" + s
                        + "\" was missing for quest " + toString() + "! Now removed from the quest.");
                continue;
            }

            this.statisticKeys.add(key);
        }

        this.overwrittenStatisticsMessage = getComponentOrConvert(yc, "overwrittenStatisticsString");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("statisticKeys", this.statisticKeys.stream().map(StatisticKey::getName).collect(Collectors.toList()));
        yc.set("overwrittenStatisticsString", this.overwrittenStatisticsMessage);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && !this.statisticKeys.isEmpty();
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        Component statsLine = Component.text("Erlaubte Statistiken: ", NamedTextColor.DARK_AQUA);

        if (this.statisticKeys.isEmpty()) {
            statsLine = statsLine.append(Component.text("Keine", NamedTextColor.RED));
        } else {
            List<StatisticKey> keyList = new ArrayList<>(this.statisticKeys);
            keyList.sort((e1, e2) -> e1.getName().compareTo(e2.getName()));

            for (int i = 0; i < keyList.size(); i++) {
                statsLine = statsLine.append(Component.text(keyList.get(i).getName(), NamedTextColor.GREEN));
                if (i + 1 < keyList.size()) {
                    statsLine = statsLine.append(Component.text(", ", NamedTextColor.GREEN));
                }
            }
        }

        result.add(suggest(statsLine, AddOrRemoveStatisticCommand.FULL_ADD_COMMAND));

        TextColor statusColor =
                (this.overwrittenStatisticsMessage == null) ? NamedTextColor.GOLD : NamedTextColor.GREEN;
        String statusText = (this.overwrittenStatisticsMessage == null) ? "(automatisch)" : "(gesetzt)";

        Component msgLine = Component.text("Statistiken-Beschreibung: ", NamedTextColor.DARK_AQUA)
                .append(getStatisticsMessage().colorIfAbsent(NamedTextColor.GREEN)).append(Component.text(" "))
                .append(Component.text(statusText, statusColor)).color(NamedTextColor.DARK_AQUA);

        result.add(suggest(msgLine, SetOverwrittenNameForSthCommand.SpecificSth.STATISTIC.fullSetCommand));

        result.add(Component.empty());
        return result;
    }

    public Set<StatisticKey> getStatisticKeys() {
        return Collections.unmodifiableSet(this.statisticKeys);
    }

    public boolean addStatistic(StatisticKey key) {
        if (this.statisticKeys.add(key)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeStatistic(StatisticKey key) {
        if (this.statisticKeys.remove(key)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearStatistics() {
        this.statisticKeys.clear();
        updateIfReal();
    }

    public Component getStatisticsMessage() {
        if (this.overwrittenStatisticsMessage != null) {
            return this.overwrittenStatisticsMessage;
        }

        return Component.text(StringUtil.replaceLast(
                getStatisticKeys().stream().map(StatisticKey::getDisplayName).collect(Collectors.joining(", ")), ", ",
                " und/oder ") + " erhöht").color(NamedTextColor.DARK_AQUA);
    }

    public void setStatisticsMessage(Component message) {
        this.overwrittenStatisticsMessage = message;
        updateIfReal();
    }

}
