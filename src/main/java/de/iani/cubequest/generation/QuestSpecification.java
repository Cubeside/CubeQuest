package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import java.util.Comparator;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class QuestSpecification implements Comparable<QuestSpecification>, ConfigurationSerializable {

    public static final Comparator<QuestSpecification> COMPARATOR = (q1, q2) -> q1.compareTo(q2);

    public static final Comparator<QuestSpecification> SIMILAR_SPECIFICATIONS_COMPARATOR = (q1, q2) -> {
        if (q1 instanceof AmountAndMaterialsQuestSpecification s1
                && q2 instanceof AmountAndMaterialsQuestSpecification s2) {
            return s1.getUsedMaterialCombination().compareTo(s2.getUsedMaterialCombination());
        } else if (q1 instanceof AmountAndEntityTypesQuestSpecification s1
                && q2 instanceof AmountAndEntityTypesQuestSpecification s2) {
            return s1.getUsedEntityTypeCombination().compareTo(s2.getUsedEntityTypeCombination());
        } else if (q1 instanceof IncreaseStatisticQuestSpecification i1
                && q2 instanceof IncreaseStatisticQuestSpecification i2) {
            return i1.getStatistic().getName().compareTo(i2.getStatistic().getName());
        } else if (q1 instanceof DeliveryQuestSpecification d1 && q2 instanceof DeliveryQuestSpecification d2) {
            int result = d1.getPreparedReceiver().compareTo(d2.getPreparedReceiver());
            if (result == 0) {
                return 0;
            }

            if (d1.getUsedMaterialCombination().equals(d2.getUsedMaterialCombination())) {
                return 0;
            }
        } else if (q1 instanceof ClickInteractorQuestSpecification i1 && q2 instanceof DeliveryQuestSpecification i2) {
            return i1.getInteractor().compareTo(i2.getPreparedReceiver().getInteractor());
        } else if (q1 instanceof DeliveryQuestSpecification i1 && q2 instanceof ClickInteractorQuestSpecification i2) {
            return i1.getPreparedReceiver().getInteractor().compareTo(i2.getInteractor());
        }

        return q1.compareTo(q2);
    };

    public abstract double generateQuest(Random ran);

    public abstract Quest createGeneratedQuest(String questName, Reward successReward);

    public abstract void clearGeneratedQuest();

    @Override
    public int compareTo(QuestSpecification other) {
        return this.getClass().getName().compareTo(other.getClass().getName());
    }

    public abstract boolean isLegal();

    protected void update() {
        CubeQuest.getInstance().getQuestGenerator().countLegalQuestSecifications();
    }

    public abstract Component getSpecificationInfo();

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QuestSpecification)) {
            return false;
        }
        return compareTo((QuestSpecification) other) == 0;
    }

    @Override
    public int hashCode() {
        // stopps warning
        return super.hashCode();
    }

}
