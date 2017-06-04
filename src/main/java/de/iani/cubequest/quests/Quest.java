package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestCreator.QuestType;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldFailEvent;
import de.iani.cubequest.events.QuestWouldSucceedEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import net.citizensnpcs.api.event.NPCClickEvent;

public abstract class Quest {

    private Integer id;
    private String name;
    private String giveMessage;
    private String successMessage;
    private String failMessage;
    private Reward successReward;
    private Reward failReward;
    private boolean ready;

    protected QuestState state;

    public Quest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        Verify.verify(id != 0);

        this.name = name == null? "" : name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.failMessage = failMessage;
        this.successReward = successReward;
        this.failReward = failReward;
        this.ready = false;
    }

    public Quest(int id, String name, String giveMessage, String successMessage, Reward successReward) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    public Quest(int id) {
        this(id, null, null, null, null);
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
     * @throws InvalidConfigurationException  wird weitergegeben
     */
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        if (!yc.getString("type").equals(QuestType.getQuestType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }

        this.name = yc.getString("name");
        this.giveMessage = yc.getString("giveMessage");
        this.successMessage = yc.getString("successMessage");
        this.failMessage = yc.getString("failMessage");
        this.successReward = new Reward(yc.getString("successReward"));
        this.failReward = new Reward(yc.getString("failReward"));
        this.ready = yc.getBoolean("ready");
    }

    /**
     * Serialisiert die Quest
     * @return serialisierte Quest
     */
    public String serialize() {
        return serialize(new YamlConfiguration());
    }

    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der Oberklasse aufrufen.
     * @param yc YamlConfiguration mit den Daten der Quest
     * @return serialisierte Quest
     */
    protected String serialize(YamlConfiguration yc) {
        yc.set("name", name);
        yc.set("giveMessage", giveMessage);
        yc.set("successMessage", successMessage);
        yc.set("failMessage", failMessage);
        yc.set("successReward", successReward);
        yc.set("failReward", failReward);
        yc.set("ready", ready);

        return yc.toString();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        Verify.verifyNotNull(val);

        QuestRenameEvent event = new QuestRenameEvent(this, name, val);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.name = event.getNewName();
        }
    }

    public String getGiveMessage() {
        return giveMessage;
    }

    public void setGiveMessage(String giveMessage) {
        this.giveMessage = giveMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public Reward getSuccessReward() {
        return successReward;
    }

    public void setSuccessReward(Reward successReward) {
        this.successReward = successReward;
    }

    public Reward getFailReward() {
        return failReward;
    }

    public void setFailReward(Reward failReward) {
        this.failReward = failReward;
    }

    public abstract QuestState createNewQuestState();

    public void giveToPlayer(Player player) {
        if (!ready) {
            throw new IllegalStateException("Quest is not ready!");
        }
        if (giveMessage != null) {
            player.sendMessage(giveMessage);
        }
        QuestState state = createNewQuestState();
        state.setStatus(Status.GIVENTO);
        CubeQuest.getInstance().getPlayerData(player).setPlayerState(id, state);
    }

    public void removeFromPlayer(Player player) {
        QuestState state = createNewQuestState();
        state.setStatus(Status.NOTGIVENTO);
        CubeQuest.getInstance().getPlayerData(player).setPlayerState(id, state);
    }

    public boolean onSuccess(Player player) {
        QuestWouldSucceedEvent event = new QuestWouldSucceedEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (successReward != null) {
            if (!successReward.pay(player)) {
                return false;
            }
        }

        Bukkit.getPluginManager().callEvent(new QuestSuccessEvent(this, player));

        if (successMessage != null) {
            player.sendMessage(successMessage);
        }
        CubeQuest.getInstance().getPlayerData(player).getPlayerState(id).setStatus(Status.GIVENTO);
        return true;
    }

    public boolean onFail(Player player) {
        QuestWouldFailEvent event = new QuestWouldFailEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (failReward != null) {
            if (!failReward.pay(player)) {
                return false;
            }
        }

        Bukkit.getPluginManager().callEvent(new QuestFailEvent(this, player));

        if (failMessage != null) {
            player.sendMessage(failMessage);
        }
        givenToPlayers.put(player.getUniqueId(), Status.FAIL);
        return true;
    }

    public Status getPlayerStatus(UUID id) {
        return givenToPlayers.containsKey(id)? givenToPlayers.get(id) : Status.NOTGIVENTO;
    }

    /**
     *
     * @return HashSet (Kopie) mit allen UUIDs, deren Status GIVENTO ist.
     */
    public Collection<UUID> getPlayersGivenTo() {
        HashSet<UUID> result = new HashSet<UUID>();
        for (UUID id: givenToPlayers.keySet()) {
            if (givenToPlayers.get(id) == Status.GIVENTO) {
                result.add(id);
            }
        }
        return result;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean val) {
        if (val) {
            if (!isLegal()) {
                throw new IllegalStateException("Quest is not legal");
            }
            this.ready = true;
        } else if (this.ready && givenToPlayers.containsValue(Status.GIVENTO)) {
            throw new IllegalStateException("Already given to some players, can not be eddited!");
        }
        this.ready = false;
    }

    public abstract boolean isLegal();

    // Alle relevanten Block-Events

    public boolean onBlockBreakEvent(BlockBreakEvent event) {
        return false;
    }

    public boolean onBlockPlaceEvent(BlockPlaceEvent event) {
        return false;
    }

    // Alle relevanten Entity-Events

    public boolean onEntityDeathEvent(EntityDeathEvent event) {
        return false;
    }

    // Alle relevanten Player-Events

    public boolean onPlayerMoveEvent(PlayerMoveEvent event) {
        return false;
    }

    public boolean onPlayerFishEvent(PlayerFishEvent event) {
        return false;
    }

    public boolean onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        return false;
    }

    // Alle relevanten NPC-Events

    public boolean onNPCClickEvent(NPCClickEvent event) {
        return false;
    }

    // Alle relevanten Quest-Events

    public boolean onQuestSuccessEvent(QuestSuccessEvent event) {
        return false;
    }

    public boolean onQuestFailEvent(QuestFailEvent event) {
        return false;
    }

}
