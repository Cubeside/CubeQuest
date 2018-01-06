package de.iani.cubequest.questStates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import de.iani.cubequest.PlayerData;

public class AmountQuestState extends QuestState {
    
    private int amount;
    
    public AmountQuestState(PlayerData data, int questId, Status status) {
        super(data, questId, status);
        
        amount = 0;
    }
    
    public AmountQuestState(PlayerData data, int questId) {
        this(data, questId, null);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc, Status status)
            throws InvalidConfigurationException {
        super.deserialize(yc, status);
        
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
        if (amount != value) {
            this.amount = value;
            updated();
        }
    }
    
    public void changeAmount(int value) {
        setAmount(amount + value);
    }
    
}
