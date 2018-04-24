package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;

public class QuestFreezeEvent extends QuestEvent {
    
    private Player player;
    
    public QuestFreezeEvent(Quest quest, Player player) {
        super(quest);
        this.player = player;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
}
