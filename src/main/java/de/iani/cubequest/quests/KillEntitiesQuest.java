package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEntitiesQuest extends Quest {

    private HashSet<EntityType> types;
    private int amount;
    private HashMap<UUID, Integer> states;

    public KillEntitiesQuest(String name, String giveMessage, String successMessage, Reward successReward, Collection<EntityType> types, int amount) {
        super(name, giveMessage, successMessage, successReward);

        this.types = (types == null)? new HashSet<EntityType>() : new HashSet<EntityType>(types);
        this.amount = amount;
        this.states = new HashMap<UUID, Integer>();
        for (UUID p: getPlayersGivenTo()) {
            states.put(p, 0);
        }
    }

    public KillEntitiesQuest(String name) {
        this(name, null, null, null, null, 0);
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

    @Override
    public boolean isLegal() {
        return !types.isEmpty() && amount > 0;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int arg) {
        if (arg < 1) {
            throw new IllegalArgumentException("arg msut be greater than 0");
        }
        this.amount = arg;
    }

    public Set<EntityType> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(EntityType type) {
        return types.add(type);
    }

    public boolean removeType(EntityType type) {
        return types.remove(type);
    }

    public void clearTypes() {
        types.clear();
    }

}
