package de.iani.cubequest.generation;

import java.util.Map;


public abstract class AmountAndMaterialsQuestSpecification extends AmountQuestSpecification {
    
    private MaterialCombination materials;
    private MaterialCombination usedMaterialCombination;
    
    public AmountAndMaterialsQuestSpecification() {
        
    }
    
    public AmountAndMaterialsQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
        
        this.materials = (MaterialCombination) serialized.get("materials");
        this.usedMaterialCombination =
                (MaterialCombination) serialized.get("usedMaterialCombination");
    }
    
    @Override
    public void clearGeneratedQuest() {
        super.clearGeneratedQuest();
        
        this.materials = null;
        this.usedMaterialCombination = null;
    }
    
    public MaterialCombination getMaterials() {
        return this.materials;
    }
    
    protected void setMaterials(MaterialCombination materials) {
        this.materials = materials;
    }
    
    public MaterialCombination getUsedMaterialCombination() {
        return this.usedMaterialCombination == null ? getMaterials() : this.usedMaterialCombination;
    }
    
    protected void setUsedMaterialCombination(MaterialCombination materials) {
        this.usedMaterialCombination = materials;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("materials", this.materials);
        result.put("usedMaterialCombination", this.usedMaterialCombination);
        return result;
    }
    
}
