package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.quests.ClickInteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ClickInteractorQuestSpecification extends DifficultyQuestSpecification {
    
    private ClickInteractorQuest dataStorageQuest;
    
    public ClickInteractorQuestSpecification() {
        super();
        
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        
        this.dataStorageQuest = new ClickInteractorQuest(-1);
    }
    
    public ClickInteractorQuestSpecification(Map<String, Object> serialized)
            throws InvalidConfigurationException {
        super(serialized);
        
        try {
            dataStorageQuest = (ClickInteractorQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    @Override
    public ClickInteractorQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create generated GotoQuest!", e);
            return null;
        }
        
        ClickInteractorQuest result = new ClickInteractorQuest(questId, questName, null,
                CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getGiveMessage(),
                CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getSuccessMessage(), successReward,
                getInteractor());
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();
        
        return result;
    }
    
    public Interactor getInteractor() {
        return dataStorageQuest.getInteractor();
    }
    
    public void setInteractor(Interactor interactor) {
        dataStorageQuest.setInteractor(interactor);
        update();
    }
    
    public String getGiveMessage() {
        return dataStorageQuest.getGiveMessage();
    }
    
    public void setGiveMessage(String giveMessage) {
        dataStorageQuest.setGiveMessage(giveMessage);
        update();
    }
    
    public String getSuccessMessage() {
        return dataStorageQuest.getSuccessMessage();
    }
    
    public void setSuccessMessage(String successMessage) {
        dataStorageQuest.setSuccessMessage(successMessage);
        update();
    }
    
    
    
    @Override
    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder("").append(super.getSpecificationInfo())
                .append(ChatColor.DARK_AQUA + " Interactor: "
                        + ChatAndTextUtil.getInteractorInfoString(dataStorageQuest.getInteractor()))
                .append(ChatColor.DARK_AQUA + " Vergabenachricht: "
                        + (getGiveMessage() == null ? ChatColor.GOLD + "NULL"
                                : ChatColor.GREEN + getGiveMessage()))
                .append(ChatColor.DARK_AQUA + " Erfolgsnachricht: "
                        + (getSuccessMessage() == null ? ChatColor.GOLD + "NULL"
                                : ChatColor.GREEN + getSuccessMessage()))
                .create();
    }
    
    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compare(other);
        if (result != 0) {
            return result;
        }
        
        ClickInteractorQuestSpecification cnpcqs = (ClickInteractorQuestSpecification) other;
        
        int i1 = getInteractor() == null ? 0 : 1;
        int i2 = cnpcqs.getInteractor() == null ? 0 : 1;
        
        if (i1 != i2) {
            return i1 - i2;
        }
        
        return i1 == 0 ? 0 : getInteractor().compareTo(cnpcqs.getInteractor());
    }
    
    @Override
    public boolean isLegal() {
        return getInteractor() != null && getInteractor().isLegal();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", dataStorageQuest);
        return result;
    }
    
}
