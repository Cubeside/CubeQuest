package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.quests.InteractorQuest;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuestTargetBubbleTarget extends BubbleTarget {
    
    private static Color[] bubbleColors = new Color[] {Color.RED, Color.RED, Color.ORANGE, Color.YELLOW,
            Color.RED.mixColors(Color.ORANGE), Color.YELLOW.mixColors(Color.ORANGE)};
    
    private InteractorQuest quest;
    
    public QuestTargetBubbleTarget(InteractorQuest quest) {
        if (!quest.isReady()) {
            throw new IllegalArgumentException("Quest must be ready.");
        }
        
        this.quest = quest;
    }
    
    @Override
    public String getName() {
        return "QuestTarget of " + this.quest.toString();
    }
    
    @Override
    public Location getLocation(boolean ignoreCache) {
        return getInteractor().getLocation(ignoreCache);
    }
    
    @Override
    public double getHeight() {
        return BubbleTarget.getStrechingFactor(getInteractor(), true) * getInteractor().getHeight();
    }
    
    @Override
    public double getWidth() {
        return BubbleTarget.getStrechingFactor(getInteractor(), false) * getInteractor().getWidth();
    }
    
    @Override
    protected boolean conditionMet(Player player, PlayerData data) {
        return data.isGivenTo(this.quest.getId());
    }
    
    @Override
    protected Color[] getBubbleColors() {
        return bubbleColors;
    }
    
    @Override
    public Interactor getInteractor() {
        return this.quest.getInteractor();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        
        if (!(other instanceof QuestTargetBubbleTarget)) {
            return false;
        }
        
        return this.quest.equals(((QuestTargetBubbleTarget) other).quest);
    }
    
    @Override
    public int hashCode() {
        return this.quest.hashCode();
    }
    
}
