package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.quests.FishingQuest;
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
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class FishingQuestSpecification extends AmountAndMaterialsQuestSpecification {
    
    public static class FishingQuestPossibilitiesSpecification
            implements ConfigurationSerializable {
        
        private static FishingQuestPossibilitiesSpecification instance;
        
        private Set<MaterialCombination> materialCombinations;
        
        public static FishingQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new FishingQuestPossibilitiesSpecification();
            }
            return instance;
        }
        
        public static FishingQuestPossibilitiesSpecification deserialize(
                Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException(
                            "tried to initialize a second object of singleton");
                }
            }
            instance = new FishingQuestPossibilitiesSpecification(serialized);
            return instance;
        }
        
        private FishingQuestPossibilitiesSpecification() {
            this.materialCombinations = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private FishingQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            try {
                this.materialCombinations =
                        serialized == null || !serialized.containsKey("materialCombinations")
                                ? new HashSet<>()
                                : new HashSet<>((List<MaterialCombination>) serialized
                                        .get("materialCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }
        
        public Set<MaterialCombination> getMaterialCombinations() {
            return Collections.unmodifiableSet(this.materialCombinations);
        }
        
        public boolean addMaterialCombination(MaterialCombination mc) {
            if (this.materialCombinations.add(mc)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public boolean removeMaterialCombination(MaterialCombination mc) {
            if (this.materialCombinations.remove(mc)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public void clearMaterialCombinations() {
            this.materialCombinations.clear();
            QuestGenerator.getInstance().saveConfig();
        }
        
        public int getWeighting() {
            return isLegal()
                    ? (int) this.materialCombinations.stream().filter(c -> c.isLegal()).count()
                    : 0;
        }
        
        public boolean isLegal() {
            return this.materialCombinations.stream().anyMatch(c -> c.isLegal());
        }
        
        public List<BaseComponent[]> getSpecificationInfo() {
            List<BaseComponent[]> result = new ArrayList<>();
            result.add(ChatAndTextUtil.headline2("Angel-Quest-Materialkombinationen:"));
            List<MaterialCombination> combinations = new ArrayList<>(this.materialCombinations);
            combinations.sort(MaterialCombination.COMPARATOR);
            for (MaterialCombination comb: combinations) {
                result.add(comb.getSpecificationInfo());
            }
            
            return result;
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            
            result.put("materialCombinations", new ArrayList<>(this.materialCombinations));
            
            return result;
        }
        
    }
    
    public FishingQuestSpecification() {
        super();
    }
    
    public FishingQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
    }
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<MaterialCombination> mCombs = new ArrayList<>(
                FishingQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        setMaterials(Util.randomElement(mCombs, ran));
        
        setAmount((int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(
                MaterialValueOption.FISH, getMaterials().getContent().stream().min((m1, m2) -> {
                    return Double.compare(
                            QuestGenerator.getInstance().getValue(MaterialValueOption.FISH, m1),
                            QuestGenerator.getInstance().getValue(MaterialValueOption.FISH, m2));
                }).get())));
        
        return gotoDifficulty;
    }
    
    @Override
    public FishingQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated FishingQuest!", e);
            return null;
        }
        
        String giveMessage = ChatColor.GOLD + "Angle "
                + buildFischingString(getMaterials().getContent(), getAmount()) + ".";
        
        FishingQuest result = new FishingQuest(questId, questName, null, giveMessage, null,
                successReward, getMaterials().getContent(), getAmount());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        return result;
    }
    
    public String buildFischingString(Collection<Material> types, int amount) {
        return amount + " " + ChatAndTextUtil.multiplieFishablesString(types);
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        
        FishingQuestSpecification fqs = (FishingQuestSpecification) other;
        result = getMaterials().compareTo(fqs.getMaterials());
        if (result != 0) {
            return result;
        }
        
        return getAmount() - fqs.getAmount();
    }
    
    @Override
    public boolean isLegal() {
        return FishingQuestPossibilitiesSpecification.getInstance().isLegal();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("getMaterials()", getMaterials());
        result.put("getAmount()", getAmount());
        return result;
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
}
