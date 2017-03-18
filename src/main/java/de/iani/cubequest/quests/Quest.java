package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldFailEvent;
import de.iani.cubequest.events.QuestWouldSucceedEvent;
import net.citizensnpcs.api.event.NPCClickEvent;

public abstract class Quest {

    private String name;
    private String giveMessage;
    private String successMessage;
    private String failMessage;
    private Reward successReward;
    private Reward failReward;
    private HashSet<ComplexQuest> superquests;
    private HashMap<UUID, Status> givenToPlayers;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL
    }

    public Quest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        Verify.verifyNotNull(name);
        //TODO: Abfragen, ob Questname schon existiert

        this.name = name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.failMessage = failMessage;
        this.successReward = successReward;
        this.failReward = failReward;
        this.superquests = new HashSet<ComplexQuest>();
        this.givenToPlayers = new HashMap<UUID, Status>();
    }

    public Quest(String name, String giveMessage, String successMessage, Reward successReward) {
        this(name, giveMessage, successMessage, null, successReward, null);
    }

    public String getName() {
        return name;
    }

    public String getGiveMessage() {
        return giveMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public Reward getSuccessReward() {
        return successReward;
    }

    public Reward getFailReward() {
        return failReward;
    }

    /**
     * @return superQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<ComplexQuest> getSuperquests() {
        return Collections.unmodifiableCollection(superquests);
    }

    public void addSuperQuest(ComplexQuest quest) {
        superquests.add(quest);
    }

    public void giveToPlayer(Player player) {
        if (giveMessage != null) {
            player.sendMessage(giveMessage);
        }
        givenToPlayers.put(player.getUniqueId(), Status.GIVENTO);
    }

    public void removeFromPlayer(UUID id) {
        givenToPlayers.remove(id);
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
        givenToPlayers.put(player.getUniqueId(), Status.SUCCESS);
        for (ComplexQuest q: superquests) {
            q.update(player);
        }
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
        for (ComplexQuest q: superquests) {
            q.update(player);
        }
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

    // Alle relevanten Block-Events

    public void onBlockBreakEvent(BlockBreakEvent event) {

    }

    public void onBlockPlaceEvent(BlockPlaceEvent event) {

    }

    // Alle relevanten Entity-Events

    public void onEntityDeathEvent(EntityDeathEvent event) {

    }

    // Alle relevanten Player-Events

    public void onPlayerMoveEvent(PlayerMoveEvent event) {

    }

    public void onPlayerFishEvent(PlayerFishEvent event) {

    }

    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {

    }

    // Alle relevanten NPC-Events

    public void onNPCClickEvent(NPCClickEvent event) {

    }

    // Alle relevanten Quest-Events

    public void onQuestSuccessEvent(QuestSuccessEvent event) {

    }

    public void onQuestFailEvent(QuestFailEvent event) {

    }

}
