package de.iani.cubequest.bubbles;

import de.iani.cubequest.questGiving.QuestGiver;
import org.bukkit.Location;


public class QuestGiverBubbleTarget extends BubbleTarget {
    
    private QuestGiver giver;
    
    public QuestGiverBubbleTarget(QuestGiver giver) {
        this.giver = giver;
    }
    
    @Override
    public Location getLocation() {
        return this.giver.getInteractor().getLocation();
    }
    
}
