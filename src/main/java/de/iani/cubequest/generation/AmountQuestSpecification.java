package de.iani.cubequest.generation;

import java.util.HashMap;
import java.util.Map;


public abstract class AmountQuestSpecification extends QuestSpecification {
    
    private int amount;
    
    public AmountQuestSpecification() {
        
    }
    
    public AmountQuestSpecification(Map<String, Object> serialized) {
        this.amount = (Integer) serialized.get("amount");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("amount", this.amount);
        return result;
    }
    
    @Override
    public void clearGeneratedQuest() {
        this.amount = 0;
    }
    
    @Override
    public boolean isLegal() {
        return this.amount > 0;
    }
    
    public int getAmount() {
        return this.amount;
    }
    
    protected void setAmount(int amount) {
        this.amount = amount;
    }
    
}
