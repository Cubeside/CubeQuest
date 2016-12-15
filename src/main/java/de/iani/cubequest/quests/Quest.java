package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

public abstract class Quest {

    private String name;
    private String giveMessage;
    private String successMessage;
    private Reward upfrontReward;
    private Reward successReward;
    private HashSet<ComplexQuest> superquests;
    private HashMap<UUID, Boolean> givenToPlayers;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS
    }

    public Quest(String name, String giveMessage, String successMessage, Reward upfrontReward, Reward successReward) {
        if (name == null) throw new NullPointerException("name may not be null");
        //TODO: Abfragen, ob Questname schon existiert

        this.name = name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.upfrontReward = upfrontReward;
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

    public Reward getUpfrontReward() {
        return upfrontReward;
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
        givenToPlayers.put(player.getUniqueId(), false);
        if (giveMessage != null) player.sendMessage(giveMessage);
        if (upfrontReward != null) upfrontReward.pay(player);
    }

    public void removeFromPlayer(UUID id) {
        givenToPlayers.remove(id);
    }

    public void onSuccess(Player player) {
        if (successMessage != null) player.sendMessage(successMessage);
        if (successReward != null) successReward.pay(player);
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

}
