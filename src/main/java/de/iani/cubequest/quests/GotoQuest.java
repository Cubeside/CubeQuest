package de.iani.cubequest.quests;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerMoveEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class GotoQuest extends ServerDependendQuest {

    private String world;
    private double x, y, z;
    private double tolarance;

    public GotoQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, int serverId,
            String world, double x, double y, double z, double tolarance) {
        super(id, name, giveMessage, successMessage, successReward, serverId);

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

    public GotoQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Location location, double tolarance) {
        this(id, name, giveMessage, successMessage, successReward, CubeQuest.getInstance().getServerId(),
                location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), tolarance);
    }

    public GotoQuest(int id) {
        this(id, null, null, null, null, CubeQuest.getInstance().getServerId(), null, 0, 0, 0, 0.5);
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
        x = yc.getDouble("target.x");
        y = yc.getDouble("target.y");
        z = yc.getDouble("target.z");
        tolarance = yc.getDouble("tolarance");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {

        yc.createSection("target");
        yc.set("target.world", world);
        yc.set("target.x", x);
        yc.set("target.y", y);
        yc.set("target.z", z);
        yc.set("tolarance", tolarance);

        return super.serializeToString(yc);
    }

    @Override
    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }
        if (!event.getTo().getWorld().getName().equals(world)) {
            return false;
        }
        if (Math.abs(event.getTo().getX() - x) > tolarance || Math.abs(event.getTo().getY() - y) > tolarance
                || Math.abs(event.getTo().getZ() - z) > tolarance) {
            return false;
        }
        onSuccess(event.getPlayer());
        return true;
    }

    @Override
    public boolean isLegal() {
        return world != null && tolarance >= 0 && (!isForThisServer() || Bukkit.getWorld(world) != null);
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        String locationString = ChatColor.DARK_AQUA + "Zu erreichender Ort: ";
        if (world == null) {
            locationString += ChatColor.RED + "NULL";
        } else {
            locationString += ChatColor.GREEN + "Welt: " + world + "x: " + x + " y: " + y + " z: " + z;
        }

        result.add(new ComponentBuilder(locationString).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Toleranz: " + (tolarance >= 0? ChatColor.GREEN : ChatColor.RED) + tolarance).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public Location getTargetLocation() {
        return isForThisServer() && world != null? new Location(Bukkit.getWorld(world), x, y, z) : null;
    }

    public double getTargetX() {
        return x;
    }

    public double getTargetY() {
        return y;
    }

    public double getTargetZ() {
        return z;
    }

    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        world = arg.getWorld().getName();
        x = arg.getX();
        y = arg.getY();
        z = arg.getZ();

        updateIfReal();
    }

    public double getTolarance() {
        return tolarance;
    }

    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
        updateIfReal();
    }

}
