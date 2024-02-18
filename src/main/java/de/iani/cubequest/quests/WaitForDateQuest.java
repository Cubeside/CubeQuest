package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

@DelegateDeserialization(Quest.class)
public class WaitForDateQuest extends Quest {

    private long dateInMs;
    private boolean done = false;
    private TimerTask task = null;

    public WaitForDateQuest(int id, String name, String displayMessage, long dateInMs) {
        super(id, name, displayMessage);
        this.dateInMs = dateInMs;
    }

    public WaitForDateQuest(int id, String name, String displayMessage, Date date) {
        this(id, name, displayMessage, date.getTime());
    }

    public WaitForDateQuest(int id) {
        this(id, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.dateInMs = yc.getLong("dateInMs");

        // This quest might not yet be registered in the QuestManager. checkTime() checks this and
        // ignores the call if this is the case.
        checkTime();
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("dateInMs", this.dateInMs);

        return super.serializeToString(yc);
    }

    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);

        if (System.currentTimeMillis() >= this.dateInMs) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(getId())) {
                    onSuccess(player);
                }
            });
        }
    }

    @Override
    public boolean isLegal() {
        return this.dateInMs > 0;
    }

    @Override
    public void setReady(boolean val) {
        boolean before = isReady();
        super.setReady(val);

        if (before != isReady()) {
            checkTime();
        }
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Datum: "
                + (this.dateInMs > 0 ? ChatColor.GREEN : ChatColor.RED) + ChatAndTextUtil.formatDate(this.dateInMs))
                        .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                "/" + SetQuestDateOrTimeCommand.FULL_DATE_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    @Override
    public List<BaseComponent[]> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();

        String waitedForDateString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);

        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            waitedForDateString += Quest.INDENTION;
        } else {
            waitedForDateString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }

        waitedForDateString += ChatColor.DARK_AQUA + "Auf den " + ChatAndTextUtil.formatDate(getDate()) + " gewartet: ";
        waitedForDateString += status.color + (status == Status.SUCCESS ? "ja" : "nein");

        result.add(new ComponentBuilder(waitedForDateString).create());

        return result;
    }

    @Override
    public boolean afterPlayerJoinEvent(QuestState state) {
        if (this.done) {
            onSuccess(state.getPlayerData().getPlayer());
            return true;
        }
        return false;
    }

    public void checkTime() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }

        Quest other = QuestManager.getInstance().getQuest(getId());
        if (other != this) {
            return;
        }
        if (!isLegal()) {
            return;
        }

        this.done = System.currentTimeMillis() >= this.dateInMs;
        if (!isReady()) {
            return;
        }

        if (!this.done) {
            this.task = new TimerTask() {

                @Override
                public void run() {
                    Bukkit.getScheduler().runTask(CubeQuest.getInstance(), () -> checkTime());
                }
            };
            CubeQuest.getInstance().getTimer().schedule(this.task, getDate());
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(getId())) {
                    onSuccess(player);
                }
            }
        }
    }

    public long getDateMs() {
        return this.dateInMs;
    }

    public Date getDate() {
        return new Date(this.dateInMs);
    }

    public void setDate(long ms) {
        this.dateInMs = ms;
        updateIfReal();
        checkTime();
    }

    public void setDate(Date date) {
        setDate(date.getTime());
    }

    public boolean isDone() {
        return this.done;
    }

    @Override
    public void onDeletion(boolean cascading) throws QuestDeletionFailedException {
        super.onDeletion(cascading);
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

}
