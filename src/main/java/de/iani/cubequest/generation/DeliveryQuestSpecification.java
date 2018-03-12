package de.iani.cubequest.generation;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class DeliveryQuestSpecification extends QuestSpecification {
    
    public static class DeliveryQuestPossibilitiesSpecification
            implements ConfigurationSerializable {
        
        private static DeliveryQuestPossibilitiesSpecification instance;
        
        private Set<DeliveryReceiverSpecification> targets;
        private Set<MaterialCombination> materialCombinations;
        
        public static DeliveryQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new DeliveryQuestPossibilitiesSpecification();
            }
            return instance;
        }
        
        public static DeliveryQuestPossibilitiesSpecification deserialize(
                Map<String, Object> serialized) throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException(
                            "tried to initialize a second object of singleton");
                }
            }
            instance = new DeliveryQuestPossibilitiesSpecification(serialized);
            return instance;
        }
        
        private DeliveryQuestPossibilitiesSpecification() {
            Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
            
            this.targets =
                    new TreeSet<>(DeliveryReceiverSpecification.CASE_INSENSITIVE_NAME_COMPARATOR);
            this.materialCombinations = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked")
        private DeliveryQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            this();
            try {
                if (serialized != null && serialized.containsKey("targets")) {
                    this.targets.addAll(
                            (List<DeliveryReceiverSpecification>) serialized.get("targets"));
                }
                this.materialCombinations =
                        serialized == null || !serialized.containsKey("materialCombinations")
                                ? new HashSet<>()
                                : new HashSet<>((List<MaterialCombination>) serialized
                                        .get("materialCombinations"));
            } catch (Exception e) {
                throw new InvalidConfigurationException(e);
            }
        }
        
        public Set<DeliveryReceiverSpecification> getTargets() {
            return Collections.unmodifiableSet(this.targets);
        }
        
        public boolean addTarget(DeliveryReceiverSpecification target) {
            if (this.targets.add(target)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public boolean removeTarget(DeliveryReceiverSpecification target) {
            if (this.targets.remove(target)) {
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
        }
        
        public void clearTargets() {
            this.targets.clear();
            QuestGenerator.getInstance().saveConfig();
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
            return isLegal() ? Math.max(
                    (int) this.targets.stream().filter(t -> t.isLegal()).count(),
                    (int) this.materialCombinations.stream().filter(c -> c.isLegal()).count()) : 0;
        }
        
        public boolean isLegal() {
            return this.targets.stream().anyMatch(t -> t.isLegal())
                    && this.materialCombinations.stream().anyMatch(c -> c.isLegal());
        }
        
        public List<BaseComponent[]> getSpecificationInfo() {
            List<BaseComponent[]> result = new ArrayList<>();
            
            result.add(ChatAndTextUtil.headline2("Liefer-Quest-Ziele:"));
            List<DeliveryReceiverSpecification> targetList = new ArrayList<>(this.targets);
            targetList.sort(DeliveryReceiverSpecification.CASE_INSENSITIVE_NAME_COMPARATOR);
            for (DeliveryReceiverSpecification target: this.targets) {
                result.add(target.getSpecificationInfo());
            }
            
            result.add(new ComponentBuilder("").create());
            result.add(ChatAndTextUtil.headline2("Liefer-Quest-Materialkombinationen:"));
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
            
            result.put("targets", new ArrayList<>(this.targets));
            result.put("materialCombinations", new ArrayList<>(this.materialCombinations));
            
            return result;
        }
        
    }
    
    public static class DeliveryReceiverSpecification
            implements ConfigurationSerializable, Comparable<DeliveryReceiverSpecification> {
        
        public static final Comparator<DeliveryReceiverSpecification> INTERACTOR_IDENTIFIER_COMPARATOR =
                (o1, o2) -> (o1.compareTo(o2));
        public static final Comparator<DeliveryReceiverSpecification> CASE_INSENSITIVE_NAME_COMPARATOR =
                (o1, o2) -> {
                    int result = o1.getName().compareToIgnoreCase(o2.getName());
                    return result != 0 ? result : o1.compareTo(o2);
                };
        
        private Interactor interactor;
        private String name;
        
        public DeliveryReceiverSpecification() {
            // if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            // throw new IllegalStateException("This server doesn't have the CitizensPlugin!");
            // }
        }
        
        public DeliveryReceiverSpecification(Map<String, Object> serialized) {
            this();
            
            this.interactor = (Interactor) serialized.get("interactor");
            this.name = (String) serialized.get("name");
            
            if (this.interactor != null && getInteractor() == null) {
                throw new IllegalArgumentException(
                        "Interactor with name " + this.name + " not found.");
            }
        }
        
        public Interactor getInteractor() {
            return this.interactor;
        }
        
        public void setInteractor(Interactor interactor) {
            this.interactor = interactor;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public boolean isLegal() {
            return this.name != null && getInteractor() != null;
        }
        
        @Override
        public int compareTo(DeliveryReceiverSpecification o) {
            return Interactor.COMPARATOR.compare(getInteractor(), o.getInteractor());
        }
        
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DeliveryReceiverSpecification)) {
                return false;
            }
            DeliveryReceiverSpecification o = (DeliveryReceiverSpecification) other;
            return Objects.equals(o.interactor, this.interactor);
        }
        
        public BaseComponent[] getSpecificationInfo() {
            return new ComponentBuilder(ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + this.name
                    + ChatColor.DARK_AQUA + " Interactor: "
                    + ChatAndTextUtil.getInteractorInfoString(getInteractor())).create();
        }
        
        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("interactor", this.interactor);
            result.put("name", this.name);
            return result;
        }
        
    }
    
    private DeliveryReceiverSpecification preparedReceiver;
    private ItemStack[] preparedDelivery;
    
    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);
        
        List<DeliveryReceiverSpecification> rSpecs =
                new ArrayList<>(DeliveryQuestPossibilitiesSpecification.instance.targets);
        rSpecs.removeIf(s -> !s.isLegal());
        rSpecs.sort(DeliveryReceiverSpecification.INTERACTOR_IDENTIFIER_COMPARATOR);
        Collections.shuffle(rSpecs, ran);
        this.preparedReceiver = Util.randomElement(rSpecs, ran);
        
        List<MaterialCombination> mCombs = new ArrayList<>(
                DeliveryQuestPossibilitiesSpecification.instance.materialCombinations);
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        Collections.shuffle(mCombs, ran);
        MaterialCombination materialCombination = Util.randomElement(mCombs, ran);
        List<Material> materials = new ArrayList<>(materialCombination.getContent());
        
        this.preparedDelivery = new ItemStack[0];
        
        double todoDifficulty = gotoDifficulty;
        while (todoDifficulty > 0) {
            Material type = Util.randomElement(materials, ran);
            double diffCost =
                    QuestGenerator.getInstance().getValue(MaterialValueOption.DELIVER, type);
            if (todoDifficulty >= type.getMaxStackSize() * diffCost) {
                this.preparedDelivery = ItemStackUtil.addItem(
                        new ItemStack(type, type.getMaxStackSize()), this.preparedDelivery);
                todoDifficulty -= type.getMaxStackSize() * diffCost;
            } else {
                this.preparedDelivery =
                        ItemStackUtil.addItem(new ItemStack(type, 1), this.preparedDelivery);
                todoDifficulty -= type.getMaxStackSize();
            }
        }
        
        return gotoDifficulty;
    }
    
    @Override
    public void clearGeneratedQuest() {
        this.preparedReceiver = null;
        this.preparedDelivery = null;
    }
    
    @Override
    public DeliveryQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated DeliveryQuest!", e);
            return null;
        }
        
        String giveMessage = CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " Liefere "
                + buildDeliveryString(this.preparedDelivery) + " an " + this.preparedReceiver.name
                + ".";
        
        DeliveryQuest result = new DeliveryQuest(questId, questName, null, giveMessage, null,
                successReward, this.preparedReceiver.interactor, this.preparedDelivery);
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();
        
        clearGeneratedQuest();
        return result;
    }
    
    public String buildDeliveryString(ItemStack[] delivery) {
        EnumMap<Material, Integer> items = new EnumMap<>(Material.class);
        Arrays.stream(delivery).forEach(item -> items.put(item.getType(), item.getAmount()
                + (items.containsKey(item.getType()) ? items.get(item.getType()) : 0)));
        
        String result = "";
        
        for (Material material: items.keySet()) {
            result += items.get(material).intValue() + " ";
            result += ItemStackUtil.toNiceString(material);
            result += ", ";
        }
        
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und ");
        
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
