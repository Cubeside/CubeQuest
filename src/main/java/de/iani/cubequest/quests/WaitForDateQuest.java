package de.iani.cubequest.quests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@DelegateDeserialization(Quest.class)
public class WaitForDateQuest extends Quest {
    
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat(Util.DATE_AND_TIME_FORMAT_STRING);
    
    private long dateInMs;
    private boolean done = false;
    private TimerTask task = null;
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            long dateInMs) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        this.dateInMs = dateInMs;
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, long dateInMs) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                dateInMs);
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward, date.getTime());
    }
    
    public WaitForDateQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Date date) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                date.getTime());
    }
    
    public WaitForDateQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.dateInMs = yc.getLong("dateInMs");
        
        checkTime();
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("dateInMs", dateInMs);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public void giveToPlayer(Player player) {
        if (System.currentTimeMillis() > dateInMs) {
            throw new IllegalStateException(
                    "Date exceeded by " + (System.currentTimeMillis() - dateInMs) + " ms!");
        }
        super.giveToPlayer(player);
    }
    
    @Override
    public boolean isLegal() {
        return System.currentTimeMillis() < dateInMs;
    }
    
    @Override
    public boolean isReady() {
        return super.isReady() && !done;
    }
    
    @Override
    public void setReady(boolean val) {
        if (done && val) {
            throw new IllegalStateException(
                    "This WaitForDateQuest is already done and cannot be set to ready.");
        }
        
        boolean before = isReady();
        super.setReady(val);
        
        if (before != isReady()) {
            checkTime();
        }
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Datum: " + dateFormat.format(new Date(dateInMs))).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        if (done) {
            this.onSuccess(state.getPlayerData().getPlayer());
            return true;
        }
        return false;
    }
    
    public void checkTime() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        Quest other = QuestManager.getInstance().getQuest(this.getId());
        if (other != this) {
            return;
        }
        if (!isReady()) {
            return;
        }
        if (System.currentTimeMillis() < dateInMs) {
            task = new TimerTask() {
                
                @Override
                public void run() {
                    Bukkit.getScheduler().runTask(CubeQuest.getInstance(), () -> checkTime());
                }
            };
            CubeQuest.getInstance().getTimer().schedule(task, getDate());
        } else {
            done = true;
            for (Player player: Bukkit.getOnlinePlayers()) {
                if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.getId())) {
                    this.onSuccess(player);
                }
            }
        }
    }
    
    public long getDateMs() {
        return dateInMs;
    }
    
    public Date getDate() {
        return new Date(dateInMs);
    }
    
    public void setDate(long ms) {
        if (done) {
            throw new IllegalStateException(
                    "WaitForDateQuest is already done and cannot be set to another date!");
        }
        
        this.dateInMs = ms;
        updateIfReal();
        checkTime();
    }
    
    public void setDate(Date date) {
        setDate(date.getTime());
    }
    
    public boolean isDone() {
        return done;
    }
    
}
