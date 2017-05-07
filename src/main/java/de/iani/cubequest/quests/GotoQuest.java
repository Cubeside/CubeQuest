package de.iani.cubequest.quests;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

public class GotoQuest extends Quest {

    private Location target;
    private double tolarance;

    public GotoQuest( String name, String giveMessage, String successMessage, Reward successReward,
            Location target, double tolarance) {
        super(name, giveMessage, successMessage, successReward);

        this.target = target;
        this.tolarance = tolarance;
    }

    public GotoQuest(String name) {
        this(name, null, null, null, null, 0.5);
    }

    @Override
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (!getPlayersGivenTo().contains(event.getPlayer().getUniqueId())) {
            return;
        }
        if (!event.getTo().getWorld().equals(target.getWorld())) {
            return;
        }
        if (Math.abs(event.getTo().getX() - target.getX()) > tolarance || Math.abs(event.getTo().getY() - target.getY()) > tolarance
                || Math.abs(event.getTo().getZ() - target.getZ()) > tolarance) {
            return;
        }
        onSuccess(event.getPlayer());
    }

    @Override
    public boolean isLegal() {
        return target != null && tolarance >= 0;
    }

    public Location getTarget() {
        return target;
    }

    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        this.target = arg;
    }

    public double getTolarance() {
        return tolarance;
    }

    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
    }

}
