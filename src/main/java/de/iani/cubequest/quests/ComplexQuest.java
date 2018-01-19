package de.iani.cubequest.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.events.QuestDeleteEvent;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

@DelegateDeserialization(Quest.class)
public class ComplexQuest extends Quest {
    
    private Structure structure;
    private HashSet<Quest> partQuests;
    private Quest failCondition;
    private Quest followupQuest;
    
    private HashSet<Integer> waitingForPartQuests;
    private int waitingForFailCondition = 0;
    private int waitingForFollowupQuest = 0;
    
    private boolean onDeleteCascade = false;
    private boolean deletionInProgress = false;
    
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
    
    public ComplexQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            Structure structure, Collection<Quest> partQuests, Quest failCondition,
            Quest followupQuest) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        
        Verify.verify(id > 0);
        
        this.structure = structure;
        this.partQuests = partQuests == null ? new HashSet<>() : new HashSet<>(partQuests);
        this.failCondition = failCondition;
        this.followupQuest = followupQuest;
        
        this.waitingForPartQuests = new HashSet<>();
    }
    
    public ComplexQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Structure structure,
            Collection<Quest> partQuests, Quest followupQuest) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                structure, partQuests, null, followupQuest);
    }
    
    public ComplexQuest(int id) {
        this(id, null, null, null, null, null, null, null, null);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.partQuests.clear();
        
        this.structure = Structure.valueOf(yc.getString("structure"));
        
        this.onDeleteCascade =
                yc.contains("onDeleteCascade") ? yc.getBoolean("onDeleteCascade") : false;
        
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
                this.waitingForPartQuests.add(i);
            } else {
                this.partQuests.add(quest);
            }
        }
        
        this.failCondition = null;
        if (failConditionId != 0) {
            Quest quest = QuestManager.getInstance().getQuest(failConditionId);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, failConditionId);
                this.waitingForFailCondition = failConditionId;
            } else {
                this.failCondition = quest;
            }
        }
        this.followupQuest = null;
        if (followupQuestId != 0) {
            Quest quest = QuestManager.getInstance().getQuest(followupQuestId);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, followupQuestId);
                this.waitingForFollowupQuest = followupQuestId;
            } else {
                this.followupQuest = quest;
            }
        }
    }
    
    public void informQuestNowThere(Quest quest) {
        if (this.waitingForPartQuests.contains(quest.getId())) {
            this.partQuests.add(quest);
            this.waitingForPartQuests.remove(quest.getId());
        }
        if (quest.getId() == this.waitingForFailCondition) {
            this.failCondition = quest;
            this.waitingForFailCondition = 0;
        }
        if (quest.getId() == this.waitingForFollowupQuest) {
            this.followupQuest = quest;
            this.waitingForFollowupQuest = 0;
        }
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("structure", this.structure.toString());
        List<Integer> partQuestIdList = new ArrayList<>();
        for (Quest q: this.partQuests) {
            partQuestIdList.add(q.getId());
        }
        yc.set("partQuests", partQuestIdList);
        yc.set("failCondition", this.failCondition == null ? 0 : this.failCondition.getId());
        yc.set("followupQuest", this.followupQuest == null ? 0 : this.followupQuest.getId());
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return this.structure != null && !this.partQuests.isEmpty()
                && (this.failCondition == null || this.failCondition.isLegal())
                && (this.followupQuest == null || this.followupQuest.isLegal())
                && this.partQuests.stream().allMatch(q -> q.isLegal());
    }
    
    @Override
    public boolean isReady() {
        if (!super.isReady()) {
            return false;
        }
        if (this.followupQuest != null && !this.followupQuest.isReady()) {
            return false;
        }
        if (this.failCondition != null && !this.failCondition.isReady()) {
            return false;
        }
        for (Quest q: this.partQuests) {
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
        if (this.partQuests.isEmpty()) {
            partQuestsCB.append(ChatColor.RED + "KEINE");
        } else {
            List<Quest> partQuestList = new ArrayList<>(this.partQuests);
            partQuestList.sort((q1, q2) -> {
                return q1.getId() - q2.getId();
            });
            
            int i = 0;
            int size = partQuestList.size();
            for (Quest quest: partQuestList) {
                partQuestsCB.append(quest.getTypeName() + " [" + quest.getId() + "]"
                        + (!quest.getName().equals("") ? " \"" + quest.getName() + "\"" : ""));
                partQuestsCB.color(quest.isLegal() ? ChatColor.GREEN : ChatColor.RED);
                partQuestsCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Info zu Quest " + quest.getId()).create()));
                partQuestsCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "cubequest questInfo " + quest.getId()));
                if (i + 1 < size) {
                    partQuestsCB.append(", ");
                }
            }
        }
        
        ComponentBuilder failConditionCB =
                new ComponentBuilder(ChatColor.DARK_AQUA + "Fail-Condition: ");
        if (this.failCondition == null) {
            failConditionCB.append(ChatColor.GOLD + "NULL");
        } else {
            failConditionCB.append(
                    this.failCondition.getTypeName() + " [" + this.failCondition.getId() + "]"
                            + (!this.failCondition.getName().equals("")
                                    ? " \"" + this.failCondition.getName() + "\""
                                    : ""));
            failConditionCB.color(this.failCondition.isLegal() ? ChatColor.GREEN : ChatColor.RED);
            failConditionCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Info zu Quest " + this.failCondition.getId()).create()));
            failConditionCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "cubequest questInfo " + this.failCondition.getId()));
        }
        
        ComponentBuilder followupQuestCB =
                new ComponentBuilder(ChatColor.DARK_AQUA + "Followup-Quest: ");
        if (this.followupQuest == null) {
            followupQuestCB.append(ChatColor.GOLD + "NULL");
        } else {
            followupQuestCB.append(
                    this.followupQuest.getTypeName() + " [" + this.followupQuest.getId() + "]"
                            + (!this.followupQuest.getName().equals("")
                                    ? " \"" + this.followupQuest.getName() + "\""
                                    : ""));
            followupQuestCB.color(this.followupQuest.isLegal() ? ChatColor.GREEN : ChatColor.RED);
            followupQuestCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Info zu Quest " + this.followupQuest.getId()).create()));
            followupQuestCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "cubequest questInfo " + this.followupQuest.getId()));
        }
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Struktur: "
                + (this.structure == null ? ChatColor.RED + "NULL"
                        : "" + ChatColor.GREEN + this.structure)).create());
        result.add(partQuestsCB.create());
        result.add(failConditionCB.create());
        result.add(followupQuestCB.create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public void giveToPlayer(Player player) {
        if (CubeQuest.getInstance().getPlayerData(player)
                .getPlayerStatus(getId()) != Status.NOTGIVENTO) {
            return;
        }
        super.giveToPlayer(player);
        for (Quest q: this.partQuests) {
            Status status =
                    CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(q.getId());
            if (status == Status.NOTGIVENTO) {
                q.giveToPlayer(player);
            } else if (status == Status.SUCCESS) {
                update(player);
            }
        }
        if (this.failCondition != null) {
            switch (CubeQuest.getInstance().getPlayerData(player)
                    .getPlayerStatus(this.failCondition.getId())) {
                case NOTGIVENTO:
                    this.failCondition.giveToPlayer(player);
                    break;
                case SUCCESS:
                    onFail(player);
                    break;
                default:
                    break; // nothing
            }
        }
    }
    
    @Override
    public boolean onSuccess(Player player) {
        if (!super.onSuccess(player)) {
            return false;
        }
        if (this.followupQuest != null) {
            this.followupQuest.giveToPlayer(player);
        }
        for (Quest q: this.partQuests) {
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(q.getId())) {
                q.removeFromPlayer(player.getUniqueId());
            }
        }
        if (this.failCondition != null && CubeQuest.getInstance().getPlayerData(player)
                .isGivenTo(this.failCondition.getId())) {
            this.failCondition.removeFromPlayer(player.getUniqueId());
        }
        return true;
    }
    
    @Override
    public boolean onFail(Player player) {
        if (!super.onFail(player)) {
            return false;
        }
        for (Quest q: this.partQuests) {
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(q.getId())) {
                q.onFail(player);
            }
        }
        return true;
    }
    
    @Override
    public void removeFromPlayer(UUID id) {
        if (CubeQuest.getInstance().getPlayerData(id)
                .getPlayerStatus(getId()) == Status.NOTGIVENTO) {
            return;
        }
        super.removeFromPlayer(id);
        for (Quest q: this.partQuests) {
            q.removeFromPlayer(id);
        }
    }
    
    @Override
    public void onDeletion() {
        this.deletionInProgress = true;
        
        if (this.onDeleteCascade) {
            for (Quest q: this.partQuests) {
                if (!QuestManager.getInstance().deleteQuest(q)) {
                    throw new RuntimeException(
                            "Could not cascade deletion from quest " + this + " to " + q + ".");
                }
            }
            
            if (!QuestManager.getInstance().deleteQuest(this.failCondition)) {
                throw new RuntimeException("Could not cascade deletion from quest " + this + " to "
                        + this.failCondition + ".");
            }
            
            if (!QuestManager.getInstance().deleteQuest(this.followupQuest)) {
                throw new RuntimeException("Could not cascade deletion from quest " + this + " to "
                        + this.followupQuest + ".");
            }
        }
    }
    
    @Override
    public boolean onQuestSuccessEvent(QuestSuccessEvent event, QuestState state) {
        if (this.partQuests.contains(event.getQuest()) || this.failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onQuestFailEvent(QuestFailEvent event, QuestState state) {
        if (this.partQuests.contains(event.getQuest()) || this.failCondition == event.getQuest()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onQuestDeleteEvent(QuestDeleteEvent event) {
        if (this.deletionInProgress) {
            return false;
        }
        
        if (this.partQuests.contains(event.getQuest())) {
            throw new IllegalStateException(
                    "Quest " + event.getQuest() + " is still part of " + this + "!");
        }
        
        if (this.failCondition == event.getQuest()) {
            throw new IllegalStateException(
                    "Quest " + event.getQuest() + " is still failCondition of " + this + "!");
        }
        
        if (this.followupQuest == event.getQuest()) {
            throw new IllegalStateException(
                    "Quest " + event.getQuest() + " is still followupQuest of " + this + "!");
        }
        
        return false;
    }
    
    @Override
    public boolean onQuestWouldBeDeletedEvent(QuestWouldBeDeletedEvent event) {
        if (this.deletionInProgress) {
            return false;
        }
        
        boolean result = false;
        Quest quest = event.getQuest();
        
        if (this.partQuests.contains(event.getQuest())) {
            result = true;
            CubeQuest.getInstance()
                    .addStoredMessage("Quest " + quest + " is part of " + this + ".");
        }
        
        if (this.failCondition == event.getQuest()) {
            result = true;
            CubeQuest.getInstance()
                    .addStoredMessage("Quest " + quest + " is failCondition of " + this + ".");
        }
        
        if (this.followupQuest == event.getQuest()) {
            result = true;
            CubeQuest.getInstance()
                    .addStoredMessage("Quest " + quest + " is followupQuest of " + this + ".");
        }
        
        if (result) {
            event.setCancelled(true);
        }
        
        return result;
    }
    
    public Structure getStructure() {
        return this.structure;
    }
    
    public void setStructure(Structure val) {
        this.structure = val;
        updateIfReal();
    }
    
    public boolean isOnDelteCascade() {
        return this.onDeleteCascade;
    }
    
    public void setOnDeleteCascade(boolean val) {
        this.onDeleteCascade = val;
        updateIfReal();
    }
    
    /**
     * @return partQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<Quest> getPartQuests() {
        return Collections.unmodifiableCollection(this.partQuests);
    }
    
    public boolean addPartQuest(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to add partQuests while ready.");
        }
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException(
                    "Adding this quest would create circle in quest-graph.");
        }
        if (this.partQuests.add(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public boolean removePartQuest(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        if (this.partQuests.remove(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }
    
    public void clearPartQuests() {
        if (isReady()) {
            throw new IllegalStateException("Impossible to remove partQuests while ready.");
        }
        this.partQuests.clear();
        updateIfReal();
    }
    
    public Quest getFollowupQuest() {
        return this.followupQuest;
    }
    
    public void setFollowupQuest(Quest quest) {
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException(
                    "Adding this quest would create circle in quest-graph.");
        }
        this.followupQuest = quest;
        updateIfReal();
    }
    
    public Quest getFailCondition() {
        return this.failCondition;
    }
    
    public void setFailCondition(Quest quest) {
        if (isReady()) {
            throw new IllegalStateException("Impossible to change failCondition while ready.");
        }
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException(
                    "Adding this quest would create circle in quest-graph.");
        }
        this.failCondition = quest;
        updateIfReal();
    }
    
    public void update(Player player) {
        if (!CubeQuest.getInstance().getPlayerData(player).isGivenTo(getId())) {
            return;
        }
        if (isFailed(player.getUniqueId())/*
                                           * && CubeQuest.getInstance().getPlayerData(player).
                                           * isGivenTo( getId())
                                           */) {
            onFail(player);
        } else if (isSuccessfull(player.getUniqueId())/*
                                                       * && CubeQuest.getInstance().getPlayerData(
                                                       * player).isGivenTo(getId())
                                                       */) {
            onSuccess(player);
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
        return otherQuestWouldCreateCircle(cQuest.followupQuest)
                || otherQuestWouldCreateCircle(cQuest.failCondition)
                || cQuest.partQuests.stream().anyMatch(q -> otherQuestWouldCreateCircle(q));
    }
    
    private boolean isSuccessfull(UUID id) {
        switch (this.structure) {
            case ALLTOBEDONE:
                for (Quest q: this.partQuests) {
                    if (CubeQuest.getInstance().getPlayerData(id)
                            .getPlayerStatus(q.getId()) != Status.SUCCESS) {
                        return false;
                    }
                }
                return true;
            case ONETOBEDONE:
                for (Quest q: this.partQuests) {
                    if (CubeQuest.getInstance().getPlayerData(id)
                            .getPlayerStatus(q.getId()) == Status.SUCCESS) {
                        return true;
                    }
                }
                return false;
            default:
                throw new NullPointerException(); // structure kann nur noch null sein
        }
    }
    
    private boolean isFailed(UUID id) {
        if (this.failCondition != null && CubeQuest.getInstance().getPlayerData(id)
                .getPlayerStatus(this.failCondition.getId()) == Status.SUCCESS) {
            return true;
        }
        switch (this.structure) {
            case ALLTOBEDONE:
                for (Quest q: this.partQuests) {
                    if (CubeQuest.getInstance().getPlayerData(id)
                            .getPlayerStatus(q.getId()) == Status.FAIL) {
                        return true;
                    }
                }
                return false;
            case ONETOBEDONE:
                for (Quest q: this.partQuests) {
                    if (CubeQuest.getInstance().getPlayerData(id)
                            .getPlayerStatus(q.getId()) != Status.FAIL) {
                        return false;
                    }
                }
                return true;
            default:
                throw new NullPointerException(); // structure kann nur noch null sein
        }
    }
    
}
