package de.iani.cubequest.generation;

import org.bukkit.Location;

import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.Quest;

public class GotoQuestSpecification extends DifficultyQuestSpecification {

    private GotoQuest dataStorageQuest;

    public GotoQuestSpecification() {
        super();

        this.dataStorageQuest = new GotoQuest(-1);
    }

    @Override
    public Quest createGeneratedQuest() {
        // TODO Auto-generated method stub
        return null;
    }

    public Location getLocation() {
        return dataStorageQuest.getTargetLocation();
    }

    public void setLocation(Location location) {
        this.dataStorageQuest.setLocation(location);;
    }

    public double getTolerance() {
        return dataStorageQuest.getTolarance();
    }

    public void setTolerance(double tolerance) {
        this.dataStorageQuest.setTolarance(tolerance);
    }

    public String getGiveMessage() {
        return this.dataStorageQuest.getGiveMessage();
    }

    public void setGiveMessage(String giveMessage) {
        this.dataStorageQuest.setGiveMessage(giveMessage);
    }

    public String getSuccessMessage() {
        return this.dataStorageQuest.getSuccessMessage();
    }

    public void setSuccessMessage(String successMessage) {
        this.dataStorageQuest.setSuccessMessage(successMessage);
    }

    @Override
    public int compare(QuestSpecification other) {
        int result = super.compare(other);
        if (result != 0) {
            return result;
        }

        GotoQuestSpecification ogqs = (GotoQuestSpecification) other;

        result = getLocation().getWorld().getName().compareTo(ogqs.getLocation().getWorld().getName());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getX(), ogqs.getLocation().getX());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getY(), ogqs.getLocation().getY());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getZ(), ogqs.getLocation().getZ());
        if (result != 0) {
            return result;
        }

        return 0;
    }

    @Override
    public boolean isLegal() {
        return getLocation() != null && getTolerance() >= 0 && getGiveMessage() != null && getSuccessMessage() != null;
    }

}
