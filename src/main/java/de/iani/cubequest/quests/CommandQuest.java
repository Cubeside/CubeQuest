package de.iani.cubequest.quests;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CommandQuest extends Quest {

    private String regex;
    private boolean caseSensitive;
    private Pattern pattern;

    public CommandQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            String regex, boolean caseSensitive) {
        super(id, name, giveMessage, successMessage, successReward);

        this.caseSensitive = caseSensitive;
        setRegex(regex, false);
    }

    public CommandQuest(int id) {
        this(id, null, null, null, null, null, false);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        caseSensitive = yc.getBoolean("caseSensitive");

        setRegex(yc.getString("regex"), false);
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("regex", regex);
        yc.set("caseSensitive", caseSensitive);

        return super.serialize(yc);
    }

    @Override
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event, QuestState state) {
        String msg = event.getMessage().substring(1);
        if (pattern.matcher(msg).matches()) {
            onSuccess(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean isLegal() {
        return pattern != null;
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Regulärer Ausdruck: " + (regex == null? ChatColor.RED + "NULL" : ChatColor.GREEN + regex)).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Beachtet Groß-/Kleinschreibung: " + ChatColor.GREEN + caseSensitive).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String val) {
        setRegex(val, true);
    }

    private void setRegex(String val, boolean updateInDB) {
        pattern = val == null? null : caseSensitive? Pattern.compile(val) : Pattern.compile(val, Pattern.CASE_INSENSITIVE);
        this.regex = val;
        if (updateInDB) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }

    public void setLiteralMatch(String val) {
        setRegex(Pattern.quote(val));
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean val) {
        pattern = regex == null? null : val? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.caseSensitive = val;
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

}
