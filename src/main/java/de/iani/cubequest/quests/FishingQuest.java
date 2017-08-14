package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;

public class FishingQuest extends Quest {

    private HashSet<Material> types;
    private int amount;

    public FishingQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward);
        Verify.verify(amount >= 0);

        if (types == null) {
            this.types = new HashSet<Material>();
        } else {
            this.types = new HashSet<Material>(types);
        }
        this.amount = amount;
    }

    public FishingQuest(int id) {
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
    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item)) {
            return false;
        }
        Item item = (Item) event.getCaught();
        if (!types.contains(item.getItemStack().getType())) {
            return false;
        }
        PlayerData pData = CubeQuest.getInstance().getPlayerData(event.getPlayer());
        if (!pData.isGivenTo(this.getId())) {
            return false;
        }
        AmountQuestState state = (AmountQuestState) pData.getPlayerState(this.getId());
        if (state.getAmount()+1 >= amount) {
            onSuccess(event.getPlayer());
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

    public Set<Material> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(Material type) {
        if (types.add(type)) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
            return true;
        }
        return false;
    }

    public boolean removeType(Material type) {
        if (types.remove(type)) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
            return true;
        }
        return false;
    }

    public void clearTypes() {
        types.clear();
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must be greater than 0");
        }
        this.amount = val;
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

}
