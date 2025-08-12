package de.iani.cubequest.questStates;

import de.iani.cubequest.PlayerData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class AmountQuestState extends QuestState {
    
    private int amount;
    
    public AmountQuestState(PlayerData data, int questId, Status status, long lastAction, boolean hidden) {
        super(data, questId, status, lastAction, hidden);
        
        this.amount = 0;
    }
    
    public AmountQuestState(PlayerData data, int questId, long lastAction, boolean hidden) {
        this(data, questId, Status.NOTGIVENTO, lastAction, hidden);
    }
    
    public AmountQuestState(PlayerData data, int questId) {
        this(data, questId, System.currentTimeMillis(), false);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc, Status status) throws InvalidConfigurationException {
        super.deserialize(yc, status);
        
        this.amount = yc.getInt("amount");
    }
    
    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("amount", this.amount);
        
        return super.serialize(yc);
    }
    
    public int getAmount() {
        return this.amount;
    }
    
    public void setAmount(int value) {
        if (this.amount != value) {
            this.amount = value;
            updated();
        }
    }
    
    public void changeAmount(int value) {
        setAmount(this.amount + value);
    }
    
}
