package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;


public class HaveQuestStatusCondition extends QuestCondition {
    
    private int questId;
    private Status status;
    
    public HaveQuestStatusCondition(boolean visible, Quest quest, Status status) {
        super(visible);
        init(quest.getId(), status);
    }
    
    public HaveQuestStatusCondition(Map<String, Object> serialized) {
        super(serialized);
        init(((Number) serialized.get("questId")).intValue(), Status.valueOf((String) serialized.get("status")));
    }
    
    private void init(int questId, Status status) {
        this.questId = questId;
        this.status = Objects.requireNonNull(status);
    }
    
    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return data.getPlayerStatus(this.questId) == this.status;
    }
    
    @Override
    public BaseComponent[] getConditionInfo() {
        Quest quest = QuestManager.getInstance().getQuest(this.questId);
        
        ChatColor color = quest == null ? ChatColor.RED : ChatColor.DARK_AQUA;
        ComponentBuilder builder = new ComponentBuilder("Quest ").color(color);
        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(quest == null ? "Quest existiert nicht" : ("Info zu " + quest.toString() + " anzeigen")));
        builder.append("" + this.questId).event(he);
        
        if (quest != null) {
            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest info " + this.questId);
            builder.event(ce);
        }
        
        builder.append(": " + this.status.toString());
        
        return builder.create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("questId", this.questId);
        result.put("status", this.status.name());
        return result;
    }
    
}
