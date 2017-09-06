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

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ComplexQuest extends Quest {

    private Structure structure;
    private HashSet<Quest> partQuests;
    private Quest failCondition;
    private Quest followupQuest;

    private HashSet<Integer> waitingForPartQuests;
    private int waitingForFailCondition = 0;
    private int waitingForFollowupQuest = 0;

    public enum Structure {
        ALLTOBEDONE, ONETOBEDONE;

        public static Structure match(String from) {
            if (from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("ALLTOBEDONE")) {
                return ALLTOBEDONE;
            }
            if (from.equalsIgnoreCase("ONE") || from.equalsIgnoreCase("ONETOBEDONE")) {
                return ONETOBEDONE;
            }
            return null;
        }
    }

    public class CircleInQuestGraphException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        public CircleInQuestGraphException() {
            super();
        }

        public CircleInQuestGraphException(String message, Throwable cause) {
            super(message, cause);
        }

        public CircleInQuestGraphException(String s) {
            super(s);
        }

        public CircleInQuestGraphException(Throwable cause) {
            super(cause);
        }

    }

    public ComplexQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Structure structure, Collection<Quest> partQuests, Quest failCondition, Quest followupQuest) {
        super(id, name, giveMessage, successMessage, successReward);

        Verify.verify(id > 0);

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
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("structure", structure.toString());
        List<Integer> partQuestIdList = new ArrayList<Integer>();
        for (Quest q: partQuests) {
            partQuestIdList.add(q.getId());
        }
        yc.set("partQuests", partQuestIdList);
        yc.set("failCondition", failCondition == null? 0 : failCondition.getId());
        yc.set("followupQuest", followupQuest == null? 0 : followupQuest.getId());

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return structure != null && !partQuests.isEmpty() && (failCondition == null || failCondition.isLegal()) && (followupQuest == null || followupQuest.isLegal())
                && partQuests.stream().allMatch(q -> q.isLegal());
    }

    @Override
    public boolean isReady() {
        if (!super.isReady()) {
            return false;
        }
        if (followupQuest != null && !followupQuest.isReady()) {
            return false;
        }
        if (failCondition != null && !failCondition.isReady()) {
            return false;
        }
        for (Quest q: partQuests) {
            if (!q.isReady()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        ComponentBuilder partQuestsCB = new ComponentBuilder(ChatColor.DARK_AQUA + "Sub-Quests: ");
        if (partQuests.isEmpty()) {
            partQuestsCB.append(ChatColor.RED + "KEINE");
        } else {
            List<Quest> partQuestList = new ArrayList<Quest>(partQuests);
            partQuestList.sort((q1, q2) -> {
                return q1.getId() - q2.getId();
            });

            int i=0;
            int size = partQuestList.size();
            for (Quest quest: partQuestList) {
                partQuestsCB.append(quest.getTypeName() + " [" + quest.getId() + "]" + (!quest.getName().equals("")? " \"" + quest.getName() + "\"" : ""));
                partQuestsCB.color(quest.isLegal()? ChatColor.GREEN : ChatColor.RED);
                partQuestsCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Info zu Quest " + quest.getId()).create()));
                partQuestsCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "cubequest questInfo " + quest.getId()));
                if (i+1 < size) {
                    partQuestsCB.append(", ");
                }
            }
        }

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Struktur: " + (structure == null? ChatColor.RED + "NULL" : "" + ChatColor.GREEN + structure)).create());
        result.add(partQuestsCB.create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    @Override
    public void giveToPlayer(Player player) {
        if (CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(this.getId()) != Status.NOTGIVENTO) {
            return;
        }
        super.giveToPlayer(player);
        for (Quest q: partQuests) {
            Status status = CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(q.getId());
            if (status == Status.NOTGIVENTO) {
                q.giveToPlayer(player);
            } else if (status == Status.SUCCESS) {
                update(player);
            }
        }
        if (failCondition != null) {
            switch(CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(failCondition.getId())) {
                case NOTGIVENTO: failCondition.giveToPlayer(player); break;
                case SUCCESS: onFail(player); break;
                default: break; // nothing
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
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(q.getId())) {
                q.removeFromPlayer(player.getUniqueId());
            }
        }
        if (failCondition != null && CubeQuest.getInstance().getPlayerData(player).isGivenTo(failCondition.getId())) {
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
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(q.getId())) {
                q.removeFromPlayer(player.getUniqueId());
            }
        }
        return true;
    }

    @Override
    public void removeFromPlayer(UUID id) {
        if (CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(this.getId()) == Status.NOTGIVENTO) {
            return;
        }
        super.removeFromPlayer(id);
        for (Quest q: partQuests) {
            q.removeFromPlayer(id);
        }
    }

    @Override
    public boolean onQuestSuccessEvent(QuestSuccessEvent event, QuestState state) {
        if (partQuests.contains(event.getQuest()) || failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean onQuestFailEvent(QuestFailEvent event, QuestState state) {
        if (partQuests.contains(event.getQuest()) || failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure val) {
        this.structure = val;
        updateIfReal();
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
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException("Adding this quest would create circle in quest-graph.");
        }
        if (partQuests.add(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removePartQuest(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        if (partQuests.remove(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearPartQuests() {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        partQuests.clear();
        updateIfReal();
    }

    public Quest getFollowupQuest() {
        return followupQuest;
    }

    public void setFollowupQuest(Quest quest) {
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException("Adding this quest would create circle in quest-graph.");
        }
        followupQuest = quest;
        updateIfReal();
    }

    public Quest getFailCondition() {
        return failCondition;
    }

    public void setFailCondition(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to change failCondition while ready.");
        }
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException("Adding this quest would create circle in quest-graph.");
        }
        failCondition = quest;
        updateIfReal();
    }

    public void update(Player player) {
        if (!CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.getId())) {
            return;
        }
        if (isSuccessfull(player.getUniqueId())) {
            onSuccess(player);
        }
        if (isFailed(player.getUniqueId())) {
            onFail(player);
        }
    }

    public boolean otherQuestWouldCreateCircle(Quest quest) {
        if (quest == this) {
            return true;
        }
        if (quest == null) {
            return false;
        }
        if (!(quest instanceof ComplexQuest)) {
            return false;
        }
        ComplexQuest cQuest = (ComplexQuest) quest;
        return otherQuestWouldCreateCircle(cQuest.followupQuest) || otherQuestWouldCreateCircle(cQuest.failCondition)
                || cQuest.partQuests.stream().anyMatch(q -> otherQuestWouldCreateCircle(q));
    }

    private boolean isSuccessfull(UUID id) {
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(q.getId()) != Status.SUCCESS) {
                                        return false;
                                    }
                                }
                                return true;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(q.getId()) == Status.SUCCESS) {
                                        return true;
                                    }
                                }
                                return false;
            default: throw new NullPointerException();      // structure kann nur noch null sein
        }
    }

    private boolean isFailed(UUID id) {
        if (failCondition != null && CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(failCondition.getId()) == Status.SUCCESS) {
            return true;
        }
        switch(structure) {
            case ALLTOBEDONE:   for (Quest q: partQuests) {
                                    if (CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(q.getId()) == Status.FAIL) {
                                        return true;
                                    }
                                }
                                return false;
            case ONETOBEDONE:   for (Quest q: partQuests) {
                                    if (CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(q.getId()) != Status.FAIL) {
                                        return false;
                                    }
                                }
                                return true;
            default: throw new NullPointerException();      // structure kann nur noch null sein
        }
    }

}
