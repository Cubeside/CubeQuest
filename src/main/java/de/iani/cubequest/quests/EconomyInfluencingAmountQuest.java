package de.iani.cubequest.quests;

import de.iani.cubequest.conditions.ServerFlagCondition;
import net.kyori.adventure.text.Component;


public abstract class EconomyInfluencingAmountQuest extends AmountQuest {

    public static final String SURVIVAL_ECONOMY_TAG = "survival_economy";

    public EconomyInfluencingAmountQuest(int id, Component name, Component displayMessage, int amount) {
        super(id, name, displayMessage, amount);
        init();
    }

    public EconomyInfluencingAmountQuest(int id, Component name, Component displayMessage) {
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
