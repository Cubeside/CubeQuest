package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;

public class GotoQuestSpecification extends DifficultyQuestSpecification {
    
    private GotoQuest dataStorageQuest;
    
    public GotoQuestSpecification() {
        super();
        
        this.dataStorageQuest = new GotoQuest(-1);
    }
    
    public GotoQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);
        
        try {
            this.dataStorageQuest = (GotoQuest) serialized.get("dataStorageQuest");
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
        
        GotoQuest result = new GotoQuest(questId, questName, null, getLocation(), getTolerance());
        result.setDisplayMessage(getGiveMessage());
        result.addGiveAction(new ChatMessageAction(getGiveMessage()));
        result.addSuccessAction(new RewardAction(successReward));
        if (!(result.getLocationName().equals(getLocationName()))) {
            result.setLocationName(getLocationName());
        }
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);
        
        return result;
    }
    
    public Location getLocation() {
        return this.dataStorageQuest.getTargetLocation();
    }
    
    public void setLocation(Location location) {
        this.dataStorageQuest.setLocation(location);
        update();
    }
    
    public double getTolerance() {
        return this.dataStorageQuest.getTolarance();
    }
    
    public void setTolerance(double tolerance) {
        this.dataStorageQuest.setTolarance(tolerance);
        update();
    }
    
    public String getLocationName() {
        return this.dataStorageQuest.getLocationName();
    }
    
    public void setLocationName(String name) {
        this.dataStorageQuest.setLocationName(name == null || name.equals("") ? null : name);
    }
    
    public String getGiveMessage() {
        for (int i = 0; i < this.dataStorageQuest.getGiveActions().size(); i++) {
            if (this.dataStorageQuest.getGiveActions().get(i) instanceof ChatMessageAction) {
                return ((ChatMessageAction) this.dataStorageQuest.getGiveActions().get(i)).getMessage();
            }
        }
        return null;
    }
    
    public void setGiveMessage(String giveMessage) {
        if (!giveMessage.startsWith(ChatColor.COLOR_CHAR + "")) {
            giveMessage = ChatColor.GOLD + giveMessage;
        }
        for (int i = 0; i < this.dataStorageQuest.getGiveActions().size(); i++) {
            if (this.dataStorageQuest.getGiveActions().get(i) instanceof ChatMessageAction) {
                this.dataStorageQuest.removeGiveAction(i);
                break;
            }
        }
        this.dataStorageQuest.addGiveAction(new ChatMessageAction(giveMessage));
        update();
    }
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder("").append(super.getSpecificationInfo()).append(" ")
                .append(ChatAndTextUtil.getLocationInfo(this.dataStorageQuest.getTargetLocation()) + " "
                        + ChatAndTextUtil.getToleranceInfo(this.dataStorageQuest.getTolarance()))
                .append(ChatColor.DARK_AQUA + " Name: ").append(TextComponent.fromLegacyText(getLocationName()))
                .append(ChatColor.DARK_AQUA + " Vergabenachricht: ")
                .append(TextComponent.fromLegacyText(
                        getGiveMessage() == null ? ChatColor.GOLD + "NULL" : ChatColor.GREEN + getGiveMessage()))
                .create();
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
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
        return getLocation() != null && getTolerance() >= 0 && getGiveMessage() != null;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", this.dataStorageQuest);
        return result;
    }
    
}
