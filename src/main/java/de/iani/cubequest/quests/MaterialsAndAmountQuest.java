package de.iani.cubequest.quests;

import de.iani.cubequest.Reward;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class MaterialsAndAmountQuest extends EconomyInfluencingAmountQuest {
    
    private Set<Material> types;
    
    public MaterialsAndAmountQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Collection<Material> types, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, amount);
        
        this.types = types == null ? EnumSet.noneOf(Material.class) : EnumSet.copyOf(types);
    }
    
    public MaterialsAndAmountQuest(int id) {
        this(id, null, null, null, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.types.clear();
        List<String> typeList = yc.getStringList("types");
        for (String s : typeList) {
            this.types.add(Material.valueOf(s));
        }
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<>();
        for (Material m : this.types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return super.isLegal() && !this.types.isEmpty();
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        String typesString = ChatColor.DARK_AQUA + "Erlaubte Materialien: ";
        if (this.types.isEmpty()) {
            typesString += ChatColor.RED + "Keine";
        } else {
            typesString += ChatColor.GREEN;
            List<Material> typeList = new ArrayList<>(this.types);
            typeList.sort((e1, e2) -> e1.name().compareTo(e2.name()));
            for (Material type : typeList) {
                typesString += type.name() + ", ";
            }
            typesString = typesString.substring(0, typesString.length() - ", ".length());
        }
        
        result.add(new ComponentBuilder(typesString)
                .event(new ClickEvent(Action.SUGGEST_COMMAND,
                        "/" + AddOrRemoveMaterialCommand.FULL_ADD_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public Set<Material> getTypes() {
        return Collections.unmodifiableSet(this.types);
    }
    
    public boolean addType(Material type) {
        if (this.types.add(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public boolean removeType(Material type) {
        if (this.types.remove(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public void clearTypes() {
        this.types.clear();
        updateIfReal();
    }
    
}
