package de.iani.cubequest.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;

public abstract class DifficultyQuestSpecification extends QuestSpecification {

    private double difficulty;

    public DifficultyQuestSpecification(double difficulty) {
        this.difficulty = difficulty;
    }

    public DifficultyQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            this.difficulty = (Double) serialized.get("difficulty");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
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
        return this.difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
        update();
    }

    @Override
    public boolean isLegal() {
        return this.difficulty > 0;
    }

    @Override
    public Component getSpecificationInfo() {
        return Component.text("Schwierigkeit: ")
                .append(Component.text(String.valueOf(this.difficulty), NamedTextColor.GREEN))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        DifficultyQuestSpecification odqs = (DifficultyQuestSpecification) other;
        result = Double.compare(getDifficulty(), odqs.getDifficulty());
        return result;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("difficulty", this.difficulty);
        return result;
    }

}
