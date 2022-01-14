package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.generation.BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification;
import de.iani.cubequest.quests.IncreaseStatisticQuest;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesideutils.RandomUtil;
import de.iani.cubesideutils.bukkit.serialization.RecordSerialization;
import de.iani.cubesideutils.bukkit.serialization.RecordSerialization.ConfigurationSerializableRecord;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class IncreaseStatisticQuestSpecification extends AmountQuestSpecification {
    
    public static record IncreaseStatisticQuestPossibility(StatisticKey statistic, boolean atMostOnce,
            String textDescription, String progressDescription) implements ConfigurationSerializableRecord {
        
        public static String SERIALIZATION_KEY = "IncreaseStatisticQuestPossibility";
        
        public static IncreaseStatisticQuestPossibility deserialize(Map<String, Object> serialized) {
            serialized.computeIfPresent("statistic",
                    (s, o) -> CubeQuest.getInstance().getCubesideStatistics().getStatisticKey((String) o, false));
            return RecordSerialization.deserialize(IncreaseStatisticQuestPossibility.class, serialized);
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = ConfigurationSerializableRecord.super.serialize();
            result.computeIfPresent("statistic", (s, o) -> ((StatisticKey) o).getName());
            return result;
        }
        
    }
    
    public static class IncreaseStatisticQuestPossibilitiesSpecification implements ConfigurationSerializable {
        
        private static IncreaseStatisticQuestPossibilitiesSpecification instance;
        
        public static final Comparator<IncreaseStatisticQuestPossibility> STATISTICS_COMPARATOR =
                (poss1, poss2) -> String.CASE_INSENSITIVE_ORDER.compare(poss1.statistic().getName(),
                        poss2.statistic().getName());
        
        private Set<IncreaseStatisticQuestPossibility> statistics;
        
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
            this.statistics = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private IncreaseStatisticQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            try {
                this.statistics = new LinkedHashSet<>((Collection<IncreaseStatisticQuestPossibility>) serialized
                        .getOrDefault("statistics", Collections.emptyList()));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }
        
        public Set<IncreaseStatisticQuestPossibility> getStatistics() {
            return Collections.unmodifiableSet(this.statistics);
        }
        
        public boolean addStatistic(IncreaseStatisticQuestPossibility statistic) {
            if (this.statistics.add(statistic)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public boolean removeStatistic(IncreaseStatisticQuestPossibility statistic) {
            if (this.statistics.remove(statistic)) {
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
            return this.statistics.size();
        }
        
        public List<BaseComponent[]> getSpecificationInfo() {
            List<BaseComponent[]> result = new ArrayList<>();
            List<IncreaseStatisticQuestPossibility> statisticList = new ArrayList<>(this.statistics);
            statisticList.sort(STATISTICS_COMPARATOR);
            for (IncreaseStatisticQuestPossibility statistic : statisticList) {
                ComponentBuilder builder = new ComponentBuilder(statistic.statistic().getName()).color(ChatColor.GREEN);
                if (statistic.atMostOnce()) {
                    builder.append(" (max. einmal)");
                }
                builder.append(": ").append(TextComponent.fromLegacyText(statistic.textDescription)).reset();
                builder.append(" | ").reset().append(TextComponent.fromLegacyText(statistic.progressDescription));
                result.add(builder.create());
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
    private String progressDescription;
    private String textDescription;
    
    public IncreaseStatisticQuestSpecification() {
        
    }
    
    public IncreaseStatisticQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
        
        this.statistic = CubeQuest.getInstance().getCubesideStatistics()
                .getStatisticKey((String) serialized.get("statistic"), false);
        this.textDescription = (String) serialized.get("textDescription");
        this.progressDescription = (String) serialized.get("progressDescription");
    }
    
    public StatisticKey getStatistic() {
        return this.statistic;
    }
    
    protected void setStatistic(StatisticKey statistic) {
        this.statistic = statistic;
    }
    
    public String getProgressDescription() {
        return this.progressDescription;
    }
    
    protected void setProgressDescription(String description) {
        this.progressDescription = description;
    }
    
    public String getTextDescription() {
        return this.textDescription;
    }
    
    protected void setTextDescription(String giveMessage) {
        this.textDescription = giveMessage;
    }
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<IncreaseStatisticQuestPossibility> statistics =
                new ArrayList<>(IncreaseStatisticQuestPossibilitiesSpecification.getInstance().getStatistics());
        statistics.sort(IncreaseStatisticQuestPossibilitiesSpecification.STATISTICS_COMPARATOR);
        IncreaseStatisticQuestPossibility statistic = RandomUtil.randomElement(statistics, ran);
        setStatistic(statistic.statistic());
        setTextDescription(statistic.textDescription());
        setProgressDescription(statistic.progressDescription());
        
        setAmount(statistic.atMostOnce() ? 1
                : (int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(statistic.statistic())));
        
        return getAmount() * QuestGenerator.getInstance().getValue(statistic.statistic());
    }
    
    @Override
    public IncreaseStatisticQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated IncreaseStatisticQuest!",
                    e);
            return null;
        }
        
        String giveMessage =
                ChatColor.GOLD + AMOUNT_PATTERN.matcher(getTextDescription()).replaceAll(String.valueOf(getAmount()));
        
        IncreaseStatisticQuest result =
                new IncreaseStatisticQuest(questId, questName, null, Set.of(getStatistic()), getAmount());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        result.addGiveAction(new ChatMessageAction(giveMessage));
        result.setStatisticsString(getProgressDescription());
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
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("statistic", this.statistic.getName());
        result.put("textDescription", this.textDescription);
        result.put("progressDescription", this.progressDescription);
        
        return result;
    }
    
}
