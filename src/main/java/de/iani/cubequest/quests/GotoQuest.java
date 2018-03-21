package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerMoveEvent;

@DelegateDeserialization(Quest.class)
public class GotoQuest extends ServerDependendQuest {
    
    private String world;
    private double x, y, z;
    private double tolarance;
    
    private String overwriteLocationName;
    
    public GotoQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, int serverId, String world, double x,
            double y, double z, double tolarance) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, serverId);
        
        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }
        
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tolarance = tolarance;
    }
    
    public GotoQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Location location, double tolarance) {
        this(id, name, displayMessage, giveMessage, successMessage, successReward,
                CubeQuest.getInstance().getServerId(), location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), tolarance);
    }
    
    public GotoQuest(int id) {
        this(id, null, null, null, null, null, CubeQuest.getInstance().getServerId(), null, 0, 0, 0,
                0.5);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        String world = yc.getString("target.world");
        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }
        
        this.world = world;
        this.x = yc.getDouble("target.x");
        this.y = yc.getDouble("target.y");
        this.z = yc.getDouble("target.z");
        this.tolarance = yc.getDouble("tolarance");
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        
        yc.createSection("target");
        yc.set("target.world", this.world);
        yc.set("target.x", this.x);
        yc.set("target.y", this.y);
        yc.set("target.z", this.z);
        yc.set("tolarance", this.tolarance);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }
        if (!event.getTo().getWorld().getName().equals(this.world)) {
            return false;
        }
        if (Math.abs(event.getTo().getX() - this.x) > this.tolarance
                || Math.abs(event.getTo().getY() - this.y) > this.tolarance
                || Math.abs(event.getTo().getZ() - this.z) > this.tolarance) {
            return false;
        }
        onSuccess(event.getPlayer());
        return true;
    }
    
    @Override
    public boolean isLegal() {
        return this.world != null && this.tolarance >= 0
                && (!isForThisServer() || Bukkit.getWorld(this.world) != null);
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zu erreichender Ort: "
                + ChatAndTextUtil.getLocationInfo(this.world, this.x, this.y, this.z)).create());
        result.add(new ComponentBuilder(ChatAndTextUtil.getToleranceInfo(this.tolarance)).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        
        String goneToLocationString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            goneToLocationString += Quest.INDENTION;
        } else {
            goneToLocationString += getStateStringStartingToken(state) + " ";
        }
        
        goneToLocationString +=
                ChatColor.DARK_AQUA + getLocationName() + ChatColor.DARK_AQUA + " erreicht: ";
        goneToLocationString +=
                state.getStatus().color + (state.getStatus() == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(goneToLocationString).create());
        
        return result;
    }
    
    public Location getTargetLocation() {
        return isForThisServer() && this.world != null
                ? new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z)
                : null;
    }
    
    public double getTargetX() {
        return this.x;
    }
    
    public double getTargetY() {
        return this.y;
    }
    
    public double getTargetZ() {
        return this.z;
    }
    
    public String getTargetWorld() {
        return this.world;
    }
    
    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        this.world = arg.getWorld().getName();
        this.x = arg.getX();
        this.y = arg.getY();
        this.z = arg.getZ();
        
        updateIfReal();
    }
    
    public double getTolarance() {
        return this.tolarance;
    }
    
    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
        updateIfReal();
    }
    
    public String getLocationName() {
        if (this.overwriteLocationName != null) {
            return this.overwriteLocationName;
        }
        
        String name = "x = " + this.x;
        name += ", y = " + this.y;
        name += ", z = " + this.z;
        name += " ± " + this.tolarance;
        name += " in Welt \"" + this.world + "\"";
        
        return name;
    }
    
    public void setOverwrittenLocationName(String name) {
        this.overwriteLocationName = name;
    }
    
}
