package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
        Verify.verify(amount >= 0);

        if (types == null) {
            this.types = new HashSet<Material>();
        } else {
            this.types = new HashSet<Material>(types);
        }
        this.amount = amount;
        this.states = new HashMap<UUID, Integer>();
        for (UUID p: getPlayersGivenTo()) {
            states.put(p, 0);
        }
    }

    public FishingQuest(String name) {
        this(name, null, null, null, null, 0);
    }

    @Override
    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item)) {
            return false;
        }
        Item item = (Item) event.getCaught();
        if (!types.contains(item.getItemStack().getType())) {
            return false;
        }
        if (getPlayerStatus(event.getPlayer().getUniqueId()) != Status.GIVENTO) {
            return false;
        }
        if (states.get(event.getPlayer().getUniqueId())+1 >= amount) {
            onSuccess(event.getPlayer());
        } else {
            states.put(event.getPlayer().getUniqueId(), states.get(event.getPlayer().getUniqueId()) + 1);
        }
        return true;
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

    public Set<Material> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(Material type) {
        return types.add(type);
    }

    public boolean removeType(Material type) {
        return types.remove(type);
    }

    public void clearTypes() {
        types.clear();
    }

}
