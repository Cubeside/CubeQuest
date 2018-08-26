package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.questStates.AmountQuestState;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class AmountQuest extends ProgressableQuest {
    
    private int amount;
    
    public AmountQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward);
        
        this.amount = amount;
    }
    
    public AmountQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.amount = yc.getInt("amount");
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("amount", this.amount);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return this.amount > 0;
    }
    
    @Override
    public AmountQuestState createQuestState(UUID id) {
        return getId() < 0 ? null
                : new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), getId());
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zu erreichende Anzahl: "
                + (this.amount > 0 ? ChatColor.GREEN : ChatColor.RED) + this.amount)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                "/" + SetQuestAmountCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public int getAmount() {
        return this.amount;
    }
    
    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must not be negative");
        }
        this.amount = val;
        updateIfReal();
    }
    
}
