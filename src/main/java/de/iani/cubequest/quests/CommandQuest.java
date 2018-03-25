package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@DelegateDeserialization(Quest.class)
public class CommandQuest extends Quest {
    
    private String regex;
    private boolean caseSensitive;
    private Pattern pattern;
    
    private String overwrittenCommandName;
    
    public CommandQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, String regex, boolean caseSensitive) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward);
        
        this.caseSensitive = caseSensitive;
        setRegex(regex, false);
    }
    
    public CommandQuest(int id) {
        this(id, null, null, null, null, null, null, false);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.caseSensitive = yc.getBoolean("caseSensitive");
        this.overwrittenCommandName =
                yc.contains("overwrittenCommandName") ? yc.getString("overwrittenCommandName")
                        : null;
        
        setRegex(yc.getString("regex"), false);
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("regex", this.regex);
        yc.set("caseSensitive", this.caseSensitive);
        yc.set("overwrittenCommandName", this.overwrittenCommandName);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event,
            QuestState state) {
        String msg = event.getMessage().substring(1);
        if (this.pattern.matcher(msg).matches()) {
            onSuccess(event.getPlayer());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isLegal() {
        return this.pattern != null;
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Regulärer Ausdruck: "
                + (this.regex == null ? ChatColor.RED + "NULL" : ChatColor.GREEN + this.regex))
                        .create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Beachtet Groß-/Kleinschreibung: "
                + ChatColor.GREEN + this.caseSensitive).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        
        String commandDispatchedString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + getStateStringStartingToken(state) + " " + ChatColor.GOLD + getName())
                            .create());
            commandDispatchedString += Quest.INDENTION;
        } else {
            commandDispatchedString += getStateStringStartingToken(state) + " ";
        }
        
        commandDispatchedString +=
                ChatColor.DARK_AQUA + "Befehl " + getCommandName() + " eingegeben: ";
        commandDispatchedString +=
                state.getStatus().color + (state.getStatus() == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(commandDispatchedString).create());
        
        return result;
    }
    
    public String getRegex() {
        return this.regex;
    }
    
    public void setRegex(String val) {
        setRegex(val, true);
    }
    
    private void setRegex(String val, boolean updateInDB) {
        this.pattern = val == null ? null
                : this.caseSensitive ? Pattern.compile(val)
                        : Pattern.compile(val, Pattern.CASE_INSENSITIVE);
        this.regex = val;
        if (updateInDB) {
            updateIfReal();
        }
    }
    
    public void setLiteralMatch(String val) {
        setRegex(Pattern.quote(val));
    }
    
    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }
    
    public void setCaseSensitive(boolean val) {
        this.pattern = this.regex == null ? null
                : val ? Pattern.compile(this.regex)
                        : Pattern.compile(this.regex, Pattern.CASE_INSENSITIVE);
        this.caseSensitive = val;
        updateIfReal();
    }
    
    public String getCommandName() {
        return this.overwrittenCommandName == null ? "\"" + this.regex + "\""
                : this.overwrittenCommandName;
    }
    
    public void setCommandName(String name) {
        this.overwrittenCommandName = name;
        updateIfReal();
    }
    
}
