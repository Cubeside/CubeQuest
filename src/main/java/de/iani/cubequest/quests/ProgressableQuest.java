package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public abstract class ProgressableQuest extends Quest {
    
    private List<QuestCondition> questProgressConditions;
    
    public ProgressableQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward);
        this.questProgressConditions = new ArrayList<>();
    }
    
    public ProgressableQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        this.questProgressConditions = new ArrayList<>();
    }
    
    public ProgressableQuest(int id) {
        super(id);
        this.questProgressConditions = new ArrayList<>();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        this.questProgressConditions = (List<QuestCondition>) yc.get("questProgressConditions",
                this.questProgressConditions);
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("questProgressConditions", this.questProgressConditions);
        return super.serializeToString(yc);
    }
    
    public List<QuestCondition> getQuestProgressConditions() {
        return Collections.unmodifiableList(this.questProgressConditions);
    }
    
    public boolean fullfillsProgressConditions(Player player, PlayerData data) {
        if (!isReady()) {
            return false;
        }
        
        if (data.getPlayerStatus(getId()) != Status.GIVENTO) {
            return false;
        }
        
        return this.questProgressConditions.stream().allMatch(qpc -> qpc.fullfills(player, data));
    }
    
    public boolean fullfillsProgressConditions(Player player) {
        return this.fullfillsProgressConditions(player,
                CubeQuest.getInstance().getPlayerData(player));
    }
    
    public void addQuestProgressCondition(QuestCondition qpc) {
        if (qpc == null) {
            throw new NullPointerException();
        }
        this.questProgressConditions.add(qpc);
        updateIfReal();
    }
    
    public void removeQuestProgressCondition(int questProgessConditionIndex) {
        this.questProgressConditions.remove(questProgessConditionIndex);
        updateIfReal();
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Fortschrittsbedingungen:"
                + (this.questProgressConditions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                        .create());
        for (int i = 0; i < this.questProgressConditions.size(); i++) {
            QuestCondition qgc = this.questProgressConditions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bedingung " + (i + 1) + ":")
                    .create());
            for (BaseComponent[] bc: qgc.getConditionInfo(true)) {
                result.add(new ComponentBuilder("  ").append(bc).create());
            }
        }
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }
    
}
