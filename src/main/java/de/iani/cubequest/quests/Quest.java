package de.iani.cubequest.quests;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.commands.AddConditionCommand;
import de.iani.cubequest.commands.AddEditOrRemoveActionCommand.ActionTime;
import de.iani.cubequest.commands.AssistedSubCommand;
import de.iani.cubequest.commands.SetAllowRetryCommand;
import de.iani.cubequest.commands.SetAutoGivingCommand;
import de.iani.cubequest.commands.SetOrAppendDisplayMessageCommand;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Quest implements ConfigurationSerializable {
    
    public static final Comparator<Quest> QUEST_DISPLAY_COMPARATOR = (q1, q2) -> {
        int result = ChatAndTextUtil.stripColors(q1.getDisplayName()).compareToIgnoreCase(ChatAndTextUtil.stripColors(q2.getDisplayName()));
        return result != 0 ? result : q1.getId() - q2.getId();
    };
    public static final Comparator<Quest> QUEST_LIST_COMPARATOR = (q1, q2) -> q1.getId() - q2.getId();
    
    protected static final String INDENTION = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.RESET; // ␣
    protected static final HoverEvent SUGGEST_COMMAND_HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Befehl einfügen"));
    
    private int id;
    private String internalName;
    private String displayName;
    private String displayMessage;
    
    private List<QuestAction> giveActions;
    private List<QuestAction> successActions;
    private List<QuestAction> failActions;
    
    private RetryOption allowRetryOnSuccess;
    private RetryOption allowRetryOnFail;
    
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
    
    public Quest(int id, String internalName, String displayMessage) {
        Verify.verify(id != 0);
        
        this.id = id;
        this.internalName = internalName == null ? "" : internalName;
        this.displayMessage = displayMessage;
        this.giveActions = new ArrayList<>();
        this.successActions = new ArrayList<>();
        this.failActions = new ArrayList<>();
        this.allowRetryOnSuccess = RetryOption.DENY_RETRY;
        this.allowRetryOnFail = RetryOption.DENY_RETRY;
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
        
        this.displayName = yc.getString("displayName");
        this.displayMessage = yc.getString("displayMessage");
        
        this.giveActions = (List<QuestAction>) yc.getList("giveActions", this.giveActions);
        this.successActions = (List<QuestAction>) yc.getList("successActions", this.successActions);
        this.failActions = (List<QuestAction>) yc.getList("failActions", this.failActions);
        
        this.allowRetryOnSuccess = RetryOption.valueOf(yc.getString("allowRetryOnSuccess", "DENY_RETRY"));
        this.allowRetryOnFail = RetryOption.valueOf(yc.getString("allowRetryOnFail", "DENY_RETRY"));
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
        yc.set("displayName", this.displayName);
        yc.set("displayMessage", this.displayMessage);
        yc.set("giveActions", this.giveActions);
        yc.set("successActions", this.successActions);
        yc.set("failActions", this.failActions);
        yc.set("allowRetryOnSuccess", this.allowRetryOnSuccess.name());
        yc.set("allowRetryOnFail", this.allowRetryOnFail.name());
        yc.set("visible", this.visible);
        yc.set("ready", this.ready);
        yc.set("questGivingConditions", this.questGivingConditions);
        
        return yc.saveToString();
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
    
    public String getDisplayName() {
        return getDisplayNameRaw() == null ? getInternalName() : getDisplayNameRaw();
    }
    
    public String getDisplayNameRaw() {
        return this.displayName;
    }
    
    public void setDisplayName(String val) {
        this.displayName = val;
        updateIfReal();
    }
    
    public String getDisplayMessage() {
        return this.displayMessage;
    }
    
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
        updateIfReal();
    }
    
    public void addDisplayMessage(String added) {
        this.displayMessage = this.displayMessage == null ? added : (this.displayMessage + " " + added);
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
    
    public RetryOption isAllowRetryOnSuccess() {
        return this.allowRetryOnSuccess;
    }
    
    public void setAllowRetryOnSuccess(RetryOption allowRetryOnSuccess) {
        this.allowRetryOnSuccess = allowRetryOnSuccess;
        updateIfReal();
    }
    
    public RetryOption isAllowRetryOnFail() {
        return this.allowRetryOnFail;
    }
    
    public void setAllowRetryOnFail(RetryOption allowRetryOnFail) {
        this.allowRetryOnFail = allowRetryOnFail;
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
        
        for (QuestAction action : this.successActions) {
            action.perform(player, data);
        }
        
        state.setStatus(Status.SUCCESS);
        Bukkit.getPluginManager().callEvent(new QuestSuccessEvent(this, player, this.allowRetryOnSuccess == RetryOption.AUTO_RETRY));
        
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
        
        for (QuestAction action : this.failActions) {
            action.perform(player, data);
        }
        
        state.setStatus(Status.FAIL);
        Bukkit.getPluginManager().callEvent(new QuestFailEvent(this, player, this.allowRetryOnFail == RetryOption.AUTO_RETRY));
        
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
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not count players given quest " + this.id + "!", e);
            return false;
        }
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
    public void onDeletion() throws QuestDeletionFailedException {
        Bukkit.getPluginManager().callEvent(new QuestDeleteEvent(this));
    }
    
    public void updateIfReal() {
        if (!this.delayDatabaseUpdate && isReal()) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }
    
    public List<BaseComponent[]> getQuestInfo() {
        ArrayList<BaseComponent[]> result = new ArrayList<>();
        result.add(new ComponentBuilder("").create());
        result.add(ChatAndTextUtil.headline1("Quest-Info zu " + getTypeName() + " [" + this.id + "]"));
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + this.internalName)
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestNameCommand.FULL_INTERNAL_COMMAND)).event(SUGGEST_COMMAND_HOVER_EVENT)
                .create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Anzeigename: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestNameCommand.FULL_DISPLAY_COMMAND)).event(SUGGEST_COMMAND_HOVER_EVENT)
                .append(TextComponent.fromLegacyText(this.displayName == null ? ChatColor.GOLD + "NULL" : ChatColor.GREEN + this.displayName))
                .create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Beschreibung im Giver: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetOrAppendDisplayMessageCommand.FULL_SET_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT)
                .append(TextComponent.fromLegacyText(this.displayMessage == null ? ChatColor.GOLD + "NULL" : ChatColor.RESET + this.displayMessage))
                .create());
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Vergabeaktionen:" + (this.giveActions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.GIVE.fullCommand + " add ")).event(SUGGEST_COMMAND_HOVER_EVENT)
                .create());
        for (int i = 0; i < this.giveActions.size(); i++) {
            QuestAction action = this.giveActions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Aktion " + (i + 1) + ": ")
                    .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.GIVE.fullCommand + " remove " + (i + 1)))
                    .event(SUGGEST_COMMAND_HOVER_EVENT).append(action.getActionInfo()).create());
        }
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfolgsaktionen:" + (this.successActions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.SUCCESS.fullCommand + " add ")).event(SUGGEST_COMMAND_HOVER_EVENT)
                .create());
        for (int i = 0; i < this.successActions.size(); i++) {
            QuestAction action = this.successActions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Aktion " + (i + 1) + ": ")
                    .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.SUCCESS.fullCommand + " remove " + (i + 1)))
                    .event(SUGGEST_COMMAND_HOVER_EVENT).append(action.getActionInfo()).create());
        }
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Misserfolgsaktionen:" + (this.failActions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.FAIL.fullCommand + " add ")).event(SUGGEST_COMMAND_HOVER_EVENT)
                .create());
        for (int i = 0; i < this.failActions.size(); i++) {
            QuestAction action = this.failActions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Aktion " + (i + 1) + ": ")
                    .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ActionTime.FAIL.fullCommand + " remove " + (i + 1)))
                    .event(SUGGEST_COMMAND_HOVER_EVENT).append(action.getActionInfo()).create());
        }
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Wiederholen nach Erfolg: "
                + (this.allowRetryOnSuccess.allow ? ChatColor.GREEN + this.allowRetryOnSuccess.name()
                        : ChatColor.GOLD + this.allowRetryOnSuccess.name()))
                                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetAllowRetryCommand.FULL_SUCCESS_COMMAND))
                                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Wiederholen nach Misserfolg: "
                + (this.allowRetryOnFail.allow ? ChatColor.GREEN + this.allowRetryOnFail.name() : ChatColor.GOLD + this.allowRetryOnFail.name()))
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetAllowRetryCommand.FULL_FAIL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Vergabebedingungen:" + (this.questGivingConditions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + AddConditionCommand.FULL_GIVING_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        for (int i = 0; i < this.questGivingConditions.size(); i++) {
            QuestCondition qgc = this.questGivingConditions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bedingung " + (i + 1) + (qgc.isVisible() ? "" : " (unsichtbar)") + ": ")
                    .append(qgc.getConditionInfo(true)).create());
        }
        result.add(new ComponentBuilder("").create());
        
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Für Spieler sichtbar: " + (this.visible ? ChatColor.GREEN : ChatColor.GOLD) + this.visible)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestVisibilityCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Wird automatisch vergeben: "
                + (CubeQuest.getInstance().isAutoGivenQuest(this) ? ChatColor.GREEN + "true" : ChatColor.GOLD + "false"))
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetAutoGivingCommand.FULL_COMMAND)).event(SUGGEST_COMMAND_HOVER_EVENT)
                        .create());
        result.add(new ComponentBuilder("").create());
        
        boolean legal = isLegal();
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfüllt Mindestvorrausetzungen: " + (legal ? ChatColor.GREEN : ChatColor.RED) + legal)
                .create());
        result.add(
                new ComponentBuilder(ChatColor.DARK_AQUA + "Auf \"fertig\" gesetzt: " + (this.ready ? ChatColor.GREEN : ChatColor.GOLD) + this.ready)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + ToggleReadyStatusCommand.FULL_COMMAND)).event(SUGGEST_COMMAND_HOVER_EVENT)
                        .create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public List<BaseComponent[]> getStateInfo(PlayerData data) {
        ArrayList<BaseComponent[]> result = new ArrayList<>();
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder("Questfortschritt für Quest \"").color(ChatColor.DARK_GREEN).underlined(true)
                .append(TextComponent.fromLegacyText(getDisplayName())).append("\"").create());
        result.add(new ComponentBuilder("").create());
        
        if (data.getPlayerState(this.id) == null) {
            result.add(new ComponentBuilder("").append(ChatAndTextUtil.getStateStringStartingToken(null))
                    .append(ChatColor.DARK_AQUA + " Nicht Vergeben").create());
            return result;
        }
        
        result.addAll(getSpecificStateInfo(data, 0));
        
        return result;
    }
    
    public abstract List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel);
    
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
    
    // Alle relevanten Entity-Events
    
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
