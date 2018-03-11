package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questGiving.QuestGiver;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuestGiverBubbleTarget extends BubbleTarget {
    
    private static Color[] bubbleColors =
            new Color[] {Color.BLUE, Color.NAVY, Color.TEAL, Color.AQUA, Color.OLIVE, Color.GREEN};
    
    private QuestGiver giver;
    
    public QuestGiverBubbleTarget(QuestGiver giver) {
        this.giver = giver;
    }
    
    @Override
    public String getName() {
        return this.giver.getName();
    }
    
    @Override
    public Location getLocation(boolean ignoreCache) {
        return this.giver.getInteractor().getLocation(ignoreCache);
    }
    
    @Override
    public double getHeight() {
        return this.giver.getInteractor().getHeight();
    }
    
    @Override
    public double getWidth() {
        return this.giver.getInteractor().getWidth();
    }
    
    @Override
    protected boolean conditionMet(Player player, PlayerData playerData) {
        return this.giver.hasQuestForPlayer(player, playerData);
    }
    
    @Override
    protected Color[] getBubbleColors() {
        return bubbleColors;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        
        if (!(other instanceof QuestGiverBubbleTarget)) {
            return false;
        }
        
        return this.giver.equals(((QuestGiverBubbleTarget) other).giver);
    }
    
    @Override
    public int hashCode() {
        return this.giver.hashCode();
    }
    
}
