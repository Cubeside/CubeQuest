package de.iani.cubequest.generation;

import java.util.Map;

public abstract class AmountAndEntityTypesQuestSpecification extends AmountQuestSpecification {
    
    private EntityTypeCombination entityTypes;
    
    public AmountAndEntityTypesQuestSpecification() {
        
    }
    
    public AmountAndEntityTypesQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
        
        this.entityTypes = (EntityTypeCombination) serialized.get("entityTypes");
    }
    
    @Override
    public void clearGeneratedQuest() {
        super.clearGeneratedQuest();
        
        this.entityTypes = null;
    }
    
    public EntityTypeCombination getEntityTypes() {
        return this.entityTypes;
    }
    
    protected void setEntityTypes(EntityTypeCombination entityTypes) {
        this.entityTypes = entityTypes;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityTypes", this.entityTypes);
        return result;
    }
}
