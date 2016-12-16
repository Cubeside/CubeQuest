package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.citizensnpcs.api.event.NPCClickEvent;

public abstract class Quest {

    private String name;
    private String giveMessage;
    private String successMessage;
    private Reward successReward;
    private HashSet<ComplexQuest> superquests;
    private HashMap<UUID, Boolean> givenToPlayers;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS
    }

    public Quest(String name, String giveMessage, String successMessage, Reward successReward) {
        if (name == null) throw new NullPointerException("name may not be null");
        //TODO: Abfragen, ob Questname schon existiert

        this.name = name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.successReward = successReward;
        this.superquests = new HashSet<ComplexQuest>();
        this.givenToPlayers = new HashMap<UUID, Boolean>();
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


    public Reward getSuccessReward() {
        return successReward;
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
        givenToPlayers.put(player.getUniqueId(), false);
    }

    public void removeFromPlayer(UUID id) {
        givenToPlayers.remove(id);
    }

    public void onSuccess(Player player) {
        if (successReward != null) {
            if (!successReward.pay(player)) {
                return;
            }
        }
        if (successMessage != null) {
            player.sendMessage(successMessage);
        }
        givenToPlayers.put(player.getUniqueId(), true);
        for (ComplexQuest q: superquests) {
            q.update(player);
        }
    }

    public Status getPlayerStatus(UUID id) {
        if (!givenToPlayers.containsKey(id)) return Status.NOTGIVENTO;
        if (givenToPlayers.get(id)) return Status.SUCCESS;
        return Status.GIVENTO;
    }

    public boolean hasSpaceForReward(Player player) {
        return (successReward == null)? true : successReward.hasSpace(player);
    }

    // Alle relevanten Block-Events

    public void onBlockBreakEvent(BlockBreakEvent event) {

    }

    public void onBlockPlaceEvent(BlockPlaceEvent event) {

    }

    // Alle relevanten Entity-Events

    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

    }

    // Alle relevanten Player-Events

    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {

    }

    public void onPlayerMoveEvent(PlayerMoveEvent event) {

    }

    // Alle relevanten NPC-Events
    public void onNPCClickEvent(NPCClickEvent event) {

    }

}
