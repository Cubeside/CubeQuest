package de.iani.cubequest.quests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

public abstract class Quest {

    private static HashMap<String, Quest> allQuests = new HashMap<String, Quest>();

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
        if (allQuests.containsKey(name)) throw new IllegalArgumentException("A quest with that name already exists.");

        this.name = name;
        this.giveMessage = giveMessage;
        this.successMessage = successMessage;
        this.upfrontReward = upfrontReward;
        this.successReward = successReward;
        this.superquests = new HashSet<ComplexQuest>();
        this.givenToPlayers = new HashMap<UUID, Boolean>();

        allQuests.put(name, this);
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

    public HashSet<ComplexQuest> getSuperquests() {
        return new HashSet<ComplexQuest>(superquests);
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
