package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.WaitForTimeQuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerQuitEvent;

@DelegateDeserialization(Quest.class)
public class WaitForTimeQuest extends Quest {
    
    private long ms;
    
    public WaitForTimeQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            long msToWait) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        this.ms = msToWait;
    }
    
    public WaitForTimeQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, long msToWait) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                msToWait);
    }
    
    public WaitForTimeQuest(int id) {
        this(id, null, null, null, null, null, 0);
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
        return new WaitForTimeQuestState(CubeQuest.getInstance().getPlayerData(player), getId(),
                this.ms);
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
        
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Zeitspanne: " + ChatAndTextUtil.formatTimespan(this.ms))
                        .create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        WaitForTimeQuestState state = (WaitForTimeQuestState) data.getPlayerState(getId());
        
        String waitedForDateString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            waitedForDateString += Quest.INDENTION;
        } else {
            waitedForDateString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }
        
        waitedForDateString += ChatColor.DARK_AQUA + "Zeit gewartet: ";
        waitedForDateString += state.getStatus().color
                + ChatAndTextUtil.formatTimespan(
                        System.currentTimeMillis() - (state.getGoal() - this.ms), " Tage",
                        " Stunden", " Minuten", " Sekunden", ", ", " und ")
                + ChatColor.DARK_AQUA + " / " + ChatAndTextUtil.formatTimespan(this.ms, " Tage",
                        " Stunden", " Minuten", " Sekunden", ", ", " und ");
        
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
