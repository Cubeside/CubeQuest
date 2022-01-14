package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.AddOrRemoveStatisticCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesidestats.api.event.PlayerStatisticUpdatedEvent;
import de.iani.cubesideutils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class IncreaseStatisticQuest extends AmountQuest {
    
    private Set<StatisticKey> statisticKeys;
    private String overwrittenStatisticsString;
    
    public IncreaseStatisticQuest(int id, String name, String displayMessage, Set<StatisticKey> statisticKeys,
            int amount) {
        super(id, name, displayMessage, amount);
        
        this.statisticKeys = new LinkedHashSet<>(statisticKeys);
    }
    
    public IncreaseStatisticQuest(int id) {
        this(id, null, null, Collections.emptySet(), 0);
    }
    
    @Override
    public boolean onPlayerStatisticUpdatedEvent(PlayerStatisticUpdatedEvent event, QuestState state) {
        if (!getStatisticKeys().contains(event.getStatistic())) {
            return false;
        }
        
        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if (!this.fulfillsProgressConditions(player, state.getPlayerData())) {
            return false;
        }
        
        int previous = event.hasPreviousValueAllTime() ? event.getPreviousValueAllTime() : 0;
        AmountQuestState amountState = (AmountQuestState) state;
        amountState.changeAmount(event.getValueAllTime() - previous);
        if (amountState.getAmount() >= getAmount()) {
            onSuccess(player);
        }
        return true;
    }
    
    @Override
    protected List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        AmountQuestState state = (AmountQuestState) data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        ComponentBuilder stepsMadeBuilder =
                new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel));
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            stepsMadeBuilder.append(Quest.INDENTION);
        } else {
            stepsMadeBuilder.append(ChatAndTextUtil.getStateStringStartingToken(state) + " ");
        }
        
        stepsMadeBuilder.append("" + ChatColor.DARK_AQUA).append(TextComponent.fromLegacyText(getStatisticsString()))
                .append(": ");
        stepsMadeBuilder.append(status.color + "" + (state == null ? 0 : state.getAmount()) + "" + ChatColor.DARK_AQUA
                + " / " + getAmount());
        
        result.add(stepsMadeBuilder.create());
        
        return result;
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        List<String> keyList = yc.getStringList("statisticKeys");
        this.statisticKeys.clear();
        for (String s : keyList) {
            StatisticKey key = CubeQuest.getInstance().getCubesideStatistics().getStatisticKey(s, false);
            if (key == null) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "StatisticKey with name \"" + s
                        + "\" was missing for quest " + toString() + "! Now removed from the quest.");
                continue;
            }
            
            this.statisticKeys.add(key);
        }
        
        this.overwrittenStatisticsString = yc.getString("overwrittenStatisticsString");
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("statisticKeys", this.statisticKeys.stream().map(StatisticKey::getName).collect(Collectors.toList()));
        yc.set("overwrittenStatisticsString", this.overwrittenStatisticsString);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return super.isLegal() && !this.statisticKeys.isEmpty();
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        String statisticsString = ChatColor.DARK_AQUA + "Erlaubte Statistiken: ";
        if (this.statisticKeys.isEmpty()) {
            statisticsString += ChatColor.RED + "Keine";
        } else {
            statisticsString += ChatColor.GREEN;
            List<StatisticKey> keyList = new ArrayList<>(this.statisticKeys);
            keyList.sort((e1, e2) -> e1.getName().compareTo(e2.getName()));
            for (StatisticKey key : keyList) {
                statisticsString += key.getName() + ", ";
            }
            statisticsString = statisticsString.substring(0, statisticsString.length() - ", ".length());
        }
        
        result.add(new ComponentBuilder(statisticsString)
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + AddOrRemoveStatisticCommand.FULL_ADD_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Statistiken-Beschreibung: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND,
                        "/" + SetOverwrittenNameForSthCommand.SpecificSth.STATISTIC.fullSetCommand))
                .event(SUGGEST_COMMAND_HOVER_EVENT).append("" + ChatColor.GREEN)
                .append(TextComponent.fromLegacyText(getStatisticsString()))
                .append(" " + (this.overwrittenStatisticsString == null ? ChatColor.GOLD + "(automatisch)"
                        : ChatColor.GREEN + "(gesetzt)"))
                .create());
        
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public Set<StatisticKey> getStatisticKeys() {
        return Collections.unmodifiableSet(this.statisticKeys);
    }
    
    public boolean addStatistic(StatisticKey key) {
        if (this.statisticKeys.add(key)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public boolean removeStatistic(StatisticKey key) {
        if (this.statisticKeys.remove(key)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public void clearStatistics() {
        this.statisticKeys.clear();
        updateIfReal();
    }
    
    public String getStatisticsString() {
        if (this.overwrittenStatisticsString != null) {
            return this.overwrittenStatisticsString;
        }
        
        return ChatColor.DARK_AQUA + StringUtil.replaceLast(
                getStatisticKeys().stream().map(StatisticKey::getDisplayName).collect(Collectors.joining(", ")), ", ",
                " und/oder ") + " erhöht";
    }
    
    public void setStatisticsString(String string) {
        this.overwrittenStatisticsString = string;
        updateIfReal();
    }
    
}
