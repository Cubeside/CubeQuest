package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.conditions.GameModeCondition;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public abstract class ProgressableQuest extends Quest {
    
    private List<QuestCondition> questProgressConditions;
    private List<QuestCondition> visibleProgressConditions;
    
    public ProgressableQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward);
        init();
    }
    
    public ProgressableQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        init();
    }
    
    public ProgressableQuest(int id) {
        super(id);
        init();
    }
    
    private void init() {
        this.questProgressConditions = new ArrayList<>();
        this.visibleProgressConditions = new ArrayList<>();
        addQuestProgressCondition(new GameModeCondition(false, GameMode.SURVIVAL), false);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        this.questProgressConditions = (List<QuestCondition>) yc.get("questProgressConditions",
                this.questProgressConditions);
        this.visibleProgressConditions.clear();
        for (QuestCondition cond: this.questProgressConditions) {
            if (cond.isVisible()) {
                this.visibleProgressConditions.add(cond);
            }
        }
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("questProgressConditions", this.questProgressConditions);
        return super.serializeToString(yc);
    }
    
    public List<QuestCondition> getQuestProgressConditions() {
        return Collections.unmodifiableList(this.questProgressConditions);
    }
    
    public boolean fulfillsProgressConditions(Player player, PlayerData data) {
        if (!isReady()) {
            return false;
        }
        
        if (data.getPlayerStatus(getId()) != Status.GIVENTO) {
            return false;
        }
        
        return this.questProgressConditions.stream().allMatch(qpc -> qpc.fulfills(player, data));
    }
    
    public boolean fulfillsProgressConditions(Player player) {
        return this.fulfillsProgressConditions(player,
                CubeQuest.getInstance().getPlayerData(player));
    }
    
    public void addQuestProgressCondition(QuestCondition qpc) {
        this.addQuestProgressCondition(qpc, true);
    }
    
    protected void addQuestProgressCondition(QuestCondition qpc, boolean updateDatabase) {
        if (qpc == null) {
            throw new NullPointerException();
        }
        this.questProgressConditions.add(qpc);
        
        if (updateDatabase) {
            updateIfReal();
        }
        
        if (qpc.isVisible()) {
            this.visibleProgressConditions.add(qpc);
        }
    }
    
    public void removeQuestProgressCondition(int questProgessConditionIndex) {
        QuestCondition cond = this.questProgressConditions.remove(questProgessConditionIndex);
        updateIfReal();
        if (cond.isVisible()) {
            this.visibleProgressConditions.remove(cond);
        }
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Fortschrittsbedingungen:"
                + (this.questProgressConditions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                        .create());
        for (int i = 0; i < this.questProgressConditions.size(); i++) {
            QuestCondition qpc = this.questProgressConditions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bedingung " + (i + 1)
                    + (qpc.isVisible() ? "" : " (unsichtbar)") + ": ")
                            .append(qpc.getConditionInfo(true)).create());
        }
        result.add(new ComponentBuilder("").create());
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        Player player = data.getPlayer();
        
        List<BaseComponent[]> result = getSpecificStateInfoInternal(data, indentionLevel);
        if (this.visibleProgressConditions.isEmpty()) {
            return result;
        }
        
        String conditionsMetString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1)
                + ChatColor.DARK_AQUA + "Dabei folgende "
                + (this.visibleProgressConditions.size() == 1 ? "Bedingung" : "Bedingungen")
                + " eingehalten:";
        result.add(new ComponentBuilder(conditionsMetString).create());
        
        for (QuestCondition cond: this.visibleProgressConditions) {
            result.add(new ComponentBuilder(
                    ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1))
                            .append(ChatAndTextUtil.getTrueFalseToken(
                                    player == null ? null : cond.fulfills(player, data)))
                            .append(" ")
                            .append(ChatAndTextUtil.stripEvents(cond.getConditionInfo())).create());
        }
        
        return result;
        
    }
    
    protected abstract List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data,
            int indentionLevel);
    
}
