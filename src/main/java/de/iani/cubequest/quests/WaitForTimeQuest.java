package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.questStates.WaitForTimeQuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerQuitEvent;

@DelegateDeserialization(Quest.class)
public class WaitForTimeQuest extends Quest {
    
    private long ms;
    
    public WaitForTimeQuest(int id, String name, String displayMessage, long msToWait) {
        super(id, name, displayMessage);
        this.ms = msToWait;
    }
    
    
    public WaitForTimeQuest(int id) {
        this(id, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.ms = yc.getLong("ms");
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("ms", this.ms);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return this.ms > 0;
    }
    
    @Override
    public WaitForTimeQuestState createQuestState(UUID player) {
        return new WaitForTimeQuestState(CubeQuest.getInstance().getPlayerData(player), getId(), this.ms);
    }
    
    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        return ((WaitForTimeQuestState) state).checkTime();
    }
    
    @Override
    public boolean onPlayerQuitEvent(PlayerQuitEvent event, QuestState state) {
        ((WaitForTimeQuestState) state).playerLeft();
        return false;
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zeitspanne: " + ChatAndTextUtil.formatTimespan(this.ms))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestDateOrTimeCommand.FULL_TIME_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @SuppressWarnings("null")
    @Override
    public List<BaseComponent[]> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        WaitForTimeQuestState state = (WaitForTimeQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        String waitedForDateString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            waitedForDateString += Quest.INDENTION;
        } else {
            waitedForDateString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        long waitedMs = status == Status.NOTGIVENTO ? 0
                : Math.min(this.ms, System.currentTimeMillis() - (state.getGoal() - this.ms));
        
        waitedForDateString += ChatColor.DARK_AQUA + "Zeit gewartet: ";
        waitedForDateString += status.color
                + ChatAndTextUtil.formatTimespan(waitedMs, " Tage", " Stunden", " Minuten", " Sekunden", ", ", " und ")
                + ChatColor.DARK_AQUA + " / "
                + ChatAndTextUtil.formatTimespan(this.ms, " Tage", " Stunden", " Minuten", " Sekunden", ", ", " und ");
        
        result.add(new ComponentBuilder(waitedForDateString).create());
        
        return result;
    }
    
    public long getTime() {
        return this.ms;
    }
    
    /**
     * Setzt die zu wartende Zeit. Betrifft keine Spieler, an die die Quest bereits vergeben wurde!
     * Deprecated, wenn isReady() true ist!
     * 
     * @param ms
     */
    public void setTime(long ms) {
        this.ms = ms;
        updateIfReal();
    }
    
}
