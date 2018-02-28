package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questGiving.QuestGiver;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuestGiverBubbleTarget extends BubbleTarget {
    
    private static Color[] bubbleColors = new Color[] {Color.RED, Color.RED, Color.ORANGE,
            Color.YELLOW, Color.RED.mixColors(Color.ORANGE), Color.YELLOW.mixColors(Color.ORANGE)};
    
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
    protected boolean conditionMet(Player player, PlayerData playerData) {
        return this.giver.hasQuestForPlayer(player, playerData);
    }
    
    @Override
    protected Color[] getBubbleColors() {
        return bubbleColors;
    }
    
}
