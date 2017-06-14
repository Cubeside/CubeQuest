package de.iani.cubequest.questStates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.PlayerData;

public class AmountQuestState extends QuestState {

    private int amount;

    public AmountQuestState(PlayerData data, int questId) {
        super(data, questId);

        amount = 0;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int value) {
        this.amount = value;
    }

    public void changeAmount(int value) {
        this.amount += value;
    }

}
