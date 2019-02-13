package de.iani.cubequest.quests;

import de.iani.cubequest.conditions.ServerFlagCondition;


public abstract class EconomyInfluencingAmountQuest extends AmountQuest {
    
    public static final String SURVIVAL_ECONOMY_TAG = "survival_economy";
    
    public EconomyInfluencingAmountQuest(int id, String name, String displayMessage, int amount) {
        super(id, name, displayMessage, amount);
        init();
    }
    
    public EconomyInfluencingAmountQuest(int id, String name, String displayMessage) {
        super(id, name, displayMessage, 0);
        init();
    }
    
    public EconomyInfluencingAmountQuest(int id) {
        super(id);
        init();
    }
    
    private void init() {
        addQuestProgressCondition(new ServerFlagCondition(false, SURVIVAL_ECONOMY_TAG), false);
    }
    
}
