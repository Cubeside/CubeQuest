package de.iani.cubequest.generation;

import java.util.Map;


public abstract class AmountAndMaterialsQuestSpecification extends AmountQuestSpecification {
    
    private MaterialCombination materials;
    
    public AmountAndMaterialsQuestSpecification() {
        
    }
    
    public AmountAndMaterialsQuestSpecification(Map<String, Object> serialized) {
        super(serialized);
        
        this.materials = (MaterialCombination) serialized.get("materials");
    }
    
    @Override
    public void clearGeneratedQuest() {
        super.clearGeneratedQuest();
        
        this.materials = null;
    }
    
    public MaterialCombination getMaterials() {
        return this.materials;
    }
    
    protected void setMaterials(MaterialCombination materials) {
        this.materials = materials;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("materials", this.materials);
        return result;
    }
    
}
