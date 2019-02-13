package de.iani.cubequest.quests;

import de.iani.cubequest.commands.SetIgnoreOppositeCommand;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;


public abstract class SymmetricalMaterialsAndAmountQuest extends MaterialsAndAmountQuest {
    
    private boolean ignoreOpposite;
    
    public SymmetricalMaterialsAndAmountQuest(int id, String name, String displayMessage,
            Collection<Material> types, int amount) {
        super(id, name, displayMessage, types, amount);
    }
    
    public SymmetricalMaterialsAndAmountQuest(int id) {
        this(id, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.ignoreOpposite = yc.getBoolean("ignoreOpposite", false);
    }
    
    @Override
    public String serializeToString(YamlConfiguration yc) {
        yc.set("ignoreOpposite", this.ignoreOpposite);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        result.remove(result.size() - 1); // Remove blank line
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA
                + "Ignoriert entgegengesetzte Aktionen: "
                + (this.ignoreOpposite ? ChatColor.GREEN : ChatColor.GOLD) + this.ignoreOpposite)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                "/" + SetIgnoreOppositeCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public boolean isIgnoreOpposite() {
        return this.ignoreOpposite;
    }
    
    public void setIgnoreOpposite(boolean ignoreOpposite) {
        this.ignoreOpposite = ignoreOpposite;
        updateIfReal();
    }
    
}
