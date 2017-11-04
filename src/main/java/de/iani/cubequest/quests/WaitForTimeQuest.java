package de.iani.cubequest.quests;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.event.player.PlayerQuitEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.WaitForTimeQuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@DelegateDeserialization(Quest.class)
public class WaitForTimeQuest extends Quest {

    private long ms;

    public WaitForTimeQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            long msToWait) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward, failReward);
        this.ms = msToWait;
    }

    public WaitForTimeQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            long msToWait) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null, msToWait);
    }

    public WaitForTimeQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.ms = yc.getLong("ms");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("ms", ms);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return ms > 0;
    }

    @Override
    public WaitForTimeQuestState createQuestState(UUID player) {
        return new WaitForTimeQuestState(CubeQuest.getInstance().getPlayerData(player), this.getId(), ms);
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
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zeitspanne: " + ChatAndTextUtil.formatTimespan(ms)).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public long getTime() {
        return ms;
    }

    /**
     * Setzt die zu wartende Zeit.
     * Betrifft keine Spieler, an die die Quest bereits vergeben wurde!
     * Deprecated, wenn isReady() true ist!
     * @param ms
     */
    public void setTime(long ms) {
        this.ms = ms;
        updateIfReal();
    }

}
