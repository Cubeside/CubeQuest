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
                int result = q1.getClass().getName().compareTo(q2.getClass().getName());
                if (result != 0) {
                    if (q1 instanceof ClickInteractorQuestSpecification
                            && q2 instanceof DeliveryQuestSpecification) {
                        ClickInteractorQuestSpecification i1 =
                                (ClickInteractorQuestSpecification) q1;
                        DeliveryQuestSpecification i2 = (DeliveryQuestSpecification) q2;
                        
                        return i1.getInteractor()
                                .compareTo(i2.getPreparedReceiver().getInteractor());
                    } else if (q1 instanceof DeliveryQuestSpecification
                            && q2 instanceof ClickInteractorQuestSpecification) {
                        DeliveryQuestSpecification i1 = (DeliveryQuestSpecification) q1;
                        ClickInteractorQuestSpecification i2 =
                                (ClickInteractorQuestSpecification) q2;
                        
                        return i1.getPreparedReceiver().getInteractor()
                                .compareTo(i2.getInteractor());
                    } else {
                        return result;
                    }
                }
                
                if (!(q1 instanceof DeliveryQuestSpecification)) {
                    return q1.compareTo(q2);
                }
                
                DeliveryQuestSpecification d1 = (DeliveryQuestSpecification) q1;
                DeliveryQuestSpecification d2 = (DeliveryQuestSpecification) q2;
                
                result = d1.getPreparedReceiver().compareTo(d2.getPreparedReceiver());
                if (result == 0) {
                    return 0;
                }
                
                if (Arrays.stream(d1.getPreparedDelivery()).map(i -> i.getType())
                        .collect(Collectors.toSet()).equals(Arrays.stream(d2.getPreparedDelivery())
                                .map(i -> i.getType()).collect(Collectors.toSet()))) {
                    return 0;
                }
                
                return d1.compareTo(d2);
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
