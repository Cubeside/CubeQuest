package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.iani.cubequest.QuestManager;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.questStates.QuestState.Status;

public class ComplexQuest extends Quest {

    private Structure structure;
    private HashSet<Quest> partQuests;
    private Quest failCondition;
    private Quest followupQuest;

    private HashSet<Integer> waitingForPartQuests;
    private int waitingForFailCondition = 0;
    private int waitingForFollowupQuest = 0;

    public enum Structure {
        ALLTOBEDONE, ONETOBEDONE
    }

    public ComplexQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Structure structure, Collection<Quest> partQuests, Quest failCondition, Quest followupQuest) {
        super(id, name, giveMessage, successMessage, successReward);

        this.structure = structure;
        this.partQuests = partQuests == null? new HashSet<Quest>() : new HashSet<Quest>(partQuests);
        this.failCondition = failCondition;
        this.followupQuest = followupQuest;

        waitingForPartQuests = new HashSet<Integer>();
    }

    public ComplexQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Structure structure, Collection<Quest> partQuests, Quest followupQuest) {
        this(id, name, giveMessage, successMessage, null, successReward, null, structure, partQuests, null, followupQuest);
    }

    public ComplexQuest(int id) {
        this(id, null, null, null, null, null, null, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        partQuests.clear();

        structure = Structure.valueOf(yc.getString("structure"));
        List<Integer> partQuestIdList = yc.getIntegerList("partQuests");
        int failConditionId = yc.getInt("failCondition");
        int followupQuestId = yc.getInt("followupQuest");

        for (int i: partQuestIdList) {
            if (i == 0) {
                continue;
            }
            Quest quest = QuestManager.getInstance().getQuest(i);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, i);
                waitingForPartQuests.add(i);
            } else {
                partQuests.add(quest);
            }
        }

        failCondition = null;
        if (failConditionId == 0) {
            Quest quest = QuestManager.getInstance().getQuest(failConditionId);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, failConditionId);
                waitingForFailCondition = failConditionId;
            } else {
                failCondition = quest;
            }
        }
        followupQuest = null;
        if (followupQuestId != 0) {
            Quest quest = QuestManager.getInstance().getQuest(followupQuestId);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, followupQuestId);
                waitingForFollowupQuest = followupQuestId;
            } else {
                followupQuest = quest;
            }
        }
    }

    public void informQuestNowThere(Quest quest) {
        if (waitingForPartQuests.contains(quest.getId())) {
            partQuests.add(quest);
            waitingForPartQuests.remove(quest.getId());
        }
        if (quest.getId() == waitingForFailCondition) {
            failCondition = quest;
            waitingForFailCondition = 0;
        }
        if (quest.getId() == waitingForFollowupQuest) {
            followupQuest = quest;
            waitingForFollowupQuest = 0;
        }
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("structure", structure.toString());
        List<Integer> partQuestIdList = new ArrayList<Integer>();
        for (Quest q: partQuests) {
            partQuestIdList.add(q.getId());
        }
        yc.set("partQuests", partQuestIdList);
        yc.set("failCondition", failCondition == null? 0 : failCondition.getId());
        yc.set("followupQuest", followupQuest == null? 0 : followupQuest.getId());

        return super.serialize(yc);
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure val) {
        this.structure = val;
    }

    /**
     * @return partQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<Quest> getPartQuests() {
        return Collections.unmodifiableCollection(partQuests);
    }

    public boolean addPartQuest(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to add partQuests while ready.");
        }
        return partQuests.add(quest);
    }

    public boolean removePartQuest(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        return partQuests.remove(quest);
    }

    public void clearPartQuests() {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        partQuests.clear();
    }

    public Quest getFollowupQuest() {
        return followupQuest;
    }

    public void setFollowupQuest(Quest quest) {
        followupQuest = quest;
    }

    public Quest getFailCondition() {
        return failCondition;
    }

    public void setFailCondition(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to change failCondition while ready.");
        }
        failCondition = quest;
    }

    @Override
    public boolean isLegal() {
        return structure != null && !partQuests.isEmpty();
    }

    @Override
    public void giveToPlayer(Player player) {
        if (getPlayerStatus(player.getUniqueId()) != Status.NOTGIVENTO) {
            return;
        }
        super.giveToPlayer(player);
        for (Quest q: partQuests) {
            if (q.getPlayerStatus(player.getUniqueId()) == Status.NOTGIVENTO) {
                q.giveToPlayer(player);
            } else if (q.getPlayerStatus(player.getUniqueId()) == Status.SUCCESS) {
                update(player);
            }
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
        if (getPlayerStatus(id) == Status.NOTGIVENTO) {
            return;
        }
        super.removeFromPlayer(id);
        for (Quest q: partQuests) {
            q.removeFromPlayer(id);
        }
    }

    @Override
    public boolean onQuestSuccessEvent(QuestSuccessEvent event) {
        if (partQuests.contains(event.getQuest()) || failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean onQuestFailEvent(QuestFailEvent event) {
        if (partQuests.contains(event.getQuest()) || failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
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
                                    if (q.getPlayerStatus(id) != Status.SUCCESS) {
                                        return false;
                                    }
                                }
                                return true;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) == Status.SUCCESS) {
                                        return true;
                                    }
                                }
                                return false;
            default: throw new NullPointerException();      // structure kann nur noch null sein
        }
    }

    private boolean isFailed(UUID id) {
        if (failCondition != null && failCondition.getPlayerStatus(id) == Status.SUCCESS) {
            return true;
        }
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) == Status.FAIL) {
                                        return true;
                                    }
                                }
                                return false;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (q.getPlayerStatus(id) != Status.FAIL) {
                                        return false;
                                    }
                                }
                                return true;
            default: throw new NullPointerException();      // structure kann nur noch null sein
        }
    }

}
