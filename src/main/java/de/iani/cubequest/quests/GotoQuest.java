package de.iani.cubequest.quests;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerMoveEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;

public class GotoQuest extends ServerDependendQuest {

    private Location target;
    private double tolarance;

    public GotoQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, int serverId,
            Location target, double tolarance) {
        super(id, name, giveMessage, successMessage, successReward, serverId);

        if (isForThisServer()) {
            this.target = target;
        }
        this.tolarance = tolarance;
    }

    public GotoQuest(int id) {
        this(id, null, null, null, null, CubeQuest.getInstance().getServerId(), null, 0.5);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        target = (Location) yc.get("target");
        tolarance = yc.getDouble("tolarance");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {

        yc.set("target", target);
        yc.set("tolarance", tolarance);

        return super.serialize(yc);
    }

    @Override
    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }
        if (!event.getTo().getWorld().equals(target.getWorld())) {
            return false;
        }
        if (Math.abs(event.getTo().getX() - target.getX()) > tolarance || Math.abs(event.getTo().getY() - target.getY()) > tolarance
                || Math.abs(event.getTo().getZ() - target.getZ()) > tolarance) {
            return false;
        }
        onSuccess(event.getPlayer());
        return true;
    }

    @Override
    public boolean isLegal() {
        return !isForThisServer() || target != null && tolarance >= 0;
    }

    public Location getTarget() {
        return target;
    }

    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        this.target = arg;
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

    public double getTolarance() {
        return tolarance;
    }

    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

}
