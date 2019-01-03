package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.QuestGenerator.EntityValueOption;
import de.iani.cubequest.quests.KillEntitiesQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class KillEntitiesQuestSpecification extends AmountAndEntityTypesQuestSpecification {
    
    public static class KillEntitiesQuestPossibilitiesSpecification
            implements ConfigurationSerializable {
        
        private static KillEntitiesQuestPossibilitiesSpecification instance;
        
        private Set<EntityTypeCombination> entityTypeCombinations;
        
        public static KillEntitiesQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new KillEntitiesQuestPossibilitiesSpecification();
            }
            return instance;
        }
        
        static void resetInstance() {
            instance = null;
        }
        
        public static KillEntitiesQuestPossibilitiesSpecification deserialize(
                Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException(
                            "tried to initialize a second object of singleton");
                }
            }
            instance = new KillEntitiesQuestPossibilitiesSpecification(serialized);
            return instance;
        }
        
        private KillEntitiesQuestPossibilitiesSpecification() {
            this.entityTypeCombinations = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private KillEntitiesQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            try {
                this.entityTypeCombinations =
                        serialized == null || !serialized.containsKey("entityTypeCombinations")
                                ? new HashSet<>()
                                : new HashSet<>((List<EntityTypeCombination>) serialized
                                        .get("entityTypeCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }
        
        public Set<EntityTypeCombination> getEntityTypeCombinations() {
            return Collections.unmodifiableSet(this.entityTypeCombinations);
        }
        
        public boolean adEntityTypeCombination(EntityTypeCombination mc) {
            if (this.entityTypeCombinations.add(mc)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public boolean removeEntityTypeCombination(EntityTypeCombination mc) {
            if (this.entityTypeCombinations.remove(mc)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public void clearEntityTypeCombinations() {
            this.entityTypeCombinations.clear();
            QuestGenerator.getInstance().saveConfig();
        }
        
        public int getWeighting() {
            return isLegal()
                    ? (int) this.entityTypeCombinations.stream().filter(c -> c.isLegal()).count()
                    : 0;
        }
        
        public boolean isLegal() {
            return this.entityTypeCombinations.stream().anyMatch(c -> c.isLegal());
        }
        
        public List<BaseComponent[]> getSpecificationInfo() {
            List<BaseComponent[]> result = new ArrayList<>();
            
            List<EntityTypeCombination> combinations = new ArrayList<>(this.entityTypeCombinations);
            combinations.sort(EntityTypeCombination.COMPARATOR);
            for (EntityTypeCombination comb : combinations) {
                result.add(comb.getSpecificationInfo());
            }
            
            return result;
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            
            result.put("entityTypeCombinations", new ArrayList<>(this.entityTypeCombinations));
            
            return result;
        }
        
    }
    
    public KillEntitiesQuestSpecification() {
        super();
    }
    
    public KillEntitiesQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
    }
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<EntityTypeCombination> eCombs =
                new ArrayList<>(KillEntitiesQuestPossibilitiesSpecification.getInstance()
                        .getEntityTypeCombinations());
        eCombs.removeIf(c -> !c.isLegal());
        eCombs.sort(EntityTypeCombination.COMPARATOR);
        Collections.shuffle(eCombs, ran);
        setEntityTypes(new EntityTypeCombination(
                Util.getGuassianSizedSubSet(Util.randomElement(eCombs, ran).getContent(), ran)));
        
        setAmount((int) Math
                .ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                        getEntityTypes().getContent().stream().min((e1, e2) -> {
                            return Double.compare(
                                    QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                                            e1),
                                    QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                                            e2));
                        }).get())));
        
        return gotoDifficulty;
    }
    
    @Override
    public KillEntitiesQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated BlockPlaceQuest!", e);
            return null;
        }
        
        String giveMessage = ChatColor.GOLD + "Töte "
                + buildKillEntitiesString(getEntityTypes().getContent(), getAmount()) + ".";
        
        KillEntitiesQuest result = new KillEntitiesQuest(questId, questName, null, giveMessage,
                null, successReward, getEntityTypes().getContent(), getAmount());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        return result;
    }
    
    public String buildKillEntitiesString(Collection<EntityType> types, int amount) {
        return amount + " " + ChatAndTextUtil.multipleMobsString(types);
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        
        KillEntitiesQuestSpecification keqs = (KillEntitiesQuestSpecification) other;
        result = getEntityTypes().compareTo(keqs.getEntityTypes());
        if (result != 0) {
            return result;
        }
        
        return getAmount() - keqs.getAmount();
    }
    
    @Override
    public boolean isLegal() {
        return KillEntitiesQuestPossibilitiesSpecification.getInstance().isLegal();
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
}
