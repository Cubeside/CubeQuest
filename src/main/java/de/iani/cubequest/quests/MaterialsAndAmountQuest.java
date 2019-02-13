package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import de.iani.cubequest.generation.MaterialCombination;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class MaterialsAndAmountQuest extends EconomyInfluencingAmountQuest {
    
    private MaterialCombination types;
    
    public MaterialsAndAmountQuest(int id, String name, String displayMessage,
            Collection<Material> types, int amount) {
        super(id, name, displayMessage, amount);
        
        this.types = types == null ? new MaterialCombination() : new MaterialCombination(types);
    }
    
    public MaterialsAndAmountQuest(int id) {
        this(id, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        Object typesObject = yc.get("types");
        if (typesObject instanceof MaterialCombination) {
            this.types = new MaterialCombination((MaterialCombination) typesObject);
        } else {
            this.types.clear();
            List<String> typeList = yc.getStringList("types");
            for (String s : typeList) {
                Material mat = Material.getMaterial(s, true);
                if (mat == null) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                            "Material with name \"" + s + "\" could not be converted for quest "
                                    + toString() + "! Now removed from the quest.");
                    continue;
                }
                
                this.types.add(mat);
            }
        }
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("types", this.types);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return super.isLegal() && this.types.isLegal();
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        String typesString = ChatColor.DARK_AQUA + "Erlaubte Materialien: ";
        if (this.types.isEmpty()) {
            typesString += ChatColor.RED + "Keine";
        } else {
            typesString += ChatColor.GREEN;
            List<Material> typeList = new ArrayList<>(this.types.getContent());
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
        return this.types.getContent();
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
