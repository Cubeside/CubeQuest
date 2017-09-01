package de.iani.cubequest.generation;

import java.util.Comparator;
import java.util.Random;

import de.iani.cubequest.quests.Quest;

public abstract class QuestSpecification implements Comparable<QuestSpecification> {

    public static final Comparator<? super QuestSpecification> COMPARATOR = (q1, q2) -> q1.compare(q2);

    public abstract double generateQuest(Random ran);

    public abstract Quest createGeneratedQuest();

    public abstract void clearGeneratedQuest();

    public int compare(QuestSpecification other) {
        return this.getClass().getName().compareTo(other.getClass().getName());
    }

    public abstract boolean isLegal();

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QuestSpecification)) {
            return false;
        }
        return compare((QuestSpecification) other) == 0;
    }

}
