package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import com.google.common.base.Verify;

public class KillEntitiesQuest extends Quest {

    private HashSet<EntityType> types;
    private int amount;
    private HashMap<UUID, Integer> states;

    public KillEntitiesQuest(String name, String giveMessage, String successMessage, Reward successReward, Collection<EntityType> types, int amount) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(types);
        Verify.verify(!types.isEmpty());
        Verify.verify(amount > 0);

        this.types = new HashSet<EntityType>(types);
        this.amount = amount;
        this.states = new HashMap<UUID, Integer>();
        for (UUID p: getPlayersGivenTo()) {
            states.put(p, 0);
        }
    }

    @Override
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!types.contains(event.getEntityType())) {
            return;
        }
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        if (getPlayerStatus(player.getUniqueId()) != Status.GIVENTO) {
            return;
        }
        if (states.get(player.getUniqueId())+1 >= amount) {
            onSuccess(player);
        } else {
            states.put(player.getUniqueId(), states.get(player.getUniqueId()) + 1);
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
