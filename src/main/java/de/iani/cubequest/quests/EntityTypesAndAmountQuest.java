package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class EntityTypesAndAmountQuest extends AmountQuest {

    private Set<EntityType> types;

    public EntityTypesAndAmountQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<EntityType> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, amount);

        this.types = (types == null)? EnumSet.noneOf(EntityType.class) : EnumSet.copyOf(types);
    }

    public EntityTypesAndAmountQuest(int id) {
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
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<String>();
        for (EntityType m: types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && !types.isEmpty();
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return this.getId() < 0? null :  new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), this.getId());
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        String typesString = ChatColor.DARK_AQUA + "Erlaubte Entity-Typen: ";
        if (types.isEmpty()) {
            typesString += ChatColor.RED + "Keine";
        } else {
            typesString += ChatColor.GREEN;
            List<EntityType> typeList = new ArrayList<EntityType>(types);
            typeList.sort((e1, e2) -> e1.name().compareTo(e2.name()));
            for (EntityType type: typeList) {
                typesString += type.name() + ", ";
            }
            typesString = typesString.substring(0, typesString.length() - ", ".length());
        }

        result.add(new ComponentBuilder(typesString).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public Set<EntityType> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(EntityType type) {
        if (types.add(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeType(EntityType type) {
        if (types.remove(type)) {
            if (this.getId() > 0) {
                CubeQuest.getInstance().getQuestCreator().updateQuest(this);
            }
            return true;
        }
        return false;
    }

    public void clearTypes() {
        types.clear();
        updateIfReal();
    }

}
