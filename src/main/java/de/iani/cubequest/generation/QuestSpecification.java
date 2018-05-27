package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class QuestSpecification
        implements Comparable<QuestSpecification>, ConfigurationSerializable {
    
    public static final Comparator<QuestSpecification> COMPARATOR = (q1, q2) -> q1.compareTo(q2);
    
    public static final Comparator<QuestSpecification> SIMILAR_SPECIFICATIONS_COMPARATOR =
            (q1, q2) -> {
                if (q1 instanceof AmountAndMaterialsQuestSpecification
                        && q2 instanceof AmountAndMaterialsQuestSpecification) {
                    AmountAndMaterialsQuestSpecification s1 =
                            (AmountAndMaterialsQuestSpecification) q1;
                    AmountAndMaterialsQuestSpecification s2 =
                            (AmountAndMaterialsQuestSpecification) q2;
                    
                    return s1.getMaterials().compareTo(s2.getMaterials());
                } else if (q1 instanceof AmountAndEntityTypesQuestSpecification
                        && q2 instanceof AmountAndEntityTypesQuestSpecification) {
                    AmountAndEntityTypesQuestSpecification s1 =
                            (AmountAndEntityTypesQuestSpecification) q1;
                    AmountAndEntityTypesQuestSpecification s2 =
                            (AmountAndEntityTypesQuestSpecification) q2;
                    
                    return s1.getEntityTypes().compareTo(s2.getEntityTypes());
                } else if (q1 instanceof DeliveryQuestSpecification
                        && q2 instanceof DeliveryQuestSpecification) {
                    DeliveryQuestSpecification d1 = (DeliveryQuestSpecification) q1;
                    DeliveryQuestSpecification d2 = (DeliveryQuestSpecification) q2;
                    
                    int result = d1.getPreparedReceiver().compareTo(d2.getPreparedReceiver());
                    if (result == 0) {
                        return 0;
                    }
                    
                    if (Arrays.stream(d1.getPreparedDelivery()).map(i -> i.getType())
                            .collect(Collectors.toSet())
                            .equals(Arrays.stream(d2.getPreparedDelivery()).map(i -> i.getType())
                                    .collect(Collectors.toSet()))) {
                        return 0;
                    }
                } else if (q1 instanceof ClickInteractorQuestSpecification
                        && q2 instanceof DeliveryQuestSpecification) {
                    ClickInteractorQuestSpecification i1 = (ClickInteractorQuestSpecification) q1;
                    DeliveryQuestSpecification i2 = (DeliveryQuestSpecification) q2;
                    
                    return i1.getInteractor().compareTo(i2.getPreparedReceiver().getInteractor());
                } else if (q1 instanceof DeliveryQuestSpecification
                        && q2 instanceof ClickInteractorQuestSpecification) {
                    DeliveryQuestSpecification i1 = (DeliveryQuestSpecification) q1;
                    ClickInteractorQuestSpecification i2 = (ClickInteractorQuestSpecification) q2;
                    
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
    
    public abstract BaseComponent[] getSpecificationInfo();
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QuestSpecification)) {
            return false;
        }
        return compareTo((QuestSpecification) other) == 0;
    }
    
}
