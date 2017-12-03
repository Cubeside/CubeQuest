package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.ClickNPCQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ClickNPCQuestSpecification extends DifficultyQuestSpecification {

    private ClickNPCQuest dataStorageQuest;

    public ClickNPCQuestSpecification() {
        super();

        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

        this.dataStorageQuest = new ClickNPCQuest(-1);
    }

    public ClickNPCQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            dataStorageQuest = (ClickNPCQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public ClickNPCQuest createGeneratedQuest(String questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated GotoQuest!", e);
            return null;
        }

        ClickNPCQuest result = new ClickNPCQuest(questId, questName, null, CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getGiveMessage(), CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " " + getSuccessMessage(), successReward, getNPC().getId());
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        return result;
    }

    public NPC getNPC() {
        return dataStorageQuest.getNPC();
    }

    public void setNPC(Integer npcId) {
        dataStorageQuest.setNPC(npcId);
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
                .append(ChatColor.DARK_AQUA + " NPC: " + ChatAndTextUtil.getNPCInfoString(true, dataStorageQuest.getNPC().getId()))
                .append(ChatColor.DARK_AQUA + " Vergabenachricht: " + (getGiveMessage() == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + getGiveMessage()))
                .append(ChatColor.DARK_AQUA + " Erfolgsnachricht: " + (getSuccessMessage() == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + getSuccessMessage())).create();
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compare(other);
        if (result != 0) {
            return result;
        }

        ClickNPCQuestSpecification cnpcqs = (ClickNPCQuestSpecification) other;

        int i1 = getNPC() == null? 0 : getNPC().getId();
        int i2 = cnpcqs.getNPC() == null? 0 : cnpcqs.getNPC().getId();

        return Integer.compare(i1, i2);
    }

    @Override
    public boolean isLegal() {
        return getNPC() != null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", dataStorageQuest);
        return result;
    }

}
