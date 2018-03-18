package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;

public class QuestSuccessEvent extends QuestEvent {
    
    private Player player;
    
    public QuestSuccessEvent(Quest quest, Player player) {
        super(quest);
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
}
