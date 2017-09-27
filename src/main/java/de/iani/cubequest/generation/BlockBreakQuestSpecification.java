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

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.quests.BlockBreakQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;

public class BlockBreakQuestSpecification extends QuestSpecification {

    public static class BlockBreakQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static BlockBreakQuestPossibilitiesSpecification instance;

        private Set<MaterialCombination> materialCombinations;

        public static BlockBreakQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new BlockBreakQuestPossibilitiesSpecification();
            }
            return instance;
        }

        public static BlockBreakQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new BlockBreakQuestPossibilitiesSpecification(serialized);
            return instance;
        }

        private BlockBreakQuestPossibilitiesSpecification() {
            Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

            this.materialCombinations = new HashSet<MaterialCombination>();
        }

        @SuppressWarnings("unchecked")
        private BlockBreakQuestPossibilitiesSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
            try {
                materialCombinations = new HashSet<MaterialCombination>((List<MaterialCombination>) serialized.get("materialCombinations"));
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

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("materialCombinations", new ArrayList<MaterialCombination>(materialCombinations));

            return result;
        }

    }

    private MaterialCombination preparedMaterials;
    private int preparedAmount;

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble()*0.9);

        List<MaterialCombination> mCombs = new ArrayList<MaterialCombination>(DeliveryQuestPossibilitiesSpecification.getInstance().getMaterialCombinations());
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
    public BlockBreakQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated BlockBreakQuest!", e);
            return null;
        }

        String giveMessage = "Baue " + buildBlockBreakString(preparedMaterials.getContent(), preparedAmount) + " ab.";

        BlockBreakQuest result = new BlockBreakQuest(questId, questName, null, giveMessage, null, successReward, preparedMaterials.getContent(), preparedAmount);
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        clearGeneratedQuest();
        return result;
    }

    public String buildBlockBreakString(Collection<Material> types, int amount) {
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

}
