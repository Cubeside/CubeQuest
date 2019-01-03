package de.iani.cubequest.generation;

import java.util.Map;

public abstract class AmountAndEntityTypesQuestSpecification extends AmountQuestSpecification {
    
    private EntityTypeCombination entityTypes;
    private EntityTypeCombination usedEntityTypeCombination;
    
    public AmountAndEntityTypesQuestSpecification() {
        
    }
    
    public AmountAndEntityTypesQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
        
        this.entityTypes = (EntityTypeCombination) serialized.get("entityTypes");
        this.usedEntityTypeCombination =
                (EntityTypeCombination) serialized.get("usedEntityTypeCombination");
    }
    
    @Override
    public void clearGeneratedQuest() {
        super.clearGeneratedQuest();
        
        this.entityTypes = null;
        this.usedEntityTypeCombination = null;
    }
    
    public EntityTypeCombination getEntityTypes() {
        return this.entityTypes;
    }
    
    protected void setEntityTypes(EntityTypeCombination entityTypes) {
        this.entityTypes = entityTypes;
    }
    
    public EntityTypeCombination getUsedEntityTypeCombination() {
        return this.usedEntityTypeCombination == null ? getEntityTypes()
                : this.usedEntityTypeCombination;
    }
    
    protected void setUsedEntityTypeCombination(EntityTypeCombination entityTypes) {
        this.usedEntityTypeCombination = entityTypes;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("entityTypes", this.entityTypes);
        result.put("usedEntityTypeCombination", this.usedEntityTypeCombination);
        return result;
    }
}
