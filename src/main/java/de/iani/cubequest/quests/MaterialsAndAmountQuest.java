package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.Reward;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class MaterialsAndAmountQuest extends AmountQuest {

    private Set<Material> types;

    public MaterialsAndAmountQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Collection<Material> types, int amount) {
        super(id, name, giveMessage, successMessage, successReward, amount);

        this.types = types == null? EnumSet.noneOf(Material.class) : EnumSet.copyOf(types);
    }

    public MaterialsAndAmountQuest(int id) {
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
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<String>();
        for (Material m: types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);

        return super.serialize(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && !types.isEmpty();
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        String typesString = ChatColor.DARK_AQUA + "Erlaubte Materialien: ";
        if (types.isEmpty()) {
            typesString += ChatColor.RED + "Keine";
        } else {
            typesString += ChatColor.GREEN;
            List<Material> typeList = new ArrayList<Material>(types);
            typeList.sort((e1, e2) -> e1.name().compareTo(e2.name()));
            for (Material type: typeList) {
                typesString += type.name() + ", ";
            }
            typesString = typesString.substring(0, typesString.length() - ", ".length());
        }

        result.add(new ComponentBuilder(typesString).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public Set<Material> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    public boolean addType(Material type) {
        if (types.add(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeType(Material type) {
        if (types.remove(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearTypes() {
        types.clear();
        updateIfReal();
    }

}
