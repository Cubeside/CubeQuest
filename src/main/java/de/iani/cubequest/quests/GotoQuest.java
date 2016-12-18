package de.iani.cubequest.quests;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.base.Verify;

public class GotoQuest extends Quest {

    private Location target;
    private double tolarance;

    public GotoQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Location target, double tolarance) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(target);
        Verify.verify(tolarance >= 0);

        this.target = target;
        this.tolarance = tolarance;
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

}
