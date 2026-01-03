package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.questStates.WaitForTimeQuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerQuitEvent;

@DelegateDeserialization(Quest.class)
public class WaitForTimeQuest extends Quest {

    private long ms;

    public WaitForTimeQuest(int id, String name, Component displayMessage, long msToWait) {
        super(id, name, displayMessage);
        this.ms = msToWait;
    }


    public WaitForTimeQuest(int id) {
        this(id, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.ms = yc.getLong("ms");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("ms", this.ms);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return this.ms > 0;
    }

    @Override
    public WaitForTimeQuestState createQuestState(UUID player) {
        return new WaitForTimeQuestState(CubeQuest.getInstance().getPlayerData(player), getId(), this.ms);
    }

    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        return ((WaitForTimeQuestState) state).checkTime();
    }

    @Override
    public boolean onPlayerQuitEvent(PlayerQuitEvent event, QuestState state) {
        ((WaitForTimeQuestState) state).playerLeft();
        return false;
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                Component.text("Zeitspanne: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(ChatAndTextUtil.formatTimespan(this.ms))),
                SetQuestDateOrTimeCommand.FULL_TIME_COMMAND));
        result.add(Component.empty());

        return result;
    }

    @SuppressWarnings("null")
    @Override
    public List<Component> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        WaitForTimeQuestState state = (WaitForTimeQuestState) data.getPlayerState(getId());
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

        long waitedMs = (status == Status.NOTGIVENTO) ? 0L
                : Math.min(this.ms, System.currentTimeMillis() - (state.getGoal() - this.ms));

        String waited =
                ChatAndTextUtil.formatTimespan(waitedMs, " Tage", " Stunden", " Minuten", " Sekunden", ", ", " und ");
        String total =
                ChatAndTextUtil.formatTimespan(this.ms, " Tage", " Stunden", " Minuten", " Sekunden", ", ", " und ");

        Component line =
                prefix.append(Component.text("Zeit gewartet: ")).append(Component.text(waited).color(status.color))
                        .append(Component.text(" / " + total)).color(NamedTextColor.DARK_AQUA);

        result.add(line);
        return result;
    }

    public long getTime() {
        return this.ms;
    }

    /**
     * Setzt die zu wartende Zeit. Betrifft keine Spieler, an die die Quest bereits vergeben wurde!
     * Deprecated, wenn isReady() true ist!
     * 
     * @param ms
     */
    public void setTime(long ms) {
        this.ms = ms;
        updateIfReal();
    }

}
