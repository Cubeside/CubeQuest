package de.iani.cubequest.quests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

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

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.Reward;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldFailEvent;
import de.iani.cubequest.events.QuestWouldSucceedEvent;
import de.iani.cubequest.questGiving.QuestGivingCondition;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.wrapper.NPCClickEventWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class Quest implements ConfigurationSerializable {

    private int id;
    private String name;
    private String displayMessage;

    private String giveMessage;
    private String successMessage;
    private String failMessage;

    private Reward successReward;
    private Reward failReward;

    private boolean ready;

    private List<QuestGivingCondition> questGivingConditions;

    public Quest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        Verify.verify(id != 0);

        this.id = id;
        this.name = name == null? "" : name;
        this.displayMessage = displayMessage;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.failMessage = failMessage;
        this.successReward = successReward;
        this.failReward = failReward;
        this.ready = false;
        this.questGivingConditions = new ArrayList<>();
    }

    public Quest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null);
    }

    public Quest(int id) {
        this(id, null, null, null, null, null);
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
     * Erzeugt eine neue YamlConfiguration aus dem String und ruft dann {@link Quest#deserialize(YamlConfigration)} auf.
     * @param serialized serialisierte Quest
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(String serialized) throws InvalidConfigurationException {
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        deserialize(yc);
    }

    /**
     * Wendet den Inhalt der YamlConfiguration auf die Quest an.
     * @param yc serialisierte Quest-Daten
     * @throws InvalidConfigurationException wird weitergegeben
     */
    @SuppressWarnings("unchecked")
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        if (!yc.getString("type").equals(QuestType.getQuestType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }

        this.name = yc.getString("name");
        this.displayMessage = yc.getString("displayMessage");
        this.giveMessage = yc.getString("giveMessage");
        this.successMessage = yc.getString("successMessage");
        this.failMessage = yc.getString("failMessage");
        this.successReward = (Reward) yc.get("successReward");
        this.failReward = (Reward) yc.get("failReward");
        this.ready = yc.getBoolean("ready");
        this.questGivingConditions = (List<QuestGivingCondition>) yc.get("questGivingConditions");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", getId());
        result.put("serialized", serializeToString());
        return result;
    }

    /**
     * Serialisiert die Quest
     * @return serialisierte Quest
     */
    public String serializeToString() {
        return serializeToString(new YamlConfiguration());
    }

    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der Oberklasse aufrufen.
     * @param yc YamlConfiguration mit den Daten der Quest
     * @return serialisierte Quest
     */
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("type", QuestType.getQuestType(this.getClass()).toString());
        yc.set("name", name);
        yc.set("displayMessage", displayMessage);
        yc.set("giveMessage", giveMessage);
        yc.set("successMessage", successMessage);
        yc.set("failMessage", failMessage);
        yc.set("successReward", successReward);
        yc.set("failReward", failReward);
        yc.set("ready", ready);
        yc.set("questGivingConditions", questGivingConditions);

        return yc.saveToString();
    }

    public int getId() {
        return id;
    }

    public boolean isRealQuest() {
        return id > 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        val = val == null? "" : val;

        if (id < 0) {
            name = val;
            return;
        }

        QuestRenameEvent event = new QuestRenameEvent(this, name, val);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.name = event.getNewName();
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }

    public String getTypeName() {
        return "" + QuestType.getQuestType(this.getClass());
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
        updateIfReal();
    }

    public String getGiveMessage() {
        return giveMessage;
    }

    public void setGiveMessage(String giveMessage) {
        this.giveMessage = giveMessage;
        updateIfReal();
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
        updateIfReal();
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
        updateIfReal();
    }

    public Reward getSuccessReward() {
        return successReward;
    }

    public void setSuccessReward(Reward successReward) {
        if (successReward.isEmpty()) {
            successReward = null;
        }
        this.successReward = successReward;
        updateIfReal();
    }

    public Reward getFailReward() {
        return failReward;
    }

    public void setFailReward(Reward failReward) {
        if (failReward.isEmpty()) {
            failReward = null;
        }
        this.failReward = failReward;
        updateIfReal();
    }

    public QuestState createQuestState(Player player) {
        return createQuestState(player.getUniqueId());
    }

    public QuestState createQuestState(UUID id) {
        return this.id < 0? null : new QuestState(CubeQuest.getInstance().getPlayerData(id), this.id);
    }

    public void giveToPlayer(Player player) {
        if (!isReady()) {
            throw new IllegalStateException("Quest is not ready!");
        }
        if (giveMessage != null) {
            player.sendMessage(giveMessage);
        }
        QuestState state = createQuestState(player);
        state.setStatus(Status.GIVENTO, false);
        CubeQuest.getInstance().getPlayerData(player).setPlayerState(id, state);
    }

    public void removeFromPlayer(UUID id) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        QuestState state = createQuestState(id);
        CubeQuest.getInstance().getPlayerData(id).setPlayerState(this.id, state);
    }

    public boolean onSuccess(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        QuestWouldSucceedEvent event = new QuestWouldSucceedEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (successMessage != null) {
            player.sendMessage(successMessage);
        }

        if (successReward != null) {
            successReward.pay(player);
        }

        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(id);
        state.setStatus(Status.SUCCESS);
        Bukkit.getPluginManager().callEvent(new QuestSuccessEvent(this, player));

        return true;
    }

    public boolean onFail(Player player) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        QuestWouldFailEvent event = new QuestWouldFailEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (failReward != null) {
            failReward.pay(player);
        }

        if (failMessage != null) {
            player.sendMessage(failMessage);
        }

        Bukkit.getPluginManager().callEvent(new QuestFailEvent(this, player));

        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(id);
        state.setStatus(Status.FAIL);

        CubeQuest.getInstance().getPlayerData(player).getPlayerState(id).setStatus(Status.FAIL);
        return true;
    }

    /**
     * Erfordert in jedem Fall einen Datenbankzugriff, aus Performance-Gründen zu häufige Aufrufe vermeiden!
     * @return Ob es mindestens einen Spieler gibt, an den diese Quest bereits vergeben wurde. Zählt auch Spieler, die die Quest bereits abgeschlossen haben (success und fail).
     */
    public boolean isGivenToPlayer() {
        try {
            return CubeQuest.getInstance().getDatabaseFassade().countPlayersGivenTo(id) > 0;
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not count players given quest " + id + "!", e);
            return false;
        }
    }

    public boolean isReady() {
        return ready && isRealQuest();
    }

    public void setReady(boolean val) {
        if (this.id < 0) {
            throw new IllegalStateException("This is no real quest!");
        }

        if (val) {
            if (!isLegal()) {
                throw new IllegalStateException("Quest is not legal");
            }
            this.ready = true;
        } else if (this.ready && isGivenToPlayer()) {
            throw new IllegalStateException("Already given to some players, can not be eddited!");
        } else {
            this.ready = false;
        }
        updateIfReal();
    }

    public List<QuestGivingCondition> getQuestGivingConditions() {
        return Collections.unmodifiableList(questGivingConditions);
    }

    public boolean fullfillsGivingConditions(PlayerData data) {
        return questGivingConditions.stream().allMatch(qgc -> qgc.fullfills(data));
    }

    public boolean fullfillsGivingConditions(UUID playerId) {
        return questGivingConditions.stream().allMatch(qgc -> qgc.fullfills(playerId));
    }

    public boolean fullfillsGivingConditions(Player player) {
        return questGivingConditions.stream().allMatch(qgc -> qgc.fullfills(player));
    }

    public void addQuestGivingCondition(QuestGivingCondition qgc) {
        if (qgc == null) {
            throw new NullPointerException();
        }
        questGivingConditions.add(qgc);
        updateIfReal();
    }

    public void removeQuestGivingCondition(int questGivingConditionIndex) {
        questGivingConditions.remove(questGivingConditionIndex);
        updateIfReal();
    }

    public abstract boolean isLegal();

    public void updateIfReal() {
        if (isRealQuest()) {
            CubeQuest.getInstance().getQuestCreator().updateQuest(this);
        }
    }

    public List<BaseComponent[]> getQuestInfo() {
        ArrayList<BaseComponent[]> result = new ArrayList<>();
        result.add(ChatAndTextUtil.headline1(ChatColor.UNDERLINE + "Quest-Info zu " + this.getTypeName() + " [" + id + "]"));
        result.add(new ComponentBuilder("").create());

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + name).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Vergabenachricht: " + (giveMessage == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + giveMessage)).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfolgsnachricht: " + (successMessage == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + successMessage)).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Misserfolgsnachricht: " + (failMessage == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + failMessage)).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfolgsbelohnung: " + (successReward == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + successReward.toNiceString())).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Misserfolgsbelohnung: " + (failReward == null? ChatColor.GOLD + "NULL" : ChatColor.GREEN + failReward.toNiceString())).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Vergabebedingungen:" + (questGivingConditions.isEmpty()? ChatColor.GOLD + " KEINE" : "")).create());
        for (int i=0; i<questGivingConditions.size(); i++) {
            QuestGivingCondition qgc = questGivingConditions.get(i);
            result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bedingung " + (i+1) + ":").create());
            for (BaseComponent[] bc: qgc.getConditionInfo()) {
                result.add(new ComponentBuilder("  ").append(bc).create());
            }
            result.addAll(qgc.getConditionInfo());
        }
        result.add(new ComponentBuilder("").create());
        boolean legal = isLegal();
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfüllt Mindestvorrausetzungen: " + (legal? ChatColor.GREEN : ChatColor.RED) + legal).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Auf \"fertig\" gesetzt: " + (ready? ChatColor.GREEN : ChatColor.GOLD) + ready).create());
        result.add(new ComponentBuilder("").create());

        return result;
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

    // Alle relevanten NPC-Events

    public boolean onNPCClickEvent(NPCClickEventWrapper event, QuestState state) {
        return false;
    }

    // Alle relevanten Quest-Events

    public boolean onQuestSuccessEvent(QuestSuccessEvent event, QuestState state) {
        return false;
    }

    public boolean onQuestFailEvent(QuestFailEvent event, QuestState state) {
        return false;
    }

}
