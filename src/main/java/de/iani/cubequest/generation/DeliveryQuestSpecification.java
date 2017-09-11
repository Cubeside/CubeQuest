package de.iani.cubequest.generation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.ClickNPCQuest;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuestSpecification extends QuestSpecification {

    private static DeliveryQuestSpecification instance;

    private Set<Integer> npcIDs;
    private Set<Set<Material>> materialCombinations;

    public static DeliveryQuestSpecification getInstance() {
        if (instance == null) {
            instance = new DeliveryQuestSpecification();
        }
        return instance;
    }

    public static DeliveryQuestSpecification deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
        if (instance != null) {
            if (instance.serialize().equals(serialized)) {
                return instance;
            } else {
                throw new IllegalStateException("tried to initialize a second object of singleton");
            }
        }
        instance = new DeliveryQuestSpecification(serialized);
        return instance;
    }

    private DeliveryQuestSpecification() {
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());

        this.npcIDs = new HashSet<Integer>();
        this.materialCombinations = new HashSet<Set<Material>>();
    }

    @SuppressWarnings("unchecked")
    private DeliveryQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            List<Integer> npcIDList = (List<Integer>) serialized.get("npcIDs");
            npcIDs = new HashSet<Integer>(npcIDList);

            List<? extends List<String>> materialCombinationList = (List<? extends List<String>>) serialized.get("materialCombinations");
            this.materialCombinations = new HashSet<Set<Material>>();
            materialCombinationList.forEach(list -> {
                EnumSet<Material> materialSet = EnumSet.noneOf(Material.class);
                list.forEach(materialName -> materialSet.add(Material.valueOf(materialName)));
                materialCombinations.add(materialSet);
            });
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public double generateQuest(Random ran) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clearGeneratedQuest() {
        // TODO Auto-generated method stub

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
        return npcIDs.getNPC();
    }

    public void setNPC(Integer npcId) {
        npcIDs.setNPC(npcId);
        update();
    }

    public String getGiveMessage() {
        return npcIDs.getGiveMessage();
    }

    public void setGiveMessage(String giveMessage) {
        npcIDs.setGiveMessage(giveMessage);
        update();
    }

    public String getSuccessMessage() {
        return npcIDs.getSuccessMessage();
    }

    public void setSuccessMessage(String successMessage) {
        npcIDs.setSuccessMessage(successMessage);
        update();
    }

    @Override
    public int compareTo(QuestSpecification other) {
        DeliveryQuestSpecification cnpcqs = (DeliveryQuestSpecification) other;

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
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("npcIDs", new ArrayList<Integer>(npcIDs));

        List<List<String>> materialCombinationList = new ArrayList<List<String>>();
        materialCombinations.forEach(set -> {
            List<String> list = new ArrayList<String>();
            set.forEach(material -> list.add(material.name()));
            materialCombinationList.add(list);
        });
        result.put("materialCombinations", materialCombinationList);

        return result;
    }

}
