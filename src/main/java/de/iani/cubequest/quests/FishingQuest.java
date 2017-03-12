package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import com.google.common.base.Verify;

public class FishingQuest extends Quest {

    private HashSet<Material> types;
    private int amount;
    private HashMap<UUID, Integer> states;

    public FishingQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(types);
        Verify.verify(!types.isEmpty());
        Verify.verify(amount > 0);

        this.types = new HashSet<Material>(types);
        this.amount = amount;
        this.states = new HashMap<UUID, Integer>();
        for (UUID p: getPlayersGivenTo()) {
            states.put(p, 0);
        }
    }

    @Override
    public void onPlayerFishEvent(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getCaught() instanceof Item)) {
            return;
        }
        Item item = (Item) event.getCaught();
        if (!types.contains(item.getItemStack().getType())) {
            return;
        }
        if (getPlayerStatus(event.getPlayer().getUniqueId()) != Status.GIVENTO) {
            return;
        }
        if (states.get(event.getPlayer().getUniqueId())+1 >= amount) {
            onSuccess(event.getPlayer());
        } else {
            states.put(event.getPlayer().getUniqueId(), states.get(event.getPlayer().getUniqueId()) + 1);
        }
    }

    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);
        states.put(player.getUniqueId(), 0);
    }

    @Override
    public void removeFromPlayer(UUID id) {
        super.removeFromPlayer(id);
        states.remove(id);
    }

    @Override
    public boolean onSuccess(Player player) {
        if (!super.onSuccess(player)) {
            return false;
        }
        states.remove(player.getUniqueId());
        return true;
    }

}
