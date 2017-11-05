package de.iani.cubequest.generation;

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

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.quests.BlockPlaceQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import net.md_5.bungee.api.chat.BaseComponent;

public class BlockPlaceQuestSpecification extends QuestSpecification {

    public static class BlockPlaceQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static BlockPlaceQuestPossibilitiesSpecification instance;

        private Set<MaterialCombination> materialCombinations;

        public static BlockPlaceQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new BlockPlaceQuestPossibilitiesSpecification();
            }
            return instance;
        }

        public static BlockPlaceQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new BlockPlaceQuestPossibilitiesSpecification(serialized);
            return instance;
        }

        private BlockPlaceQuestPossibilitiesSpecification() {
            this.materialCombinations = new HashSet<>();
        }

        @SuppressWarnings("unchecked")
        private BlockPlaceQuestPossibilitiesSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
            try {
                materialCombinations = serialized == null || !serialized.containsKey("materialCombinations")? new HashSet<>() : new HashSet<>((List<MaterialCombination>) serialized.get("materialCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }

        public Set<MaterialCombination> getMaterialCombinations() {
            return Collections.unmodifiableSet(materialCombinations);
        }

        public boolean addMaterialCombination(MaterialCombination mc) {
            return materialCombinations.add(mc);
        }

        public boolean removeMaterialCombination(MaterialCombination mc) {
            return materialCombinations.remove(mc);
        }

        public void clearMaterialCombinations() {
            materialCombinations.clear();
        }

        public int getWeighting() {
            return isLegal()? (int) materialCombinations.stream().filter(c -> c.isLegal()).count() : 0;
        }

        public boolean isLegal() {
            return materialCombinations.stream().anyMatch(c -> c.isLegal());
        }

        public List<BaseComponent[]> getSpecificationInfo() {
            List<BaseComponent[]> result = new ArrayList<>();
            result.add(ChatAndTextUtil.headline2("Block-Platzier-Quest-Materialkombinationen:"));
            List<MaterialCombination> combinations = new ArrayList<>(materialCombinations);
            combinations.sort(MaterialCombination.COMPARATOR);
            for (MaterialCombination comb: combinations) {
                result.add(comb.getSpecificationInfo());
            }

            return result;
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();

            result.put("materialCombinations", new ArrayList<>(materialCombinations));

            return result;
        }

    }

    private MaterialCombination preparedMaterials;
    private int preparedAmount;

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble()*0.9);

        List<MaterialCombination> mCombs = new ArrayList<>(DeliveryQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        MaterialCombination materialCombination = Util.randomElement(mCombs, ran);

        preparedAmount = (int) Math.ceil(gotoDifficulty / QuestGenerator.getInstance().getValue(
                materialCombination.getContent().stream().min((m1, m2) -> {
                    return Double.compare(QuestGenerator.getInstance().getValue(m1), QuestGenerator.getInstance().getValue(m2));
                }).get()));

        return gotoDifficulty;
    }

    @Override
    public void clearGeneratedQuest() {
        preparedMaterials = null;
        preparedAmount = 0;
    }

    @Override
    public BlockPlaceQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated BlockPlaceQuest!", e);
            return null;
        }

        String giveMessage = "Platziere " + buildBlockPlaceString(preparedMaterials.getContent(), preparedAmount) + ".";

        BlockPlaceQuest result = new BlockPlaceQuest(questId, questName, null, giveMessage, null, successReward, preparedMaterials.getContent(), preparedAmount);
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        clearGeneratedQuest();
        return result;
    }

    public String buildBlockPlaceString(Collection<Material> types, int amount) {
        String result = amount + " ";

        for (Material material: types) {
            result += ItemStackUtil.toNiceString(material) + "-";
            result += ", ";
        }

        result = ChatAndTextUtil.replaceLast(result, "-", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");

        result += "blöcke";

        return result;
    }

    @Override
    public int compareTo(QuestSpecification other) {
        return super.compare(other);
    }

    @Override
    public boolean isLegal() {
        return DeliveryQuestPossibilitiesSpecification.getInstance().isLegal();
    }

    /**
     * Throws UnsupportedOperationException! Not actually serializable!
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
