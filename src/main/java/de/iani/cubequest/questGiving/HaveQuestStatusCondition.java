package de.iani.cubequest.questGiving;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;


public class HaveQuestStatusCondition extends QuestCondition {
    
    private int questId;
    private Status status;
    
    public HaveQuestStatusCondition(Quest quest, Status status) {
        this.questId = quest.getId();
        this.status = status;
    }
    
    public HaveQuestStatusCondition(Map<String, Object> serialized) {
        this.questId = (Integer) serialized.get("questId");
        this.status = Status.valueOf((String) serialized.get("status"));
    }
    
    @Override
    public boolean fullfills(Player player, PlayerData data) {
        return data.getPlayerStatus(this.questId) == this.status;
    }
    
    @Override
    public List<BaseComponent[]> getConditionInfo() {
        Quest quest = QuestManager.getInstance().getQuest(this.questId);
        
        ChatColor color = quest == null ? ChatColor.RED : ChatColor.DARK_AQUA;
        ComponentBuilder builder = new ComponentBuilder("Quest ").color(color);
        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(quest == null ? "Quest existiert nicht"
                        : ("Info zu " + quest.toString() + " anzeigen")).create());
        builder.append("" + this.questId).event(he);
        
        if (quest != null) {
            ClickEvent ce =
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest info " + this.questId);
            builder.event(ce);
        }
        
        builder.append(": " + this.status.toString());
        
        return Collections.singletonList(builder.create());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("questId", this.questId);
        result.put("status", this.status.toString());
        
        return result;
    }
    
}
