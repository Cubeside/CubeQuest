package de.iani.cubequest.generation;

import java.util.Comparator;
import java.util.Random;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class QuestSpecification
        implements Comparable<QuestSpecification>, ConfigurationSerializable {
    
    public static final Comparator<? super QuestSpecification> COMPARATOR =
            (q1, q2) -> q1.compare(q2);
    
    public abstract double generateQuest(Random ran);
    
    public abstract Quest createGeneratedQuest(String questName, Reward successReward);
    
    public abstract void clearGeneratedQuest();
    
    public int compare(QuestSpecification other) {
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
        return compare((QuestSpecification) other) == 0;
    }
    
}
