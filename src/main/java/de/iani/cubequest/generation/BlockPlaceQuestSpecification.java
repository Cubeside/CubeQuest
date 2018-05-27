package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.quests.BlockPlaceQuest;
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

public class BlockPlaceQuestSpecification extends AmountAndMaterialsQuestSpecification {
    
    public static class BlockPlaceQuestPossibilitiesSpecification
            implements ConfigurationSerializable {
        
        private static BlockPlaceQuestPossibilitiesSpecification instance;
        
        private Set<MaterialCombination> materialCombinations;
        
        public static BlockPlaceQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new BlockPlaceQuestPossibilitiesSpecification();
            }
            return instance;
        }
        
        public static BlockPlaceQuestPossibilitiesSpecification deserialize(
                Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException(
                            "tried to initialize a second object of singleton");
                }
            }
            instance = new BlockPlaceQuestPossibilitiesSpecification(serialized);
            return instance;
        }
        
        private BlockPlaceQuestPossibilitiesSpecification() {
            this.materialCombinations = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private BlockPlaceQuestPossibilitiesSpecification(Map<String, Object> serialized)
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
            result.add(ChatAndTextUtil.headline2("Block-Platzier-Quest-Materialkombinationen:"));
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
    
    public BlockPlaceQuestSpecification() {
        super();
    }
    
    public BlockPlaceQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
    }
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<MaterialCombination> mCombs = new ArrayList<>(
                BlockPlaceQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        setMaterials(Util.randomElement(mCombs, ran));
        
        setAmount((int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(
                MaterialValueOption.PLACE, getMaterials().getContent().stream().min((m1, m2) -> {
                    return Double.compare(
                            QuestGenerator.getInstance().getValue(MaterialValueOption.PLACE, m1),
                            QuestGenerator.getInstance().getValue(MaterialValueOption.PLACE, m2));
                }).get())));
        
        return gotoDifficulty;
    }
    
    @Override
    public BlockPlaceQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated BlockPlaceQuest!", e);
            return null;
        }
        
        String giveMessage = ChatColor.GOLD + "Platziere "
                + buildBlockPlaceString(getMaterials().getContent(), getAmount()) + ".";
        
        BlockPlaceQuest result = new BlockPlaceQuest(questId, questName, null, giveMessage, null,
                successReward, getMaterials().getContent(), getAmount());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        return result;
    }
    
    public String buildBlockPlaceString(Collection<Material> types, int amount) {
        return amount + " " + ChatAndTextUtil.multipleBlockString(types);
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        
        BlockPlaceQuestSpecification bpqs = (BlockPlaceQuestSpecification) other;
        result = getMaterials().compareTo(bpqs.getMaterials());
        if (result != 0) {
            return result;
        }
        
        return getAmount() - bpqs.getAmount();
    }
    
    @Override
    public boolean isLegal() {
        return BlockPlaceQuestPossibilitiesSpecification.getInstance().isLegal();
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new BaseComponent[0];
    }
    
}
