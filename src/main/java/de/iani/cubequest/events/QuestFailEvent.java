package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;

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
