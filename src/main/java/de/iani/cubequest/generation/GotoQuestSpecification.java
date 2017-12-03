package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class GotoQuestSpecification extends DifficultyQuestSpecification {

    private GotoQuest dataStorageQuest;

    public GotoQuestSpecification() {
        super();

        this.dataStorageQuest = new GotoQuest(-1);
    }

    public GotoQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            dataStorageQuest = (GotoQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public GotoQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated GotoQuest!", e);
            return null;
        }

        GotoQuest result = new GotoQuest(questId, questName, null, CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getGiveMessage(), CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getSuccessMessage(), successReward, getLocation(), getTolerance());
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        return result;
    }

    public Location getLocation() {
        return dataStorageQuest.getTargetLocation();
    }

    public void setLocation(Location location) {
        this.dataStorageQuest.setLocation(location);
        update();
    }

    public double getTolerance() {
        return dataStorageQuest.getTolarance();
    }

    public void setTolerance(double tolerance) {
        this.dataStorageQuest.setTolarance(tolerance);
        update();
    }

    public String getGiveMessage() {
        return this.dataStorageQuest.getGiveMessage();
    }

    public void setGiveMessage(String giveMessage) {
        this.dataStorageQuest.setGiveMessage(giveMessage);
        update();
    }

    public String getSuccessMessage() {
        return this.dataStorageQuest.getSuccessMessage();
    }

    public void setSuccessMessage(String successMessage) {
        this.dataStorageQuest.setSuccessMessage(successMessage);
        update();
    }

    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder("").append(super.getSpecificationInfo()).append(" ")
                .append(ChatAndTextUtil.getLocationInfo(dataStorageQuest.getTargetLocation()) + " " + ChatAndTextUtil.getToleranceInfo(dataStorageQuest.getTolarance()))
                .append(ChatColor.DARK_AQUA + " Vergabenachricht: " + (getGiveMessage() == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + getGiveMessage()))
                .append(ChatColor.DARK_AQUA + " Erfolgsnachricht: " + (getSuccessMessage() == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + getSuccessMessage())).create();
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", dataStorageQuest);
        return result;
    }

}
