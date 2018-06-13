package de.iani.cubequest.quests;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.Reward;
import de.iani.cubequest.commands.AssistedSubCommand;
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
import net.md_5.bungee.api.chat.ComponentBuilder;
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
        int result = q1.getName().compareToIgnoreCase(q2.getName());
        return result != 0 ? result : q1.getId() - q2.getId();
    };
    public static final Comparator<Quest> QUEST_LIST_COMPARATOR =
            (q1, q2) -> q1.getId() - q2.getId();
    
    protected static final String INDENTION =
            ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.RESET; // ␣
    
    private int id;
    private String name;
    private String displayMessage;
    
    private String giveMessage;
    private String successMessage;
    private String failMessage;
    
    private Reward successReward;
    private Reward failReward;
    
    private RetryOption allowRetryOnSuccess;
    private RetryOption allowRetryOnFail;
    
    private boolean visible;
    
    private boolean ready;
    
    private boolean delayDatabaseUpdate = false;
    
    private List<QuestCondition> questGivingConditions;
    
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
    
    public Quest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward) {
        Verify.verify(id != 0);
        
        this.id = id;
        this.name = name == null ? "" : name;
        this.displayMessage = displayMessage;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.failMessage = failMessage;
        this.successReward = successReward;
        this.failReward = failReward;
        this.allowRetryOnSuccess = RetryOption.DENY_RETRY;
        this.allowRetryOnFail = RetryOption.DENY_RETRY;
        this.visible = false;
        this.ready = false;
        this.questGivingConditions = new ArrayList<>();
    }
    
    public Quest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null);
    }
    
    public Quest(int id) {
        this(id, null, null, null, null, null);
    }
    
    public static Quest deserialize(Map<String, Object> serialized)
            throws InvalidConfigurationException {
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
        if (!this.name.equals(newName)) {
            QuestRenameEvent event = new QuestRenameEvent(this, this.name, newName);
            Bukkit.getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                // Reset name on other servers
                Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(),
                        () -> updateIfReal(), 1L);
            } else {
                this.name = newName;
            }
        } else {
            this.name = newName;
        }
        
        this.displayMessage = yc.getString("displayMessage");
        this.giveMessage = yc.getString("giveMessage");
        this.successMessage = yc.getString("successMessage");
        this.failMessage = yc.getString("failMessage");
        this.successReward = (Reward) yc.get("successReward");
        this.failReward = (Reward) yc.get("failReward");
        this.allowRetryOnSuccess =
                RetryOption.valueOf(yc.getString("allowRetryOnSuccess", "DENY_RETRY"));
        this.allowRetryOnFail = RetryOption.valueOf(yc.getString("allowRetryOnFail", "DENY_RETRY"));
        this.visible = yc.contains("visible") ? yc.getBoolean("visible") : false;
        this.ready = yc.getBoolean("ready");
        this.questGivingConditions =
                (List<QuestCondition>) yc.get("questGivingConditions", this.questGivingConditions);
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
        yc.set("name", this.name);
        yc.set("displayMessage", this.displayMessage);
        yc.set("giveMessage", this.giveMessage);
        yc.set("successMessage", this.successMessage);
        yc.set("failMessage", this.failMessage);
        yc.set("successReward", this.successReward);
        yc.set("failReward", this.failReward);
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
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String val) {
        val = val == null ? "" : val;
        
        if (this.id < 0) {
            this.name = val;
            return;
        }
        
        QuestRenameEvent event = new QuestRenameEvent(this, this.name, val);
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            this.name = event.getNewName();
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }
    
    public String getTypeName() {
        return QuestType.getQuestType(this.getClass()).toString();
    }
    
    public String getDisplayMessage() {
        return this.displayMessage;
    }
    
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
        updateIfReal();
    }
    
    public String getGiveMessage() {
        return this.giveMessage;
    }
    
    public void setGiveMessage(String giveMessage) {
        this.giveMessage = giveMessage;
        updateIfReal();
    }
    
    public String getSuccessMessage() {
        return this.successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
        updateIfReal();
    }
    
    public String getFailMessage() {
        return this.failMessage;
    }
    
    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
        updateIfReal();
    }
    
    public Reward getSuccessReward() {
        return this.successReward;
    }
    
    public void setSuccessReward(Reward successReward) {
        if (successReward != null && successReward.isEmpty()) {
            successReward = null;
        }
        this.successReward = successReward;
        updateIfReal();
    }
    
    public Reward getFailReward() {
        return this.failReward;
    }
    
    public void setFailReward(Reward failReward) {
        if (failReward != null && failReward.isEmpty()) {
            failReward = null;
        }
        this.failReward = failReward;
        updateIfReal();
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
        return this.id < 0 ? null
                : new QuestState(CubeQuest.getInstance().getPlayerData(id), this.id);
    }
    
    public void giveToPlayer(Player player) {
        if (!isReady()) {
            throw new IllegalStateException("Quest is not ready!");
        }
        if (this.giveMessage != null) {
            player.sendMessage(CubeQuest.PLUGIN_TAG + " " + this.giveMessage);
        }
        QuestState state = createQuestState(player);
        state.setStatus(Status.GIVENTO, false);
        CubeQuest.getInstance().getPlayerData(player).setPlayerState(this.id, state);
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
        
        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(this.id);
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }
        
        QuestWouldSucceedEvent event = new QuestWouldSucceedEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        
        if (this.successMessage != null) {
            player.sendMessage(CubeQuest.PLUGIN_TAG + " " + this.successMessage);
        }
        
        if (this.successReward != null) {
            this.successReward.pay(player);
        }
        
        state.setStatus(Status.SUCCESS);
        Bukkit.getPluginManager().callEvent(new QuestSuccessEvent(this, player));
        
        if (this.allowRetryOnSuccess == RetryOption.AUTO_RETRY) {
            giveToPlayer(player);
        }
        
        return true;
    }
    
    public boolean onFail(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }
        
        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(this.id);
        if (state.getStatus() != Status.GIVENTO) {
            return false;
        }
        
        QuestWouldFailEvent event = new QuestWouldFailEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        
        if (this.failReward != null) {
            this.failReward.pay(player);
        }
        
        if (this.failMessage != null) {
            player.sendMessage(CubeQuest.PLUGIN_TAG + " " + this.failMessage);
        }
        
        state.setStatus(Status.FAIL);
        Bukkit.getPluginManager().callEvent(new QuestFailEvent(this, player));
        
        if (this.allowRetryOnFail == RetryOption.AUTO_RETRY) {
            giveToPlayer(player);
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
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not count players given quest " + this.id + "!", e);
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
                throw new IllegalStateException("Quest is not legal");
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
    
    public boolean fullfillsGivingConditions(Player player, PlayerData data) {
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
        
        return this.questGivingConditions.stream().allMatch(qgc -> qgc.fullfills(player, data));
    }
    
    public boolean fullfillsGivingConditions(Player player) {
        return fullfillsGivingConditions(player, CubeQuest.getInstance().getPlayerData(player));
    }
    
    public void addQuestGivingCondition(QuestCondition qgc) {
        if (qgc == null) {
            throw new NullPointerException();
        }
        this.questGivingConditions.add(qgc);
        updateIfReal();
    }
    
    public void removeQuestGivingCondition(int questGivingConditionIndex) {
        this.questGivingConditions.remove(questGivingConditionIndex);
        updateIfReal();
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
        result.add(ChatAndTextUtil.headline1(
                ChatColor.UNDERLINE + "Quest-Info zu " + getTypeName() + " [" + this.id + "]"));
        result.add(new ComponentBuilder("").create());
        
        result.add(
                new ComponentBuilder(ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + this.name)
                        .create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Beschreibung im Giver: "
                + (this.displayMessage == null ? ChatColor.GOLD + "NULL"
                        : ChatColor.RESET + this.displayMessage)).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Vergabenachricht: "
                + (this.giveMessage == null ? ChatColor.GOLD + "NULL"
                        : ChatColor.RESET + this.giveMessage)).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfolgsnachricht: "
                + (this.successMessage == null ? ChatColor.GOLD + "NULL"
                        : ChatColor.RESET + this.successMessage)).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Misserfolgsnachricht: "
                + (this.failMessage == null ? ChatColor.GOLD + "NULL"
                        : ChatColor.RESET + this.failMessage)).create());
        result.add(new ComponentBuilder("").create());
        result.add(
                new ComponentBuilder(
                        ChatColor.DARK_AQUA + "Erfolgsbelohnung: "
                                + (this.successReward == null ? ChatColor.GOLD + "NULL"
                                        : ChatColor.GREEN + this.successReward.toNiceString()))
                                                .create());
        result.add(
                new ComponentBuilder(
                        ChatColor.DARK_AQUA + "Misserfolgsbelohnung: "
                                + (this.failReward == null ? ChatColor.GOLD + "NULL"
                                        : ChatColor.GREEN + this.failReward.toNiceString()))
                                                .create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Wiederholen nach Erfolg: "
                + (this.allowRetryOnSuccess.allow
                        ? ChatColor.GREEN + this.allowRetryOnSuccess.name()
                        : ChatColor.GOLD + this.allowRetryOnSuccess.name())).create());
        result.add(
                new ComponentBuilder(ChatColor.DARK_AQUA + "Wiederholen nach Misserfolg: "
                        + (this.allowRetryOnFail.allow
                                ? ChatColor.GREEN + this.allowRetryOnFail.name()
                                : ChatColor.GOLD + this.allowRetryOnFail.name())).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Vergabebedingungen:"
                + (this.questGivingConditions.isEmpty() ? ChatColor.GOLD + " KEINE" : ""))
                        .create());
        for (int i = 0; i < this.questGivingConditions.size(); i++) {
            QuestCondition qgc = this.questGivingConditions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bedingung " + (i + 1)
                    + (qgc.isVisible() ? "" : " (unsichtbar)") + ": ")
                            .append(qgc.getConditionInfo(true)).create());
        }
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Für Spieler sichtbar: "
                + (this.visible ? ChatColor.GREEN : ChatColor.GOLD) + this.visible).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Wird automatisch vergeben: "
                + (CubeQuest.getInstance().getAutoGivenQuests().contains(this)
                        ? ChatColor.GREEN + "true"
                        : ChatColor.GOLD + "false")).create());
        result.add(new ComponentBuilder("").create());
        boolean legal = isLegal();
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfüllt Mindestvorrausetzungen: "
                + (legal ? ChatColor.GREEN : ChatColor.RED) + legal).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Auf \"fertig\" gesetzt: "
                + (this.ready ? ChatColor.GREEN : ChatColor.GOLD) + this.ready).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public List<BaseComponent[]> getStateInfo(PlayerData data) {
        ArrayList<BaseComponent[]> result = new ArrayList<>();
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(
                ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "Questfortschritt für Quest \""
                        + getName() + ChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "\"")
                                .create());
        result.add(new ComponentBuilder("").create());
        
        if (data.getPlayerState(this.id) == null) {
            result.add(new ComponentBuilder("")
                    .append(ChatAndTextUtil.getStateStringStartingToken(null))
                    .append(ChatColor.DARK_AQUA + " Nicht Vergeben").create());
            return result;
        }
        
        result.addAll(getSpecificStateInfo(data, 0));
        
        return result;
    }
    
    public abstract List<BaseComponent[]> getSpecificStateInfo(PlayerData data, int indentionLevel);
    
    @Override
    public String toString() {
        return "[" + getTypeName() + " " + this.id + " " + getName() + "]";
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
    
    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event,
            QuestState state) {
        return false;
    }
    
    // Wrapper für alle relevanten Events mit Interactorn
    
    public boolean onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event,
            QuestState state) {
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
