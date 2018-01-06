package de.iani.cubequest.events;

import org.bukkit.entity.Player;
import de.iani.cubequest.quests.Quest;

public class QuestFailEvent extends QuestEvent {
    
    private Player player;
    
    public QuestFailEvent(Quest quest, Player player) {
        super(quest);
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
}
