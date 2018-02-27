package de.iani.cubequest.bubbles;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questGiving.QuestGiver;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuestGiverBubbleTarget extends BubbleTarget {
    
    private QuestGiver giver;
    
    public QuestGiverBubbleTarget(QuestGiver giver) {
        this.giver = giver;
    }
    
    @Override
    public Location getLocation() {
        return this.giver.getInteractor().getLocation();
    }
    
    @Override
    protected boolean conditionMet(Player player, PlayerData playerData) {
        return this.giver.hasQuestForPlayer(player, playerData);
    }
    
}
