package de.iani.cubequest.events;

import de.iani.cubequest.quests.Quest;
import org.bukkit.entity.Player;

public class QuestSuccessEvent extends QuestEvent {
    
    private Player player;
    private boolean autoRegiven;
    
    public QuestSuccessEvent(Quest quest, Player player, boolean autoRegiven) {
        super(quest);
        this.player = player;
        this.autoRegiven = autoRegiven;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public boolean isAutoRegiven() {
        return this.autoRegiven;
    }
    
}
