package de.iani.cubequest.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.base.Verify;

public class ComplexQuest extends Quest {

    private Structure structure;
    private HashSet<Quest> partQuests;
    private Quest failCondition;
    private Quest followupQuest;

    public enum Structure {
        ALLTOBEDONE, ONETOBEDONE
    }

    public ComplexQuest(String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Structure structure, Collection<Quest> partQuests, Quest failCondition, Quest followupQuest) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(structure);
        Verify.verifyNotNull(partQuests);
        Verify.verify(!partQuests.isEmpty());

        this.structure = structure;
        this.partQuests = new HashSet<Quest>(partQuests);
        this.failCondition = failCondition;
        this.followupQuest = followupQuest;

        for (Quest q: partQuests) {
            q.addSuperQuest(this);
        }
        if (failCondition != null) {
            failCondition.addSuperQuest(this);
        }
    }

    public ComplexQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Structure structure, Collection<Quest> partQuests, Quest followupQuest) {
        this(name, giveMessage, successMessage, null, successReward, null, structure, partQuests, null, followupQuest);
    }

    public Structure getStructure() {
        return structure;
    }

    /**
     * @return partQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<Quest> getPartQuests() {
        return Collections.unmodifiableCollection(partQuests);
    }

    public Quest getFollowupQuest() {
        return followupQuest;
    }

    @Override
    public void giveToPlayer(Player player) {
        if (getPlayerStatus(player.getUniqueId()) != Status.NOTGIVENTO) return;
        super.giveToPlayer(player);
        for (Quest q: partQuests) {
            if (q.getPlayerStatus(player.getUniqueId()) == Status.NOTGIVENTO) q.giveToPlayer(player);
            else if (q.getPlayerStatus(player.getUniqueId()) == Status.SUCCESS) update(player);
        }
    }

    @Override
    public boolean onSuccess(Player player) {
        if (!super.onSuccess(player)) {
            return false;
        }
        if (followupQuest != null) {
            followupQuest.giveToPlayer(player);
        }
        for (Quest q: partQuests) {
            if (q.getPlayerStatus(player.getUniqueId()) == Status.GIVENTO) {
                q.removeFromPlayer(player.getUniqueId());
            }
        }
        if (failCondition != null && failCondition.getPlayerStatus(player.getUniqueId()) == Status.GIVENTO) {
            failCondition.removeFromPlayer(player.getUniqueId());
        }
        return true;
    }

    @Override
    public boolean onFail(Player player) {
        if (!super.onFail(player)) {
            return false;
        }
        for (Quest q: partQuests) {
            if (q.getPlayerStatus(player.getUniqueId()) == Status.GIVENTO) {
                q.removeFromPlayer(player.getUniqueId());
            }
        }
        return true;
    }

    @Override
    public void removeFromPlayer(UUID id) {
        if (getPlayerStatus(id) == Status.NOTGIVENTO) return;
        super.removeFromPlayer(id);
        for (Quest q: partQuests) {
            q.removeFromPlayer(id);
        }
    }

    public void update(Player player) {
        if (getPlayerStatus(player.getUniqueId()) != Status.GIVENTO) {
            return;
        }
        if (isSuccessfull(player.getUniqueId())) {
            onSuccess(player);
        }
        if (isFailed(player.getUniqueId())) {
            onFail(player);
        }
    }

    private boolean isSuccessfull(UUID id) {
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) != Status.SUCCESS) return false;
                                }
                                return true;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) == Status.SUCCESS) return true;
                                }
                                return false;
            default: throw new IllegalStateException("Unknown Structure, should not happen!");  // Kompiliert nicht ohne default
        }
    }

    private boolean isFailed(UUID id) {
        if (failCondition != null && failCondition.getPlayerStatus(id) == Status.SUCCESS) {
            return true;
        }
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) == Status.FAIL) return true;
                                }
                                return false;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) != Status.FAIL) return false;
                                }
                                return true;
            default: throw new IllegalStateException("Unknown Structure, should not happen!");  // Kompiliert nicht ohne default
        }
    }

}
