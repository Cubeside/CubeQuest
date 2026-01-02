package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class DeliveryQuestSpecification extends QuestSpecification {

    public static class DeliveryQuestPossibilitiesSpecification implements ConfigurationSerializable {

        private static DeliveryQuestPossibilitiesSpecification instance;

        private Set<DeliveryReceiverSpecification> targets;
        private Set<MaterialCombination> materialCombinations;

        public static DeliveryQuestPossibilitiesSpecification getInstance() {
            if (instance == null) {
                instance = new DeliveryQuestPossibilitiesSpecification();
            }
            return instance;
        }

        static void resetInstance() {
            instance = null;
        }

        public static DeliveryQuestPossibilitiesSpecification deserialize(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            if (instance != null) {
                if (instance.serialize().equals(serialized)) {
                    return instance;
                } else {
                    throw new IllegalStateException("tried to initialize a second object of singleton");
                }
            }
            instance = new DeliveryQuestPossibilitiesSpecification(serialized);
            return instance;
        }

        private DeliveryQuestPossibilitiesSpecification() {
            this.targets = new TreeSet<>(DeliveryReceiverSpecification.CASE_INSENSITIVE_NAME_COMPARATOR);
            this.materialCombinations = new HashSet<>();
        }

        @SuppressWarnings("unchecked")
        private DeliveryQuestPossibilitiesSpecification(Map<String, Object> serialized)
                throws InvalidConfigurationException {
            this();
            try {
                if (serialized != null && serialized.containsKey("targets")) {
                    for (DeliveryReceiverSpecification spec : (List<DeliveryReceiverSpecification>) serialized
                            .get("targets")) {
                        if (spec.getInteractor() == null) {
                            CubeQuest.getInstance().getLogger().log(Level.WARNING,
                                    "DeliveryReciever \"" + spec.getName()
                                            + "\" has no interactor, will be removed (if generator is saved).");
                        } else if (!spec.getInteractor().isLegal()) {
                            CubeQuest.getInstance().getLogger().log(Level.WARNING, "DeliveryReciever interactor for \""
                                    + spec.getName() + "\" is illegal, will be removed (if generator is saved).");
                        } else {
                            this.targets.add(spec);
                        }
                    }
                }
                this.materialCombinations =
                        serialized == null || !serialized.containsKey("materialCombinations") ? new HashSet<>()
                                : new HashSet<>((List<MaterialCombination>) serialized.get("materialCombinations"));
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
                CubeQuest.getInstance().removeProtecting(target);
                QuestGenerator.getInstance().saveConfig();
                return true;
            }
            return false;
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
                    ? (int) this.targets.stream().filter(t -> t.isLegal()).count()
                            + (int) this.materialCombinations.stream().filter(c -> c.isLegal()).count()
                    : 0;
        }

        public boolean isLegal() {
            return this.targets.stream().anyMatch(t -> t.isLegal())
                    && this.materialCombinations.stream().anyMatch(c -> c.isLegal());
        }

        public List<Component> getReceiverSpecificationInfo() {
            List<Component> result = new ArrayList<>();

            List<DeliveryReceiverSpecification> targetList = new ArrayList<>(this.targets);
            targetList.sort(DeliveryReceiverSpecification.CASE_INSENSITIVE_NAME_COMPARATOR);
            for (DeliveryReceiverSpecification target : this.targets) {
                result.add(target.getSpecificationInfo());
            }

            return result;
        }

        public List<Component> getContentSpecificationInfo() {
            List<Component> result = new ArrayList<>();

            List<MaterialCombination> combinations = new ArrayList<>(this.materialCombinations);
            combinations.sort(MaterialCombination.COMPARATOR);
            for (MaterialCombination comb : combinations) {
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
            implements InteractorProtecting, ConfigurationSerializable, Comparable<DeliveryReceiverSpecification> {

        public static final Comparator<DeliveryReceiverSpecification> INTERACTOR_IDENTIFIER_COMPARATOR =
                (o1, o2) -> (o1.compareTo(o2));
        public static final Comparator<DeliveryReceiverSpecification> CASE_INSENSITIVE_NAME_COMPARATOR = Comparator
                .comparing(DeliveryReceiverSpecification::getName,
                        ComponentUtilAdventure.TEXT_ONLY_CASE_INSENSITIVE_ORDER)
                .thenComparing(Comparator.naturalOrder());

        private Interactor interactor;
        private Component name;

        public DeliveryReceiverSpecification() {}

        public DeliveryReceiverSpecification(Map<String, Object> serialized) {
            this();

            this.interactor = (Interactor) serialized.get("interactor");
            if (serialized.get("name") instanceof String s) {
                this.name = ComponentUtilAdventure.getLegacyComponentSerializer().deserialize(s);
            } else {
                this.name = (Component) serialized.get("name");
            }

            CubeQuest.getInstance().addProtecting(this);
        }

        @Override
        public Interactor getInteractor() {
            return this.interactor;
        }

        public void setInteractor(Interactor interactor) {
            CubeQuest.getInstance().removeProtecting(this);
            this.interactor = interactor;
            CubeQuest.getInstance().addProtecting(this);
            interactor.getName();
            interactor.getLocation();
        }

        public Component getName() {
            return this.name;
        }

        public void setName(Component name) {
            this.name = name;
        }

        public boolean isLegal() {
            return this.name != null && getInteractor() != null;
        }

        @Override
        public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event) {
            if (event.getInteractor().equals(this.interactor)) {
                event.setCancelled(true);
                return true;
            }

            return false;
        }

        @Override
        public void onCacheChanged() {
            // nothing
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

        public Component getSpecificationInfo() {
            return Component.textOfChildren(Component.text("Name: ").color(NamedTextColor.DARK_AQUA), this.name,
                    Component.text("Interactor: ").color(NamedTextColor.DARK_AQUA),
                    ChatAndTextUtil.getInteractorInfo(getInteractor())).color(NamedTextColor.GREEN);
        }

        @Override
        public Component getProtectingInfo() {
            return getSpecificationInfo();
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("interactor", this.interactor);
            result.put("name", this.name);
            return result;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.interactor);
        }

    }

    private DeliveryReceiverSpecification preparedReceiver;
    private ItemStack[] preparedDelivery;

    private MaterialCombination usedMaterialCombination;

    public DeliveryQuestSpecification() {
        super();
    }

    @SuppressWarnings("unchecked")
    public DeliveryQuestSpecification(Map<String, Object> serialized) {
        this.preparedReceiver = (DeliveryReceiverSpecification) serialized.get("preparedReceiver");
        this.preparedDelivery = ((List<ItemStack>) serialized.get("preparedDelivery")).toArray(new ItemStack[0]);
        this.usedMaterialCombination =
                (MaterialCombination) serialized.getOrDefault("usedMaterialCombination", new MaterialCombination(
                        Arrays.stream(this.preparedDelivery).map(ItemStack::getType).collect(Collectors.toList())));
    }

    @Override
    public double generateQuest(Random ran) {
        double gotoDifficulty = 0.1 + (ran.nextDouble() * 0.9);

        List<DeliveryReceiverSpecification> rSpecs =
                new ArrayList<>(DeliveryQuestPossibilitiesSpecification.instance.targets);
        rSpecs.removeIf(s -> !s.isLegal());
        rSpecs.sort(DeliveryReceiverSpecification.INTERACTOR_IDENTIFIER_COMPARATOR);
        Collections.shuffle(rSpecs, ran);
        this.preparedReceiver = Util.randomElement(rSpecs, ran);

        List<MaterialCombination> mCombs =
                new ArrayList<>(DeliveryQuestPossibilitiesSpecification.instance.materialCombinations);
        mCombs.removeIf(c -> !c.isLegal());
        mCombs.sort(MaterialCombination.COMPARATOR);
        this.usedMaterialCombination = Util.randomElement(mCombs, ran);
        List<Material> materials = new ArrayList<>(this.usedMaterialCombination.getContent());
        int maxMaterials = 3 + ran.nextInt(6);
        if (materials.size() > maxMaterials) {
            Collections.shuffle(materials, ran);
            materials.subList(maxMaterials, materials.size()).clear();
        }

        this.preparedDelivery = new ItemStack[0];

        double todoDifficulty = gotoDifficulty;
        while (todoDifficulty > 0 && this.preparedDelivery.length < 27) {
            Material type = Util.randomElement(materials, ran);
            double diffCost = QuestGenerator.getInstance().getValue(MaterialValueOption.DELIVER, type);

            int count;
            if (todoDifficulty >= type.getMaxStackSize() * diffCost) {
                count = type.getMaxStackSize();
            } else if (ran.nextDouble() < this.preparedDelivery.length * 0.05) {
                count = (int) Math.ceil(todoDifficulty / diffCost);
            } else {
                count = (int) Math.floor((ran.nextDouble() * 0.3 + 0.3) * todoDifficulty / diffCost);
            }

            count = Math.max(count, 1);
            this.preparedDelivery = ItemStackUtil.addItem(new ItemStack(type, count), this.preparedDelivery);
            todoDifficulty -= count * diffCost;
        }

        return gotoDifficulty - todoDifficulty;
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
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated DeliveryQuest!", e);
            return null;
        }

        Component giveMessage = Component
                .textOfChildren(Component.text("Liefere "),
                        ItemStacks.toComponent(this.preparedDelivery, Style.style(NamedTextColor.GOLD)),
                        Component.text(" an "), this.preparedReceiver.name, Component.text("."))
                .color(NamedTextColor.GOLD);

        DeliveryQuest result = new DeliveryQuest(questId, questName, null, this.preparedReceiver.getInteractor(),
                this.preparedDelivery);
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(giveMessage);
        result.addGiveAction(new ChatMessageAction(giveMessage));
        result.addSuccessAction(new RewardAction(successReward));
        result.setInteractorName(this.preparedReceiver.getName());
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);

        return result;
    }

    public DeliveryReceiverSpecification getPreparedReceiver() {
        return this.preparedReceiver;
    }

    public ItemStack[] getPreparedDelivery() {
        return ItemStacks.deepCopy(this.preparedDelivery);
    }

    public MaterialCombination getUsedMaterialCombination() {
        return this.usedMaterialCombination;
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        DeliveryQuestSpecification dqs = (DeliveryQuestSpecification) other;
        result = this.preparedReceiver.compareTo(dqs.preparedReceiver);
        if (result != 0) {
            return result;
        }

        return ItemStackUtil.ITEMSTACK_ARRAY_COMPARATOR.compare(this.preparedDelivery, dqs.preparedDelivery);
    }

    @Override
    public boolean isLegal() {
        return DeliveryQuestPossibilitiesSpecification.getInstance().isLegal();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("preparedReceiver", this.preparedReceiver);
        result.put("preparedDelivery", new ArrayList<>(Arrays.asList(this.preparedDelivery)));
        result.put("usedMaterialCombination", this.usedMaterialCombination);
        return result;
    }

    @Override
    public Component getSpecificationInfo() {
        return Component.empty();
    }

}
