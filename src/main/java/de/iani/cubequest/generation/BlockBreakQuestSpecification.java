package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.quests.BlockBreakQuest;
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

public class BlockBreakQuestSpecification extends QuestSpecification {
    
    public static class BlockBreakQuestPossibilitiesSpecification
            implements ConfigurationSerializable {
        
        private static BlockBreakQuestPossibilitiesSpecification instance;
        
        private Set<MaterialCombination> materialCombinations;
        
        public static BlockBreakQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new BlockBreakQuestPossibilitiesSpecification();
            }
            return instance;
        }
        
        public static BlockBreakQuestPossibilitiesSpecification deserialize(
                Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException(
                            "tried to initialize a second object of singleton");
                }
            }
            instance = new BlockBreakQuestPossibilitiesSpecification(serialized);
            return instance;
        }
        
        private BlockBreakQuestPossibilitiesSpecification() {
            this.materialCombinations = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private BlockBreakQuestPossibilitiesSpecification(Map<String, Object> serialized)
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
            result.add(ChatAndTextUtil.headline2("Block-Abbau-Quest-Materialkombinationen:"));
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
    
    private MaterialCombination preparedMaterials;
    private int preparedAmount;
    
    public BlockBreakQuestSpecification() {
        super();
    }
    
    public BlockBreakQuestSpecification(Map<String, Object> serialized) {
        this.preparedMaterials = (MaterialCombination) serialized.get("preparedMaterials");
        this.preparedAmount = (Integer) serialized.get("preparedAmount");
    }
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<MaterialCombination> mCombs = new ArrayList<>(
                BlockBreakQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        this.preparedMaterials = Util.randomElement(mCombs, ran);
        
        this.preparedAmount = (int) Math.ceil(
                gotoDifficulty / QuestGenerator.getInstance().getValue(MaterialValueOption.BREAK,
                        this.preparedMaterials.getContent().stream().min((m1, m2) -> {
                            return Double.compare(
                                    QuestGenerator.getInstance().getValue(MaterialValueOption.BREAK,
                                            m1),
                                    QuestGenerator.getInstance().getValue(MaterialValueOption.BREAK,
                                            m2));
                        }).get()));
        
        return gotoDifficulty;
    }
    
    @Override
    public void clearGeneratedQuest() {
        this.preparedMaterials = null;
        this.preparedAmount = 0;
    }
    
    @Override
    public BlockBreakQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated BlockBreakQuest!", e);
            return null;
        }
        
        String giveMessage = ChatColor.GOLD + "Baue "
                + buildBlockBreakString(this.preparedMaterials.getContent(), this.preparedAmount)
                + " ab.";
        
        BlockBreakQuest result = new BlockBreakQuest(questId, questName, null, giveMessage, null,
                successReward, this.preparedMaterials.getContent(), this.preparedAmount);
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        return result;
    }
    
    public String buildBlockBreakString(Collection<Material> types, int amount) {
        return amount + " " + ChatAndTextUtil.multipleBlockString(types);
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        
        BlockBreakQuestSpecification bbqs = (BlockBreakQuestSpecification) other;
        result = this.preparedMaterials.compareTo(bbqs.preparedMaterials);
        if (result != 0) {
            return result;
        }
        
        return this.preparedAmount - bbqs.preparedAmount;
    }
    
    @Override
    public boolean isLegal() {
        return BlockBreakQuestPossibilitiesSpecification.getInstance().isLegal();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("preparedMaterials", this.preparedMaterials);
        result.put("preparedAmount", this.preparedAmount);
        return result;
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
}
