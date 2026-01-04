package de.iani.cubequest.quests;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.commands.AddConditionCommand;
import de.iani.cubequest.commands.AddEditMoveOrRemoveActionCommand.ActionTime;
import de.iani.cubequest.commands.AssistedSubCommand;
import de.iani.cubequest.commands.RemoveConditionCommand;
import de.iani.cubequest.commands.SetAllowGiveBackCommand;
import de.iani.cubequest.commands.SetAllowRetryCommand;
import de.iani.cubequest.commands.SetAutoGivingCommand;
import de.iani.cubequest.commands.SetAutoRemoveCommand;
import de.iani.cubequest.commands.SetOrAppendDisplayMessageCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.commands.SetQuestNameCommand;
import de.iani.cubequest.commands.SetQuestVisibilityCommand;
import de.iani.cubequest.commands.ToggleReadyStatusCommand;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.events.QuestDeleteEvent;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestFreezeEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSetReadyEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldFailEvent;
import de.iani.cubequest.events.QuestWouldFreezeEvent;
import de.iani.cubequest.events.QuestWouldSucceedEvent;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.event.PlayerStatisticUpdatedEvent;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Quest implements ConfigurationSerializable {

    public static final Comparator<Quest> QUEST_DISPLAY_COMPARATOR =
            Comparator.comparing(Quest::getDisplayName, ComponentUtilAdventure.TEXT_ONLY_CASE_INSENSITIVE_ORDER)
                    .thenComparing(Quest::getId);
    public static final Comparator<Quest> QUEST_LIST_COMPARATOR = (q1, q2) -> q1.getId() - q2.getId();

    protected static final Component INDENTION = textOfChildren(space(), space(), space());
    protected static final HoverEvent<Component> SUGGEST_COMMAND_HOVER_EVENT =
            HoverEvent.showText(text("Befehl einfügen"));

    private int id;
    private String internalName;
    private Component displayName;
    private Component displayMessage;
    private Component overwrittenStateMessage;

    private List<QuestAction> giveActions;
    private List<QuestAction> successActions;
    private List<QuestAction> failActions;

    private RetryOption allowRetryOnSuccess;
    private RetryOption allowRetryOnFail;

    private boolean allowGiveBack;
    private long autoRemoveMs;

    private boolean visible;

    private boolean ready;

    private boolean delayDatabaseUpdate = false;

    private List<QuestCondition> questGivingConditions;
    private List<QuestCondition> visibleGivingConditions;

    public enum RetryOption {

        DENY_RETRY(false), ALLOW_RETRY(true), AUTO_RETRY(true);

        public final boolean allow;

        public static RetryOption match(String s) {
            String u = s.toUpperCase();
            String l = s.toLowerCase();
            try {
                return valueOf(u);
            } catch (IllegalArgumentException e) {
                // ignore
            }

            if (u.startsWith("DENY") || AssistedSubCommand.FALSE_STRINGS.contains(l)) {
                return DENY_RETRY;
            }
            if (u.startsWith("ALLOW") || AssistedSubCommand.TRUE_STRINGS.contains(l)) {
                return ALLOW_RETRY;
            }
            if (u.startsWith("AUTO")) {
                return AUTO_RETRY;
            }

            return null;
        }

        private RetryOption(boolean allow) {
            this.allow = allow;
        }
    }

    public Quest(int id, Component displayName, Component displayMessage) {
        Verify.verify(id != 0);

        this.id = id;
        this.internalName = displayName == null ? "" : ComponentUtilAdventure.rawText(displayName);
        this.displayMessage = displayMessage;
        this.giveActions = new ArrayList<>();
        this.successActions = new ArrayList<>();
        this.failActions = new ArrayList<>();
        this.allowRetryOnSuccess = RetryOption.DENY_RETRY;
        this.allowRetryOnFail = RetryOption.DENY_RETRY;
        this.allowGiveBack = false;
        this.autoRemoveMs = -1;
        this.visible = false;
        this.ready = false;
        this.questGivingConditions = new ArrayList<>();
        this.visibleGivingConditions = new ArrayList<>();
    }

    public Quest(int id) {
        this(id, null, null);
    }

    public static Quest deserialize(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            int questId = (Integer) serialized.get("id");
            String serializedString = (String) serialized.get("serialized");
            return CubeQuest.getInstance().getQuestCreator().create(questId, serializedString);
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    /**
     * Erzeugt eine neue YamlConfiguration aus dem String und ruft dann
     * {@link Quest#deserialize(YamlConfigration)} auf.
     * 
     * @param serialized serialisierte Quest
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public final void deserialize(String serialized) throws InvalidConfigurationException {
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        deserialize(yc);
    }

    /**
     * Wendet den Inhalt der YamlConfiguration auf die Quest an.
     * 
     * @param yc serialisierte Quest-Daten
     * @throws InvalidConfigurationException wird weitergegeben
     */
    @SuppressWarnings("unchecked")
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        if (!yc.getString("type").equals(QuestType.getQuestType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }

        String newName = yc.getString("name");
        if (!this.internalName.equals(newName)) {
            QuestRenameEvent event = new QuestRenameEvent(this, this.internalName, newName);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                // Reset name on other servers
                Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> updateIfReal(), 1L);
            } else {
                this.internalName = newName;
            }
        } else {
            this.internalName = newName;
        }

        this.displayName = ChatAndTextUtil.getComponentOrConvert(yc, "displayName");
        this.displayMessage = ChatAndTextUtil.getComponentOrConvert(yc, "displayMessage");
        this.overwrittenStateMessage = ChatAndTextUtil.getComponentOrConvert(yc, "overwrittenStateMessage");

        this.giveActions = (List<QuestAction>) yc.getList("giveActions", this.giveActions);
        this.successActions = (List<QuestAction>) yc.getList("successActions", this.successActions);
        this.failActions = (List<QuestAction>) yc.getList("failActions", this.failActions);

        this.allowRetryOnSuccess = RetryOption.valueOf(yc.getString("allowRetryOnSuccess", "DENY_RETRY"));
        this.allowRetryOnFail = RetryOption.valueOf(yc.getString("allowRetryOnFail", "DENY_RETRY"));

        this.allowGiveBack = yc.getBoolean("allowGiveBack", false);
        this.autoRemoveMs = yc.getLong("autoRemoveMs", -1);

        this.visible = yc.contains("visible") ? yc.getBoolean("visible") : false;
        this.ready = yc.getBoolean("ready");
        this.questGivingConditions = (List<QuestCondition>) yc.get("questGivingConditions", this.questGivingConditions);
        this.visibleGivingConditions.clear();
        for (QuestCondition cond : this.questGivingConditions) {
            if (cond.isVisible()) {
                this.visibleGivingConditions.add(cond);
            }
        }
    }

    @Override
    public final Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", getId());
        result.put("serialized", serializeToString());
        return result;
    }

    /**
     * Serialisiert die Quest
     * 
     * @return serialisierte Quest
     */
    public final String serializeToString() {
        return serializeToString(new YamlConfiguration());
    }

    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der
     * Oberklasse aufrufen.
     * 
     * @param yc YamlConfiguration mit den Daten der Quest
     * @return serialisierte Quest
     */
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("type", QuestType.getQuestType(this.getClass()).toString());
        yc.set("name", this.internalName);
        yc.set("displayName", SerializableAdventureComponent.ofOrNull(this.displayName));
        yc.set("displayMessage", SerializableAdventureComponent.ofOrNull(this.displayMessage));
        yc.set("overwrittenStateMessage", SerializableAdventureComponent.ofOrNull(this.overwrittenStateMessage));
        yc.set("giveActions", this.giveActions);
        yc.set("successActions", this.successActions);
        yc.set("failActions", this.failActions);
        yc.set("allowRetryOnSuccess", this.allowRetryOnSuccess.name());
        yc.set("allowRetryOnFail", this.allowRetryOnFail.name());
        yc.set("allowGiveBack", this.allowGiveBack);
        yc.set("autoRemoveMs", this.autoRemoveMs);
        yc.set("visible", this.visible);
        yc.set("ready", this.ready);
        yc.set("questGivingConditions", this.questGivingConditions);

        return yc.saveToString();
    }

    public boolean performDataUpdate() {
        boolean changed = false;

        for (int i = 0; i < this.giveActions.size(); i++) {
            QuestAction action = this.giveActions.get(i);
            QuestAction updated = action.performDataUpdate();
            if (action == updated) {
                continue;
            }
            this.giveActions.set(i, updated);
            changed = true;
        }
        for (int i = 0; i < this.successActions.size(); i++) {
            QuestAction action = this.successActions.get(i);
            QuestAction updated = action.performDataUpdate();
            if (action == updated) {
                continue;
            }
            this.successActions.set(i, updated);
            changed = true;
        }
        for (int i = 0; i < this.failActions.size(); i++) {
            QuestAction action = this.failActions.get(i);
            QuestAction updated = action.performDataUpdate();
            if (action == updated) {
                continue;
            }
            this.failActions.set(i, updated);
            changed = true;
        }

        boolean conditionsChanged = false;
        for (int i = 0; i < this.questGivingConditions.size(); i++) {
            QuestCondition condition = this.questGivingConditions.get(i);
            QuestCondition updated = condition.performDataUpdate();
            if (condition == updated) {
                continue;
            }
            this.questGivingConditions.set(i, updated);
            conditionsChanged = true;
        }

        if (conditionsChanged) {
            this.visibleGivingConditions.clear();
            for (QuestCondition cond : this.questGivingConditions) {
                if (cond.isVisible()) {
                    this.visibleGivingConditions.add(cond);
                }
            }
            changed = true;
        }

        return changed;
    }

    public final int getId() {
        return this.id;
    }

    public final boolean isReal() {
        return this.id > 0;
    }

    public String getTypeName() {
        return QuestType.getQuestType(this.getClass()).toString();
    }

    public String getInternalName() {
        return this.internalName;
    }

    public void setInternalName(String val) {
        val = val == null ? "" : val;

        if (this.id < 0) {
            this.internalName = val;
            return;
        }

        QuestRenameEvent event = new QuestRenameEvent(this, this.internalName, val);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.internalName = event.getNewName();
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }

    public Component getDisplayName() {
        return getDisplayNameRaw() == null ? text(getInternalName()) : getDisplayNameRaw();
    }

    public Component getDisplayNameRaw() {
        return this.displayName;
    }

    public void setDisplayName(Component val) {
        this.displayName = val;
        updateIfReal();
    }

    public Component getDisplayMessage() {
        return this.displayMessage;
    }

    public void setDisplayMessage(Component displayMessage) {
        this.displayMessage = displayMessage;
        updateIfReal();
    }

    public void addDisplayMessage(Component added) {
        if (this.displayMessage == null) {
            this.displayMessage = added;
        } else {
            this.displayMessage = textOfChildren(this.displayMessage, added).compact();
        }
        updateIfReal();
    }

    public Component getOverwrittenStateMessage() {
        return this.overwrittenStateMessage;
    }

    public void setOverwrittenStateMessage(Component progressMessage) {
        this.overwrittenStateMessage = progressMessage;
        updateIfReal();
    }

    public List<QuestAction> getGiveActions() {
        return Collections.unmodifiableList(this.giveActions);
    }

    public void addGiveAction(QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        this.giveActions.add(action);
        updateIfReal();
    }

    public QuestAction replaceGiveAction(int giveActionIndex, QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        QuestAction old = this.giveActions.set(giveActionIndex, action);
        updateIfReal();
        return old;
    }

    public QuestAction removeGiveAction(int giveActionIndex) {
        QuestAction old = this.giveActions.remove(giveActionIndex);
        updateIfReal();
        return old;
    }

    public void moveGiveAction(int fromIndex, int toIndex) {
        QuestAction action = this.giveActions.remove(fromIndex);
        this.giveActions.add(toIndex, action);
    }

    public List<QuestAction> getSuccessActions() {
        return Collections.unmodifiableList(this.successActions);
    }

    public void addSuccessAction(QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        this.successActions.add(action);
        updateIfReal();
    }

    public QuestAction replaceSuccessAction(int successActionIndex, QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        QuestAction old = this.successActions.set(successActionIndex, action);
        updateIfReal();
        return old;
    }

    public QuestAction removeSuccessAction(int successActionIndex) {
        QuestAction old = this.successActions.remove(successActionIndex);
        updateIfReal();
        return old;
    }

    public void moveSuccessAction(int fromIndex, int toIndex) {
        QuestAction action = this.successActions.remove(fromIndex);
        this.successActions.add(toIndex, action);
    }

    public List<QuestAction> getFailActions() {
        return Collections.unmodifiableList(this.failActions);
    }

    public void addFailAction(QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        this.failActions.add(action);
        updateIfReal();
    }

    public QuestAction replaceFailAction(int failActionIndex, QuestAction action) {
        if (action == null) {
            throw new NullPointerException();
        }

        QuestAction old = this.failActions.set(failActionIndex, action);
        updateIfReal();
        return old;
    }

    public QuestAction removeFailAction(int failActionIndex) {
        QuestAction old = this.failActions.remove(failActionIndex);
        updateIfReal();
        return old;
    }

    public void moveFailAction(int fromIndex, int toIndex) {
        QuestAction action = this.failActions.remove(fromIndex);
        this.failActions.add(toIndex, action);
    }

    public RetryOption getAllowRetryOnSuccess() {
        return this.allowRetryOnSuccess;
    }

    public void setAllowRetryOnSuccess(RetryOption allowRetryOnSuccess) {
        this.allowRetryOnSuccess = allowRetryOnSuccess;
        updateIfReal();
    }

    public RetryOption getAllowRetryOnFail() {
        return this.allowRetryOnFail;
    }

    public void setAllowRetryOnFail(RetryOption allowRetryOnFail) {
        this.allowRetryOnFail = allowRetryOnFail;
        updateIfReal();
    }

    public boolean isAllowGiveBack() {
        return this.allowGiveBack;
    }

    public void setAllowGiveBack(boolean allowGiveBack) {
        this.allowGiveBack = allowGiveBack;
        updateIfReal();
    }

    public long getAutoRemoveMs() {
        return this.autoRemoveMs;
    }

    public void setAutoRemoveMs(long autoRemoveMs) {
        this.autoRemoveMs = autoRemoveMs;
        updateIfReal();
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        updateIfReal();
    }

    protected boolean isDelayDatabaseUpdate() {
        return this.delayDatabaseUpdate;
    }

    // Wenn true wird nicht geupdated, bis wieder auf false gesetzt.
    public void setDelayDatabaseUpdate(boolean delay) {
        if (delay) {
            this.delayDatabaseUpdate = true;
        } else {
            boolean oldVal = this.delayDatabaseUpdate;
            this.delayDatabaseUpdate = false;
            if (oldVal) {
                updateIfReal();
            }
        }
    }

    public QuestState createQuestState(Player player) {
        return createQuestState(player.getUniqueId());
    }

    public QuestState createQuestState(UUID id) {
        return this.id < 0 ? null : new QuestState(CubeQuest.getInstance().getPlayerData(id), this.id);
    }

    public void giveToPlayer(Player player) {
        if (!isReady()) {
            throw new IllegalStateException("Quest " + this + " is not ready!");
        }

        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Giving quest " + this.id + " to player " + player.getName() + "[" + player.getUniqueId() + "]");

        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        for (QuestAction action : this.giveActions) {
            action.perform(player, data);
        }

        QuestState state = createQuestState(player);
        state.setStatus(Status.GIVENTO, false);
        data.setPlayerState(this.id, state);
    }

    public void removeFromPlayer(UUID id) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        OfflinePlayer player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(id);
        String name = player == null ? "*unknown*" : player.getName();
        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Removing quest " + this.id + " from player " + name + "[" + id + "]");

        QuestState state = createQuestState(id);
        state.setStatus(Status.NOTGIVENTO, false);
        CubeQuest.getInstance().getPlayerData(id).setPlayerState(this.id, state);
    }

    public boolean onSuccess(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        QuestState state = data.getPlayerState(this.id);
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }

        QuestWouldSucceedEvent event = new QuestWouldSucceedEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Quest " + this.id + " succeeding for player " + player.getName() + "[" + player.getUniqueId() + "]");

        for (QuestAction action : this.successActions) {
            action.perform(player, data);
        }

        state.setStatus(Status.SUCCESS);
        data.updateDailyQuestStreak(this);
        Bukkit.getPluginManager()
                .callEvent(new QuestSuccessEvent(this, player, this.allowRetryOnSuccess == RetryOption.AUTO_RETRY));

        if (this.allowRetryOnSuccess == RetryOption.AUTO_RETRY) {
            data.addPendingRegiving();
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                giveToPlayer(player);
                data.removePendingRegiving();
            });
        }

        return true;
    }

    public boolean onFail(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        QuestState state = data.getPlayerState(this.id);
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }

        QuestWouldFailEvent event = new QuestWouldFailEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Quest " + this.id + " failing for player " + player.getName() + "[" + player.getUniqueId() + "]");

        for (QuestAction action : this.failActions) {
            action.perform(player, data);
        }

        state.setStatus(Status.FAIL);
        Bukkit.getPluginManager()
                .callEvent(new QuestFailEvent(this, player, this.allowRetryOnFail == RetryOption.AUTO_RETRY));

        if (this.allowRetryOnFail == RetryOption.AUTO_RETRY) {
            data.addPendingRegiving();
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                giveToPlayer(player);
                data.removePendingRegiving();
            });
        }

        return true;
    }

    public boolean onFreeze(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(this.id);
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }

        QuestWouldFreezeEvent event = new QuestWouldFreezeEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Quest " + this.id + " freezing for player " + player.getName() + "[" + player.getUniqueId() + "]");

        state.setStatus(Status.FROZEN);
        Bukkit.getPluginManager().callEvent(new QuestFreezeEvent(this, player));

        return true;
    }

    /**
     * Erfordert in jedem Fall einen Datenbankzugriff, aus Performance-Gründen zu häufige Aufrufe
     * vermeiden!
     * 
     * @return Ob es mindestens einen Spieler gibt, an den diese Quest bereits vergeben wurde. Zählt
     *         auch Spieler, die die Quest bereits abgeschlossen haben (success und fail).
     */
    public boolean isGivenToPlayer() {
        try {
            return CubeQuest.getInstance().getDatabaseFassade().countPlayersGivenTo(this.id) > 0;
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not count players given quest " + this.id + "!", e);
            return false;
        }
    }

    public boolean shouldAutoRemove(PlayerData pData) {
        if (this.autoRemoveMs < 0) {
            return false;
        }
        return getLastAction(pData) + this.autoRemoveMs <= System.currentTimeMillis();
    }

    protected long getLastAction(PlayerData pData) {
        QuestState state = pData.getPlayerState(getId());
        return state == null ? -1 : state.getLastAction();
    }

    public boolean isReady() {
        return this.ready && isReal();
    }

    public void setReady(boolean val) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        if (val) {
            if (!isLegal()) {
                throw new IllegalStateException("Quest is not legal (id " + this.id + ").");
            }

            QuestSetReadyEvent event = new QuestSetReadyEvent(this, val);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                throw new IllegalStateException("QuestSetReadyEvent cancelled.");
            }

            this.ready = true;
        } else {
            QuestSetReadyEvent event = new QuestSetReadyEvent(this, val);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                throw new IllegalStateException("QuestSetReadyEvent cancelled.");
            }

            this.ready = false;
        }
        updateIfReal();

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeInt(GlobalChatMsgType.QUEST_SETREADY.ordinal());
            msgout.writeInt(getId());
            msgout.writeBoolean(val);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
            return;
        }

        byte[] msgarry = msgbytes.toByteArray();
        CubeQuest.getInstance().sendToGlobalDataChannel(msgarry);
    }

    public List<QuestCondition> getQuestGivingConditions() {
        return Collections.unmodifiableList(this.questGivingConditions);
    }

    public List<QuestCondition> getVisibleGivingConditions() {
        return Collections.unmodifiableList(this.visibleGivingConditions);
    }

    public boolean fulfillsGivingConditions(Player player, PlayerData data) {
        if (!isReady()) {
            return false;
        }

        Status status = data.getPlayerStatus(this.id);
        if (status == Status.GIVENTO || status == Status.FROZEN) {
            return false;
        }
        if (status == Status.SUCCESS && !this.allowRetryOnSuccess.allow) {
            return false;
        }
        if (status == Status.FAIL && !this.allowRetryOnFail.allow) {
            return false;
        }

        return this.questGivingConditions.stream().allMatch(qgc -> qgc.fulfills(player, data));
    }

    public boolean fulfillsGivingConditions(Player player) {
        return fulfillsGivingConditions(player, CubeQuest.getInstance().getPlayerData(player));
    }

    public void addQuestGivingCondition(QuestCondition qgc) {
        if (qgc == null) {
            throw new NullPointerException();
        }
        this.questGivingConditions.add(qgc);

        updateIfReal();

        if (qgc.isVisible()) {
            this.visibleGivingConditions.add(qgc);
        }
    }

    public void removeQuestGivingCondition(int questGivingConditionIndex) {
        QuestCondition cond = this.questGivingConditions.remove(questGivingConditionIndex);
        updateIfReal();
        if (cond.isVisible()) {
            this.visibleGivingConditions.remove(cond);
        }
    }

    public abstract boolean isLegal();

    @SuppressWarnings("unused")
    public void onDeletion(boolean cascading) throws QuestDeletionFailedException {
        CubeQuest.getInstance().addStoredMessage(this + " deleted.");
        Bukkit.getPluginManager().callEvent(new QuestDeleteEvent(this, cascading));
    }

    public void updateIfReal() {
        if (!this.delayDatabaseUpdate && isReal()) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }

    protected static Component suggest(Component component, String command) {
        return component.clickEvent(ClickEvent.suggestCommand("/" + command)).hoverEvent(SUGGEST_COMMAND_HOVER_EVENT);
    }

    public List<Component> getQuestInfo() {
        List<Component> result = new ArrayList<>();

        result.add(empty());
        result.add(text("Quest-Info zu " + getTypeName() + " [" + this.id + "]", NamedTextColor.DARK_GREEN)
                .decorate(TextDecoration.UNDERLINED));
        result.add(empty());

        result.add(
                suggest(text("Name: ", NamedTextColor.DARK_AQUA).append(text(this.internalName, NamedTextColor.GREEN)),
                        SetQuestNameCommand.FULL_INTERNAL_COMMAND));

        result.add(suggest(
                textOfChildren(text("Anzeigename: ", NamedTextColor.DARK_AQUA),
                        this.displayName != null ? this.displayName : text("NULL", NamedTextColor.GOLD)),
                SetQuestNameCommand.FULL_DISPLAY_COMMAND));

        result.add(suggest(
                textOfChildren(text("Beschreibung im Giver: ", NamedTextColor.DARK_AQUA),
                        this.displayMessage != null ? this.displayMessage : text("NULL", NamedTextColor.GOLD)),
                SetOrAppendDisplayMessageCommand.FULL_SET_COMMAND));

        result.add(suggest(
                textOfChildren(text("Beschreibung in Fortschrittsanzeige: ", NamedTextColor.DARK_AQUA),
                        this.overwrittenStateMessage == null ? text(" (automatisch)", NamedTextColor.GOLD)
                                : this.overwrittenStateMessage.append(text(" (gesetzt)", NamedTextColor.GREEN))),
                SetOverwrittenNameForSthCommand.SpecificSth.STATE_MESSAGE.fullSetCommand));

        result.add(empty());

        result.add(suggest(
                text("Vergabeaktionen:", NamedTextColor.DARK_AQUA)
                        .append(this.giveActions.isEmpty() ? text(" KEINE", NamedTextColor.GOLD) : Component.empty()),
                ActionTime.GIVE.fullCommand + " add "));

        for (int i = 0; i < this.giveActions.size(); i++) {
            QuestAction action = this.giveActions.get(i);
            result.add(
                    suggest(text("Aktion " + (i + 1) + ": ", NamedTextColor.DARK_AQUA).append(action.getActionInfo()),
                            ActionTime.GIVE.fullCommand + " remove " + (i + 1)));
        }

        result.add(empty());

        result.add(suggest(
                text("Erfolgsaktionen:", NamedTextColor.DARK_AQUA).append(
                        this.successActions.isEmpty() ? text(" KEINE", NamedTextColor.GOLD) : Component.empty()),
                ActionTime.SUCCESS.fullCommand + " add "));

        for (int i = 0; i < this.successActions.size(); i++) {
            QuestAction action = this.successActions.get(i);
            result.add(
                    suggest(text("Aktion " + (i + 1) + ": ", NamedTextColor.DARK_AQUA).append(action.getActionInfo()),
                            ActionTime.SUCCESS.fullCommand + " remove " + (i + 1)));
        }

        result.add(empty());

        result.add(suggest(
                text("Misserfolgsaktionen:", NamedTextColor.DARK_AQUA)
                        .append(this.failActions.isEmpty() ? text(" KEINE", NamedTextColor.GOLD) : Component.empty()),
                ActionTime.FAIL.fullCommand + " add "));

        for (int i = 0; i < this.failActions.size(); i++) {
            QuestAction action = this.failActions.get(i);
            result.add(
                    suggest(text("Aktion " + (i + 1) + ": ", NamedTextColor.DARK_AQUA).append(action.getActionInfo()),
                            ActionTime.FAIL.fullCommand + " remove " + (i + 1)));
        }

        result.add(empty());

        result.add(suggest(
                text("Wiederholen nach Erfolg: ", NamedTextColor.DARK_AQUA).append(text(this.allowRetryOnSuccess.name(),
                        this.allowRetryOnSuccess.allow ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                SetAllowRetryCommand.FULL_SUCCESS_COMMAND));

        result.add(suggest(
                text("Wiederholen nach Misserfolg: ", NamedTextColor.DARK_AQUA)
                        .append(text(this.allowRetryOnFail.name(),
                                this.allowRetryOnFail.allow ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                SetAllowRetryCommand.FULL_FAIL_COMMAND));

        result.add(empty());

        result.add(suggest(
                text("Kann zurückgegeben werden: ", NamedTextColor.DARK_AQUA)
                        .append(text(String.valueOf(this.allowGiveBack),
                                this.allowGiveBack ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                SetAllowGiveBackCommand.FULL_COMMAND));

        result.add(suggest(
                text("Wird automatisch entfernt: ", NamedTextColor.DARK_AQUA)
                        .append(this.autoRemoveMs < 0 ? text("nein", NamedTextColor.GOLD)
                                : text(StringUtil.formatTimespan(this.autoRemoveMs), NamedTextColor.GREEN)),
                SetAutoRemoveCommand.FULL_COMMAND));

        result.add(empty());

        result.add(suggest(
                text("Vergabebedingungen:", NamedTextColor.DARK_AQUA).append(
                        this.questGivingConditions.isEmpty() ? text(" KEINE", NamedTextColor.GOLD) : Component.empty()),
                AddConditionCommand.FULL_GIVING_COMMAND));

        for (int i = 0; i < this.questGivingConditions.size(); i++) {
            QuestCondition qgc = this.questGivingConditions.get(i);
            result.add(suggest(
                    textOfChildren(text("Bedingung " + (i + 1) + (qgc.isVisible() ? "" : " (unsichtbar)") + ": ",
                            NamedTextColor.DARK_AQUA), qgc.getConditionInfo(true)),
                    RemoveConditionCommand.FULL_GIVING_COMMAND + " " + (i + 1)));
        }

        result.add(empty());

        result.add(suggest(
                text("Für Spieler sichtbar: ", NamedTextColor.DARK_AQUA).append(
                        text(String.valueOf(this.visible), this.visible ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                SetQuestVisibilityCommand.FULL_COMMAND));

        result.add(suggest(
                text("Wird automatisch vergeben: ", NamedTextColor.DARK_AQUA).append(text(
                        String.valueOf(CubeQuest.getInstance().isAutoGivenQuest(this)),
                        CubeQuest.getInstance().isAutoGivenQuest(this) ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                SetAutoGivingCommand.FULL_COMMAND));

        result.add(empty());

        boolean legal = isLegal();
        result.add(text("Erfüllt Mindestvorrausetzungen: ", NamedTextColor.DARK_AQUA)
                .append(text(String.valueOf(legal), legal ? NamedTextColor.GREEN : NamedTextColor.RED)));

        result.add(suggest(
                text("Auf \"fertig\" gesetzt: ", NamedTextColor.DARK_AQUA).append(
                        text(String.valueOf(this.ready), this.ready ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                ToggleReadyStatusCommand.FULL_COMMAND));

        result.add(empty());

        return result;
    }

    // unmaked: ignore overwrittenStateMessage
    public List<Component> getStateInfo(PlayerData data, boolean unmasked) {
        ArrayList<Component> result = new ArrayList<>();

        result.add(empty());
        result.add(text("Questfortschritt für Quest \"").append(getDisplayName()).append(text("\""))
                .color(NamedTextColor.DARK_GREEN).decorate(TextDecoration.UNDERLINED));
        result.add(empty());

        if (data.getPlayerState(this.id) == null) {
            result.add(ChatAndTextUtil.getStateStringStartingToken(Status.NOTGIVENTO)
                    .append(text(" Nicht Vergeben", NamedTextColor.DARK_AQUA)));
            return result;
        }

        result.addAll(getSpecificStateInfo(data, unmasked, 0));
        return result;
    }

    public List<Component> getSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        if (unmasked || this.overwrittenStateMessage == null) {
            return buildSpecificStateInfo(data, unmasked, indentionLevel);
        }

        List<Component> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());

        result.add(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                .append(ChatAndTextUtil.getStateStringStartingToken(state)).append(text(" "))
                .append(this.overwrittenStateMessage));

        return result;
    }

    protected abstract List<Component> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel);

    public boolean displayStateInComplex() {
        return this.overwrittenStateMessage == null || Component.IS_NOT_EMPTY.test(this.overwrittenStateMessage);
    }

    @Override
    public String toString() {
        return "[" + getTypeName() + " " + this.id + " " + getInternalName() + "]";
    }

    // Alle relevanten Block-Events

    public boolean onBlockBreakEvent(BlockBreakEvent event, QuestState state) {
        return false;
    }

    public boolean onBlockPlaceEvent(BlockPlaceEvent event, QuestState state) {
        return false;
    }

    public boolean onBlockReceiveGameEvent(BlockReceiveGameEvent event, Player player, QuestState state) {
        return false;
    }

    // Alle relevanten Entity-Events

    public boolean onEntityDamageEvent(EntityDamageEvent event, QuestState state) {
        return false;
    }

    public boolean onEntityKilledByPlayerEvent(EntityDeathEvent event, QuestState state) {
        return false;
    }

    public boolean onEntityTamedByPlayerEvent(EntityTameEvent event, QuestState state) {
        return false;
    }

    // Alle relevanten Player-Events

    public boolean afterPlayerJoinEvent(QuestState state) {
        return false;
    }

    public boolean onPlayerQuitEvent(PlayerQuitEvent event, QuestState state) {
        return false;
    }

    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        return false;
    }

    public boolean onPlayerFishEvent(PlayerFishEvent event, QuestState state) {
        return false;
    }

    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event, QuestState state) {
        return false;
    }

    public boolean onPlayerStatisticUpdatedEvent(PlayerStatisticUpdatedEvent event, QuestState state) {
        return false;
    }

    // Wrapper für alle relevanten Events mit Interactorn

    public boolean onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event, QuestState state) {
        return false;
    }

    // Alle relevanten Quest-Events

    public boolean onQuestSuccessEvent(QuestSuccessEvent event, QuestState state) {
        return false;
    }

    public boolean onQuestFailEvent(QuestFailEvent event, QuestState state) {
        return false;
    }

    public boolean onQuestFreezeEvent(QuestFreezeEvent event, QuestState state) {
        return false;
    }

}
