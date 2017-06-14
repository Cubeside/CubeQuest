package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState.Status;

public class KillEntitiesQuest extends Quest {

    private HashSet<EntityType> types;
    private int amount;

    public KillEntitiesQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, Collection<EntityType> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward);

        this.types = (types == null)? new HashSet<EntityType>() : new HashSet<EntityType>(types);
        this.amount = amount;
    }

    public KillEntitiesQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        types.clear();
        List<String> typeList = yc.getStringList("types");
        for (String s: typeList) {
            types.add(EntityType.valueOf(s));
        }
        amount = yc.getInt("amount");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<String>();
        for (EntityType m: types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);
        yc.set("amount", amount);

        return super.serialize(yc);
    }

    @Override
    public boolean onEntityDeathEvent(EntityDeathEvent event) {
        if (!types.contains(event.getEntityType())) {
            return false;
        }
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return false;
        }
        AmountQuestState state = (AmountQuestState) CubeQuest.getInstance().getPlayerData(player).getPlayerState(this.getId());
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }
        if (state.getAmount()+1 >= amount) {
            onSuccess(player);
        } else {
            state.changeAmount(1);
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return !types.isEmpty() && amount > 0;
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), this.getId());
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
