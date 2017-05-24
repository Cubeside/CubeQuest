package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceQuest extends Quest {

    private HashSet<Material> types;
    private int amount;
    private HashMap<UUID, Integer> states;

    public BlockPlaceQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward);

        this.types = types == null? new HashSet<Material>() : new HashSet<Material>(types);
        this.amount = amount;
        this.states = new HashMap<UUID, Integer>();
        for (UUID p: getPlayersGivenTo()) {
            states.put(p, 0);
        }
    }

    public BlockPlaceQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        types.clear();
        List<String> typeList = yc.getStringList("types");
        for (String s: typeList) {
            types.add(Material.valueOf(s));
        }
        amount = yc.getInt("amount");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<String>();
        for (Material m: types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);
        yc.set("amount", amount);

        return super.serialize(yc);
    }

    @Override
    public boolean onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return false;
        }
        if (!types.contains(event.getBlock().getType())) {
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must be greater than 0");
        }
        this.amount = val;
    }

}
