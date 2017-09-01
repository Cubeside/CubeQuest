package de.iani.cubequest.generation;

import java.util.Random;

public abstract class DifficultyQuestSpecification extends QuestSpecification {

    private double difficulty;

    public DifficultyQuestSpecification(double difficulty) {
        this.difficulty = difficulty;
    }

    public DifficultyQuestSpecification() {
        this(0);
    }

    @Override
    public double generateQuest(Random ran) {
        return getDifficulty();
    }

    @Override
    public void clearGeneratedQuest() {
        // nothing
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public boolean isLegal() {
        return difficulty > 0;
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compare(other);
        if (result != 0) {
            return result;
        }

        DifficultyQuestSpecification odqs = (DifficultyQuestSpecification) other;
        result = Double.compare(getDifficulty(), odqs.getDifficulty());
        return result;
    }

}
