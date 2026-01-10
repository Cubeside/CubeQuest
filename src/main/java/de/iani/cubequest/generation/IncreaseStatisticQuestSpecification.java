package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.generation.BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification;
import de.iani.cubequest.quests.IncreaseStatisticQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.serialization.RecordSerialization;
import de.iani.cubesideutils.bukkit.serialization.RecordSerialization.ConfigurationSerializableRecord;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class IncreaseStatisticQuestSpecification extends AmountQuestSpecification {

    public static record IncreaseStatisticQuestPossibility(StatisticKey statistic, double weight, boolean atMostOnce,
            Component textDescription, Component progressDescription) implements ConfigurationSerializableRecord {

        public static String SERIALIZATION_KEY = "IncreaseStatisticQuestPossibility";

        public static IncreaseStatisticQuestPossibility deserialize(Map<String, Object> serialized) {
            serialized.computeIfPresent("statistic",
                    (s, o) -> CubeQuest.getInstance().getCubesideStatistics().getStatisticKey((String) o, false));
            serialized.computeIfPresent("textDescription", (k, v) -> ChatAndTextUtil.getComponentOrConvert(v));
            serialized.computeIfPresent("progressDescription", (k, v) -> ChatAndTextUtil.getComponentOrConvert(v));
            return RecordSerialization.deserialize(IncreaseStatisticQuestPossibility.class, serialized);
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = ConfigurationSerializableRecord.super.serialize();
            result.computeIfPresent("statistic", (s, o) -> ((StatisticKey) o).getName());
            result.computeIfPresent("textDescription",
                    (k, v) -> SerializableAdventureComponent.ofOrNull((Component) v));
            result.computeIfPresent("progressDescription",
                    (k, v) -> SerializableAdventureComponent.ofOrNull((Component) v));
            return result;
        }

    }

    public static class IncreaseStatisticQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static IncreaseStatisticQuestPossibilitiesSpecification instance;

        public static final Comparator<IncreaseStatisticQuestPossibility> STATISTICS_COMPARATOR =
                (poss1, poss2) -> String.CASE_INSENSITIVE_ORDER.compare(poss1.statistic().getName(),
                        poss2.statistic().getName());

        private List<IncreaseStatisticQuestPossibility> statistics;

        public static IncreaseStatisticQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new IncreaseStatisticQuestPossibilitiesSpecification();
            }
            return instance;
        }

        static void resetInstance() {
            instance = null;
        }

        public static IncreaseStatisticQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new IncreaseStatisticQuestPossibilitiesSpecification(
                    serialized == null ? Collections.emptyMap() : serialized);
            return instance;
        }

        private IncreaseStatisticQuestPossibilitiesSpecification() {
            this.statistics = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        private IncreaseStatisticQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            try {
                this.statistics = new ArrayList<>((Collection<IncreaseStatisticQuestPossibility>) serialized
                        .getOrDefault("statistics", Collections.emptyList()));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }

        public List<IncreaseStatisticQuestPossibility> getStatistics() {
            return Collections.unmodifiableList(this.statistics);
        }

        public void addStatistic(IncreaseStatisticQuestPossibility statistic) {
            this.statistics.add(statistic);
            QuestGenerator.getInstance().saveConfig();
        }

        public boolean removeStatistic(StatisticKey statisticKey) {
            if (this.statistics.removeIf(possibility -> possibility.statistic() == statisticKey)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }

        public void clearStatistics() {
            this.statistics.clear();
            QuestGenerator.getInstance().saveConfig();
        }

        public int getWeighting() {
            return (int) Math
                    .round(this.statistics.stream().mapToDouble(IncreaseStatisticQuestPossibility::weight).sum());
        }

        public List<Component> getSpecificationInfo() {
            List<Component> result = new ArrayList<>();

            List<IncreaseStatisticQuestPossibility> statisticList = new ArrayList<>(this.statistics);
            statisticList.sort(STATISTICS_COMPARATOR);

            for (IncreaseStatisticQuestPossibility statistic : statisticList) {
                Component c = Component.text(statistic.statistic().getName(), NamedTextColor.GREEN)
                        .append(Component.text(" Gew. " + statistic.weight))
                        .append(statistic.atMostOnce() ? Component.text(" (max. einmal)") : Component.empty())
                        .append(Component.text(": ")).append(statistic.textDescription).append(Component.text(" | "))
                        .append(statistic.progressDescription);

                result.add(c.color(NamedTextColor.DARK_AQUA));
            }

            return result;
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();

            result.put("statistics", new ArrayList<>(this.statistics));

            return result;
        }

        public boolean isLegal() {
            return !getStatistics().isEmpty();
        }

    }

    public static final String AMOUNT_PLACEHOLDER = "##";
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(Pattern.quote(AMOUNT_PLACEHOLDER));

    private StatisticKey statistic;
    private Component progressDescription;
    private Component textDescription;

    public IncreaseStatisticQuestSpecification() {

    }

    public IncreaseStatisticQuestSpecification(Map<String, Object> serialized) {
        super(serialized);

        this.statistic = CubeQuest.getInstance().getCubesideStatistics()
                .getStatisticKey((String) serialized.get("statistic"), false);

        this.textDescription = ChatAndTextUtil.getComponentOrConvert(serialized, "textDescription");
        this.progressDescription = ChatAndTextUtil.getComponentOrConvert(serialized, "progressDescription");
    }

    public StatisticKey getStatistic() {
        return this.statistic;
    }

    protected void setStatistic(StatisticKey statistic) {
        this.statistic = statistic;
    }

    public Component getProgressDescription() {
        return this.progressDescription;
    }

    protected void setProgressDescription(Component description) {
        this.progressDescription = description;
    }

    public Component getTextDescription() {
        return this.textDescription;
    }

    protected void setTextDescription(Component giveMessage) {
        this.textDescription = giveMessage;
    }

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);

        List<IncreaseStatisticQuestPossibility> statistics =
                new ArrayList<>(IncreaseStatisticQuestPossibilitiesSpecification.getInstance().getStatistics());
        statistics.sort(IncreaseStatisticQuestPossibilitiesSpecification.STATISTICS_COMPARATOR);

        double totalWeight = statistics.stream().mapToDouble(IncreaseStatisticQuestPossibility::weight).sum();
        double targetWeight = ran.nextDouble() * totalWeight;
        IncreaseStatisticQuestPossibility statistic = statistics.get(0);

        double sum = 0;
        for (IncreaseStatisticQuestPossibility curr : statistics) {
            sum += curr.weight();
            if (sum >= targetWeight) {
                statistic = curr;
                break;
            }
        }

        setStatistic(statistic.statistic());
        setTextDescription(statistic.textDescription());
        setProgressDescription(statistic.progressDescription());

        setAmount(statistic.atMostOnce() ? 1
                : (int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(statistic.statistic())));

        return getAmount() * QuestGenerator.getInstance().getValue(statistic.statistic());
    }

    @Override
    public IncreaseStatisticQuest createGeneratedQuest(Component questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated IncreaseStatisticQuest!",
                    e);
            return null;
        }

        Component giveMessage =
                ComponentUtilAdventure.replacePattern(getTextDescription(), AMOUNT_PATTERN, Component.text(getAmount()))
                        .colorIfAbsent(NamedTextColor.GOLD);

        IncreaseStatisticQuest result =
                new IncreaseStatisticQuest(questId, questName, null, Set.of(getStatistic()), getAmount());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        result.addGiveAction(new ChatMessageAction(giveMessage));
        result.setStatisticsMessage(getProgressDescription());
        result.addSuccessAction(new RewardAction(successReward));
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);

        return result;
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        IncreaseStatisticQuestSpecification isqs = (IncreaseStatisticQuestSpecification) other;
        result = getStatistic().getName().compareTo(isqs.getStatistic().getName());
        if (result != 0) {
            return result;
        }

        return getAmount() - isqs.getAmount();
    }

    @Override
    public boolean isLegal() {
        return BlockBreakQuestPossibilitiesSpecification.getInstance().isLegal();
    }

    @Override
    public Component getSpecificationInfo() {
        return Component.empty();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("statistic", this.statistic.getName());
        result.put("textDescription", SerializableAdventureComponent.ofOrNull(this.textDescription));
        result.put("progressDescription", SerializableAdventureComponent.ofOrNull(this.progressDescription));

        return result;
    }

}
