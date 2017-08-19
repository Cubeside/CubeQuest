package de.iani.cubequest.quests;

import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;

public abstract class AmountQuest extends Quest {

    private int amount;

    public AmountQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            int amount) {
        super(id, name, giveMessage, successMessage, successReward);

        this.amount = amount;
    }

    public AmountQuest(int id) {
        this(id, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        amount = yc.getInt("amount");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("amount", amount);

        return super.serialize(yc);
    }

    @Override
    public boolean isLegal() {
        return amount > 0;
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), this.getId());
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must not be negative");
        }
        this.amount = val;
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

}