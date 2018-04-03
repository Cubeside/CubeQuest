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

public class KillEntitiesQuestSpecification extends QuestSpecification {
    
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
            
            result.add(ChatAndTextUtil.headline2("Entity-Töten-Kombinationen:"));
            List<EntityTypeCombination> combinations = new ArrayList<>(this.entityTypeCombinations);
            combinations.sort(EntityTypeCombination.COMPARATOR);
            for (EntityTypeCombination comb: combinations) {
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
    
    private EntityTypeCombination preparedEntityTypes;
    private int preparedAmount;
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<EntityTypeCombination> eCombs =
                new ArrayList<>(KillEntitiesQuestPossibilitiesSpecification.getInstance()
                        .getEntityTypeCombinations());
        eCombs.removeIf(c -> !c.isLegal());
        eCombs.sort(EntityTypeCombination.COMPARATOR);
        Collections.shuffle(eCombs, ran);
        this.preparedEntityTypes = Util.randomElement(eCombs, ran);
        
        this.preparedAmount = (int) Math
                .ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                        this.preparedEntityTypes.getContent().stream().min((e1, e2) -> {
                            return Double.compare(
                                    QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                                            e1),
                                    QuestGenerator.getInstance().getValue(EntityValueOption.KILL,
                                            e2));
                        }).get()));
        
        return gotoDifficulty;
    }
    
    @Override
    public void clearGeneratedQuest() {
        this.preparedEntityTypes = null;
        this.preparedAmount = 0;
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
        
        String giveMessage = ChatColor.GOLD + "Töte " + buildKillEntitiesString(
                this.preparedEntityTypes.getContent(), this.preparedAmount) + ".";
        
        KillEntitiesQuest result = new KillEntitiesQuest(questId, questName, null, giveMessage,
                null, successReward, this.preparedEntityTypes.getContent(), this.preparedAmount);
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        clearGeneratedQuest();
        return result;
    }
    
    public String buildKillEntitiesString(Collection<EntityType> types, int amount) {
        return amount + " " + ChatAndTextUtil.multipleMobsString(types);
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        return super.compare(other);
    }
    
    @Override
    public boolean isLegal() {
        return KillEntitiesQuestPossibilitiesSpecification.getInstance().isLegal();
    }
    
    /**
     * Throws UnsupportedOperationException! Not actually serializable!
     * 
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public Map<String, Object> serialize() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
}
