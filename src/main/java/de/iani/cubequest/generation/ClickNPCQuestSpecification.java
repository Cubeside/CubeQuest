package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.logging.Level;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.ClickNPCQuest;
import net.citizensnpcs.api.npc.NPC;

public class ClickNPCQuestSpecification extends DifficultyQuestSpecification {

    private ClickNPCQuest dataStorageQuest;

    public ClickNPCQuestSpecification() {
        super();

        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

        this.dataStorageQuest = new ClickNPCQuest(-1);
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

        ClickNPCQuest result = new ClickNPCQuest(questId, questName, getGiveMessage(), getSuccessMessage(), successReward, getNPC().getId());
        QuestManager.getInstance().addQuest(result);
        result.updateIfReal();

        return result;
    }

    public NPC getNPC() {
        return dataStorageQuest.getNPC();
    }

    public void setNPC(Integer npcId) {
        dataStorageQuest.setNPC(npcId);
    }

    public String getGiveMessage() {
        return dataStorageQuest.getGiveMessage();
    }

    public void setGiveMessage(String giveMessage) {
        dataStorageQuest.setGiveMessage(giveMessage);
    }

    public String getSuccessMessage() {
        return dataStorageQuest.getSuccessMessage();
    }

    public void setSuccessMessage(String successMessage) {
        dataStorageQuest.setSuccessMessage(successMessage);
    }

    public Reward getSuccessReward() {
        return dataStorageQuest.getSuccessReward();
    }

    @Override
    public int compareTo(QuestSpecification other) {
        ClickNPCQuestSpecification cnpcqs = (ClickNPCQuestSpecification) other;

        int i1 = getNPC() == null? 0 : getNPC().getId();
        int i2 = cnpcqs.getNPC() == null? 0 : cnpcqs.getNPC().getId();

        return Integer.compare(i1, i2);
    }

    @Override
    public boolean isLegal() {
        return getNPC() != null;
    }

}
