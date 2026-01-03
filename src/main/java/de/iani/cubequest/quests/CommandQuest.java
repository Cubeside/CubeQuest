package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetCancelCommandCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.commands.SetQuestRegexCommand;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@DelegateDeserialization(Quest.class)
public class CommandQuest extends ProgressableQuest {

    private String regex;
    private boolean caseSensitive;
    private Pattern pattern;

    private boolean cancelCommand;

    private Component overwrittenCommandName;

    public CommandQuest(int id, String name, Component displayMessage, String regex, boolean caseSensitive) {
        super(id, name, displayMessage);

        this.caseSensitive = caseSensitive;
        this.cancelCommand = false;
        setRegex(regex, false);
    }

    public CommandQuest(int id) {
        this(id, null, null, null, false);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.caseSensitive = yc.getBoolean("caseSensitive");
        this.cancelCommand = yc.getBoolean("cancelCommand", false);
        this.overwrittenCommandName = getComponentOrConvert(yc, "overwrittenCommandName");

        setRegex(yc.getString("regex"), false);
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("regex", this.regex);
        yc.set("caseSensitive", this.caseSensitive);
        yc.set("cancelCommand", this.cancelCommand);
        yc.set("overwrittenCommandName", this.overwrittenCommandName);

        return super.serializeToString(yc);
    }

    @Override
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event, QuestState state) {
        if (!this.fulfillsProgressConditions(event.getPlayer(), state.getPlayerData())) {
            return false;
        }

        String msg = event.getMessage().substring(1);
        if (this.pattern.matcher(msg).matches()) {
            onSuccess(event.getPlayer());
            if (this.cancelCommand) {
                event.setCancelled(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isLegal() {
        return this.pattern != null;
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        Component regexComponent = (this.regex == null) ? Component.text("NULL", NamedTextColor.RED)
                : Component.text(this.regex, NamedTextColor.GREEN);

        result.add(suggest(Component.text("Regulärer Ausdruck: ", NamedTextColor.DARK_AQUA).append(regexComponent),
                SetQuestRegexCommand.FULL_QUOTE_COMMAND));

        result.add(Component.text("Beachtet Groß-/Kleinschreibung: " + this.caseSensitive, NamedTextColor.DARK_AQUA)
                .hoverEvent(HoverEvent.showText(Component.text("(kein Befehl verfügbar)"))));

        result.add(suggest(
                Component.text("Blockiert Befehl: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(String.valueOf(this.cancelCommand), NamedTextColor.GREEN)),
                SetCancelCommandCommand.FULL_COMMAND));

        TextColor nameStatusColor = (this.overwrittenCommandName == null) ? NamedTextColor.GOLD : NamedTextColor.GREEN;
        String nameStatusText = (this.overwrittenCommandName == null) ? "(automatisch)" : "(gesetzt)";

        result.add(suggest(
                Component.text("Bezeichnung: ", NamedTextColor.DARK_AQUA)
                        .append(getCommandName().colorIfAbsent(NamedTextColor.GREEN)).append(Component.text(" "))
                        .append(Component.text(nameStatusText, nameStatusColor)),
                SetOverwrittenNameForSthCommand.SpecificSth.COMMAND.fullSetCommand));

        result.add(Component.empty());
        return result;
    }

    @Override
    public List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        QuestState state = data.getPlayerState(getId());
        Status status = (state == null) ? Status.NOTGIVENTO : state.getStatus();

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            result.add(baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "))
                    .append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD)).color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "));
        }

        result.add(prefix.append(Component.text("Befehl ")).append(getCommandName())
                .append(Component.text(" eingegeben: "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color))
                .color(NamedTextColor.DARK_AQUA));

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
                : this.caseSensitive ? Pattern.compile(val) : Pattern.compile(val, Pattern.CASE_INSENSITIVE);
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
                : val ? Pattern.compile(this.regex) : Pattern.compile(this.regex, Pattern.CASE_INSENSITIVE);
        this.caseSensitive = val;
        updateIfReal();
    }

    public boolean isCancelCommand() {
        return this.cancelCommand;
    }

    public void setCancelCommand(boolean val) {
        this.cancelCommand = val;
        updateIfReal();
    }

    public Component getCommandName() {
        return this.overwrittenCommandName == null ? Component.text("\"" + this.regex + "\"")
                : this.overwrittenCommandName;
    }

    public void setCommandName(Component name) {
        this.overwrittenCommandName = name;
        updateIfReal();
    }

}
