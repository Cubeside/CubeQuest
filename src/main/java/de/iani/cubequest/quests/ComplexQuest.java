package de.iani.cubequest.quests;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.commands.AddOrRemoveSubQuestCommand;
import de.iani.cubequest.commands.QuestStateInfoCommand;
import de.iani.cubequest.commands.SetAchievementQuestCommand;
import de.iani.cubequest.commands.SetComplexQuestStructureCommand;
import de.iani.cubequest.commands.SetFailAfterSemiSuccessCommand;
import de.iani.cubequest.commands.SetFollowupRequiredForSuccessCommand;
import de.iani.cubequest.commands.SetOrRemoveFailureQuestCommand;
import de.iani.cubequest.commands.SetOrRemoveFollowupQuestCommand;
import de.iani.cubequest.events.QuestDeleteEvent;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestFreezeEvent;
import de.iani.cubequest.events.QuestSetReadyEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.questStates.WaitForTimeQuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import de.iani.cubesideutils.RandomUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;

@DelegateDeserialization(Quest.class)
public class ComplexQuest extends Quest {

    private Structure structure;
    private Set<Quest> subQuests;
    private Quest failCondition;
    private Quest followupQuest;

    private boolean followupRequiredForSuccess;
    private boolean failAfterSemiSuccess;

    private boolean achievement;

    // nicht-persistenter zustand

    private Set<Integer> waitingForPartQuests;
    private int waitingForFailCondition;
    private int waitingForFollowupQuest;

    private boolean deletionInProgress;

    public enum Structure {

        ALL_TO_BE_DONE, ONE_TO_BE_DONE, RANDOM_BE_DONE;

        public static Structure match(String from) {
            from = from.toUpperCase().replaceAll(Pattern.quote("_"), "");
            if (from.equalsIgnoreCase("ALL") || from.equalsIgnoreCase("ALLTOBEDONE")) {
                return ALL_TO_BE_DONE;
            }
            if (from.equalsIgnoreCase("ONE") || from.equalsIgnoreCase("ONETOBEDONE")) {
                return ONE_TO_BE_DONE;
            }
            if (from.equalsIgnoreCase("RANDOM") || from.equals("RANDOMBEDONE")) {
                return RANDOM_BE_DONE;
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

    public ComplexQuest(int id, String name, String displayMessage, Structure structure, Collection<Quest> partQuests,
            Quest failCondition, Quest followupQuest) {
        super(id, name, displayMessage);

        Verify.verify(id > 0);

        this.structure = structure;
        this.subQuests = partQuests == null ? new LinkedHashSet<>() : new LinkedHashSet<>(partQuests);
        this.failCondition = failCondition;
        this.followupQuest = followupQuest;

        this.followupRequiredForSuccess = true;
        this.failAfterSemiSuccess = false;

        this.waitingForPartQuests = new LinkedHashSet<>();
        this.waitingForFailCondition = 0;
        this.waitingForFollowupQuest = 0;

        this.deletionInProgress = false;

        this.achievement = false;
    }

    public ComplexQuest(int id, String name, String displayMessage, Structure structure, Collection<Quest> partQuests,
            Quest followupQuest) {
        this(id, name, displayMessage, structure, partQuests, null, followupQuest);
    }

    public ComplexQuest(int id) {
        this(id, null, null, Structure.ALL_TO_BE_DONE, null, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.subQuests.clear();

        this.structure = yc.get("structure") == null ? null : Structure.match(yc.getString("structure"));
        this.followupRequiredForSuccess = yc.getBoolean("followupRequiredForSuccess", false);
        this.failAfterSemiSuccess = yc.getBoolean("failAfterSemiSuccess", false);

        this.achievement = yc.getBoolean("achievement", false);

        List<Integer> partQuestIdList = yc.getIntegerList("partQuests");
        int failConditionId = yc.getInt("failCondition");
        int followupQuestId = yc.getInt("followupQuest");

        for (int i : partQuestIdList) {
            if (i == 0) {
                continue;
            }
            Quest quest = QuestManager.getInstance().getQuest(i);
            if (quest == null) {
                QuestManager.getInstance().registerWaitingForQuest(this, i);
                this.waitingForPartQuests.add(i);
            } else {
                this.subQuests.add(quest);
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
            this.subQuests.add(quest);
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
        yc.set("structure", this.structure == null ? null : this.structure.toString());
        yc.set("followupRequiredForSuccess", this.followupRequiredForSuccess);
        yc.set("failAfterSemiSuccess", this.failAfterSemiSuccess);
        yc.set("achievement", this.achievement);
        List<Integer> partQuestIdList = new ArrayList<>();
        for (Quest q : this.subQuests) {
            partQuestIdList.add(q.getId());
        }
        yc.set("partQuests", partQuestIdList);
        yc.set("failCondition", this.failCondition == null ? 0 : this.failCondition.getId());
        yc.set("followupQuest", this.followupQuest == null ? 0 : this.followupQuest.getId());

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return this.structure != null && !this.subQuests.isEmpty()
                && (this.failCondition == null || this.failCondition.isLegal())
                && this.subQuests.stream().allMatch(q -> q.isLegal())
                && (this.followupQuest != null || !this.followupRequiredForSuccess);
    }

    @Override
    public boolean isReady() {
        if (!super.isReady()) {
            return false;
        }
        if (this.failCondition != null && !this.failCondition.isReady()) {
            return false;
        }
        for (Quest q : this.subQuests) {
            if (!q.isReady()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setReady(boolean val) {
        super.setReady(val);

        for (Quest q : this.subQuests) {
            q.setReady(val);
        }
        if (this.failCondition != null) {
            this.failCondition.setReady(val);
        }
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        ComponentBuilder partQuestsCB = new ComponentBuilder(ChatColor.DARK_AQUA + "Sub-Quests: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + AddOrRemoveSubQuestCommand.FULL_ADD_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT);
        if (this.subQuests.isEmpty()) {
            partQuestsCB.append(ChatColor.RED + "KEINE");
        } else {
            List<Quest> partQuestList = new ArrayList<>(this.subQuests);
            partQuestList.sort((q1, q2) -> {
                return q1.getId() - q2.getId();
            });

            int i = 0;
            int size = partQuestList.size();
            for (Quest quest : partQuestList) {
                partQuestsCB.append(quest.getTypeName() + " [" + quest.getId() + "]"
                        + (!quest.getInternalName().equals("") ? " \"" + quest.getInternalName() + "\"" : ""));
                partQuestsCB.color(quest.isLegal() ? ChatColor.GREEN : ChatColor.RED);
                partQuestsCB
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Info zu Quest " + quest.getId())));
                partQuestsCB
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest questInfo " + quest.getId()));
                if (i + 1 < size) {
                    partQuestsCB.append(", ");
                }
            }
        }

        ComponentBuilder failConditionCB = new ComponentBuilder(ChatColor.DARK_AQUA + "Fail-Condition: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetOrRemoveFailureQuestCommand.FULL_SET_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT);
        if (this.failCondition == null) {
            failConditionCB.append(ChatColor.GOLD + "NULL");
        } else {
            failConditionCB.append(this.failCondition.getTypeName() + " [" + this.failCondition.getId() + "]"
                    + (!this.failCondition.getInternalName().equals("")
                            ? " \"" + this.failCondition.getInternalName() + "\""
                            : ""));
            failConditionCB.color(this.failCondition.isLegal() ? ChatColor.GREEN : ChatColor.RED);
            failConditionCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("Info zu Quest " + this.failCondition.getId())));
            failConditionCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/cubequest questInfo " + this.failCondition.getId()));
        }

        ComponentBuilder followupQuestCB = new ComponentBuilder(ChatColor.DARK_AQUA + "Followup-Quest: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetOrRemoveFollowupQuestCommand.FULL_SET_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT);
        if (this.followupQuest == null) {
            followupQuestCB.append((this.followupRequiredForSuccess ? ChatColor.RED : ChatColor.GOLD) + "NULL");
        } else {
            followupQuestCB.append(this.followupQuest.getTypeName() + " [" + this.followupQuest.getId() + "]"
                    + (!this.followupQuest.getInternalName().equals("")
                            ? " \"" + this.followupQuest.getInternalName() + "\""
                            : ""));
            followupQuestCB.color(this.followupQuest.isLegal() ? ChatColor.GREEN : ChatColor.GOLD);
            followupQuestCB.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("Info zu Quest " + this.followupQuest.getId())));
            followupQuestCB.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/cubequest questInfo " + this.followupQuest.getId()));
        }

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Struktur: "
                + (this.structure == null ? ChatColor.RED + "NULL" : "" + ChatColor.GREEN + this.structure)).event(
                        new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetComplexQuestStructureCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(partQuestsCB.create());
        result.add(failConditionCB.create());
        result.add(followupQuestCB.create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Followup notwendig für Erfolg: "
                + (this.followupRequiredForSuccess ? ChatColor.GREEN : ChatColor.GOLD)
                + this.followupRequiredForSuccess).event(
                        new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetFollowupRequiredForSuccessCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Fail-Condition auch in Followup: "
                + (this.failAfterSemiSuccess ? ChatColor.GREEN : ChatColor.GOLD) + this.failAfterSemiSuccess).event(
                        new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetFailAfterSemiSuccessCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Ist Achievement-Quest: "
                + (this.achievement ? ChatColor.GREEN + "true" : ChatColor.GOLD + "false"))
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetAchievementQuestCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    @Override
    public List<BaseComponent[]> buildSpecificStateInfo(PlayerData data, boolean unmasked, int indentionLevel) {
        indentionLevel %= 5;

        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());

        String subquestsDoneString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);

        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            // subquestsDoneString += Quest.INDENTION;
        } else {
            subquestsDoneString += ChatAndTextUtil.getStateStringStartingToken(state) + " ";
        }

        Collection<Quest> relevantSubQuests = this.subQuests;
        if (this.structure == Structure.RANDOM_BE_DONE) {
            relevantSubQuests = relevantSubQuests.stream()
                    .filter(quest -> data.getPlayerStatus(quest.getId()) == Status.GIVENTO).toList();
        }

        subquestsDoneString +=
                ChatColor.DARK_AQUA + (relevantSubQuests.size() == 1 ? "Die folgende Quest abgeschlossen:"
                        : ((this.structure == Structure.ALL_TO_BE_DONE ? "Alle" : "Eine der")
                                + " folgenden Quests abgeschlossen:"));
        result.add(new ComponentBuilder(subquestsDoneString).create());

        for (Quest quest : relevantSubQuests) {
            if (unmasked || quest.displayStateInComplex()) {
                result.addAll(getSubQuestStateInfo(quest, data, unmasked, indentionLevel + 1));
            }
        }

        if (this.failAfterSemiSuccess) {
            result.addAll(getFollowupQuestsStateInfo(this.followupQuest, data, unmasked, indentionLevel));
            result.addAll(getFailureQuestsStateInfo(this.followupQuest, data, unmasked, indentionLevel));
        } else {
            result.addAll(getFailureQuestsStateInfo(this.followupQuest, data, unmasked, indentionLevel));
            result.addAll(getFollowupQuestsStateInfo(this.followupQuest, data, unmasked, indentionLevel));
        }

        return result;
    }

    private List<BaseComponent[]> getFollowupQuestsStateInfo(Quest quest, PlayerData data, boolean unmasked,
            int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        if (this.followupRequiredForSuccess && (unmasked || this.followupQuest.displayStateInComplex())
                && data.getPlayerStatus(this.followupQuest.getId()) != Status.NOTGIVENTO) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatColor.DARK_AQUA + "Anschließend folgende Quest abgeschlossen:").create());
            result.addAll(getSubQuestStateInfo(this.followupQuest, data, unmasked, indentionLevel));
        }
        return result;
    }

    private List<BaseComponent[]> getFailureQuestsStateInfo(Quest quest, PlayerData data, boolean unmasked,
            int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        if (this.failCondition != null && (unmasked || this.failCondition.displayStateInComplex())) {
            QuestState failState = data.getPlayerState(this.failCondition.getId());

            if (this.failCondition instanceof WaitForDateQuest) {
                result.add(new ComponentBuilder(getWaitForFailDateString(failState, indentionLevel)).create());
            } else if (this.failCondition instanceof WaitForTimeQuest) {
                result.add(new ComponentBuilder(
                        getWaitForFailTimeString((WaitForTimeQuestState) failState, indentionLevel)).create());
            } else {
                Status failStatus = failState == null ? Status.NOTGIVENTO : failState.getStatus();
                String failString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel) + ChatColor.DARK_AQUA
                        + "Nicht die folgende Quest abgeschlossen: ";
                failString += failStatus.invert().color + (failStatus != Status.SUCCESS ? "ja" : "nein");
                result.add(new ComponentBuilder(failString).create());

                result.addAll(getSubQuestStateInfo(this.failCondition, data, unmasked, indentionLevel + 1));
            }
        }
        return result;
    }

    private List<BaseComponent[]> getSubQuestStateInfo(Quest quest, PlayerData data, boolean unmasked,
            int indentionLevel) {
        if (!quest.isVisible() || quest.getOverwrittenStateMessage() != null) {
            return quest.getSpecificStateInfo(data, unmasked, indentionLevel);
        }

        String nameString = quest.getDisplayName();
        nameString = ChatAndTextUtil.stripColors(nameString).isEmpty() ? String.valueOf(quest.getId()) : nameString;

        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Fortschritt anzeigen"));
        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/" + QuestStateInfoCommand.NORMAL_FULL_COMMAND + " " + data.getId() + " " + quest.getId());

        TextComponent mainComponent =
                new TextComponent(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                        + ChatAndTextUtil.getStateStringStartingToken(data.getPlayerState(quest.getId())) + " ")
                                .append(TextComponent.fromLegacyText(ChatColor.GOLD + nameString)).create());
        mainComponent.setHoverEvent(he);
        mainComponent.setClickEvent(ce);
        return Collections.singletonList(new BaseComponent[] {mainComponent});
    }

    private String getWaitForFailDateString(QuestState failState, int indentionLevel) {
        String result = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1)
                + (failState.getStatus() != Status.SUCCESS ? ChatColor.DARK_AQUA + "Läuft ab am "
                        : ChatColor.RED + "Abgelaufen am ");
        result += ChatAndTextUtil.formatDate(((WaitForDateQuest) this.failCondition).getDate());
        return result;
    }

    private String getWaitForFailTimeString(WaitForTimeQuestState failState, int indentionLevel) {
        if (failState == null) {
            return ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1) + ChatColor.DARK_AQUA + "Läuft nach "
                    + ChatAndTextUtil.formatTimespan(((WaitForTimeQuest) this.failCondition).getTime(), " Tagen",
                            " Stunden", " Minuten", " Sekunden", ", ", " und ")
                    + " ab.";
        }

        String result = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel + 1)
                + (failState.getStatus() != Status.SUCCESS ? ChatColor.DARK_AQUA + "Läuft ab am "
                        : ChatColor.RED + "Abgelaufen am ");
        result += ChatAndTextUtil.formatDate(failState.getGoal());
        return result;
    }

    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);
        if (this.followupRequiredForSuccess) {
            this.followupQuest.removeFromPlayer(player.getUniqueId());
        }
        if (this.structure == Structure.RANDOM_BE_DONE) {
            RandomUtil.randomElement(this.subQuests.stream().toList()).giveToPlayer(player);
        } else {
            for (Quest q : this.subQuests) {
                q.giveToPlayer(player);
            }
        }
        if (this.failCondition != null) {
            this.failCondition.giveToPlayer(player);
        }
    }

    @Override
    public void removeFromPlayer(UUID id) {
        super.removeFromPlayer(id);
        for (Quest q : this.subQuests) {
            q.removeFromPlayer(id);
        }
        if (this.failCondition != null) {
            this.failCondition.removeFromPlayer(id);
        }
        if (this.followupRequiredForSuccess) {
            Status followupStatus = this.followupQuest == null ? Status.NOTGIVENTO
                    : CubeQuest.getInstance().getPlayerData(id).getPlayerStatus(this.followupQuest.getId());
            if (followupStatus != Status.NOTGIVENTO) {
                this.followupQuest.removeFromPlayer(id);
            }
        }
    }

    @Override
    public boolean onFreeze(Player player) {
        if (!super.onFreeze(player)) {
            return false;
        }

        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        for (Quest quest : this.subQuests) {
            if (data.isGivenTo(quest.getId())) {
                quest.onFreeze(player);
            }
        }

        if (this.failCondition != null && data.isGivenTo(this.failCondition.getId())) {
            this.failCondition.onFreeze(player);
        }

        if (this.followupRequiredForSuccess && data.isGivenTo(this.followupQuest.getId())) {
            this.followupQuest.onFreeze(player);
        }

        return true;
    }

    @Override
    public boolean onSuccess(Player player) {
        if (!super.onSuccess(player)) {
            return false;
        }

        freezeFailCondition(player);
        return true;
    }

    @Override
    public boolean onFail(Player player) {
        if (!super.onFail(player)) {
            return false;
        }
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        for (Quest q : this.subQuests) {
            if (data.isGivenTo(q.getId())) {
                q.onFail(player);
            }
        }
        if (this.followupRequiredForSuccess && data.isGivenTo(this.followupQuest.getId())) {
            this.followupQuest.onFail(player);
        }
        return true;
    }

    @Override
    public void onDeletion(boolean cascading) throws QuestDeletionFailedException {
        this.deletionInProgress = true;
        super.onDeletion(cascading);

        if (cascading) {
            for (Quest q : this.subQuests) {
                for (String s : QuestManager.getInstance().deleteQuest(q, true, false)) {
                    CubeQuest.getInstance().addStoredMessage(s);
                }
            }

            if (this.failCondition != null) {
                for (String s : QuestManager.getInstance().deleteQuest(this.failCondition, true, false)) {
                    CubeQuest.getInstance().addStoredMessage(s);
                }
            }

            if (this.followupQuest != null) {
                for (String s : QuestManager.getInstance().deleteQuest(this.followupQuest, true, false)) {
                    CubeQuest.getInstance().addStoredMessage(s);
                }
            }
        }
    }

    @Override
    protected long getLastAction(PlayerData pData) {
        long lastAction = pData.getPlayerState(getId()).getLastAction();

        for (Quest q : this.subQuests) {
            lastAction = Math.max(lastAction, q.getLastAction(pData));
        }

        if (this.failCondition != null) {
            lastAction = Math.max(lastAction, this.failCondition.getLastAction(pData));
        }
        if (this.followupQuest != null && this.followupRequiredForSuccess) {
            lastAction = Math.max(lastAction, this.followupQuest.getLastAction(pData));
        }

        return lastAction;
    }

    @Override
    public boolean onQuestSuccessEvent(QuestSuccessEvent event, QuestState state) {
        if (isRelevant(event.getQuest()) && !event.isAutoRegiven()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean onQuestFailEvent(QuestFailEvent event, QuestState state) {
        if (isRelevant(event.getQuest()) && !event.isAutoRegiven()) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public boolean onQuestFreezeEvent(QuestFreezeEvent event, QuestState state) {
        if (isRelevant(event.getQuest())) {
            update(event.getPlayer());
            return true;
        }
        return false;
    }

    private boolean isRelevant(Quest other) {
        return this.subQuests.contains(other) || this.failCondition == other || this.followupQuest == other;
    }

    public boolean onQuestSetReadyEvent(QuestSetReadyEvent event) {
        if (event.getSetReady()) {
            return false;
        }

        if (!this.subQuests.contains(event.getQuest()) && event.getQuest() != this.failCondition) {
            return false;
        }

        if (!isReady()) {
            return false;
        }

        try {
            setReady(false);
        } catch (IllegalStateException e) {
            event.setCancelled(true);
        }

        return true;
    }

    public boolean onQuestDeleteEvent(QuestDeleteEvent event) {
        if (this.deletionInProgress) {
            return false;
        }

        if (this.subQuests.contains(event.getQuest())) {
            throw new IllegalStateException("Quest " + event.getQuest() + " is still part of " + this + "!");
        }

        if (this.failCondition == event.getQuest()) {
            throw new IllegalStateException("Quest " + event.getQuest() + " is still failCondition of " + this + "!");
        }

        if (this.followupQuest == event.getQuest()) {
            throw new IllegalStateException("Quest " + event.getQuest() + " is still followupQuest of " + this + "!");
        }

        return false;
    }

    public boolean onQuestWouldBeDeletedEvent(QuestWouldBeDeletedEvent event) {
        boolean result = false;
        Quest quest = event.getQuest();

        if (quest == this && event.isCascading()) {
            this.deletionInProgress = true;
            try {
                for (Quest q : this.subQuests) {
                    QuestWouldBeDeletedEvent e = new QuestWouldBeDeletedEvent(q, true);
                    e.callEvent();
                    result |= e.isCancelled();
                }
                if (this.failCondition != null) {
                    QuestWouldBeDeletedEvent e = new QuestWouldBeDeletedEvent(this.failCondition, true);
                    e.callEvent();
                    result |= e.isCancelled();
                }
                if (this.followupQuest != null) {
                    QuestWouldBeDeletedEvent e = new QuestWouldBeDeletedEvent(this.followupQuest, true);
                    e.callEvent();
                    result |= e.isCancelled();
                }

                if (result) {
                    event.setCancelled(true);
                }
                return result;
            } finally {
                this.deletionInProgress = false;
            }
        }

        if (this.deletionInProgress) {
            return false;
        }

        if (this.subQuests.contains(event.getQuest())) {
            result = true;
            CubeQuest.getInstance().addStoredMessage("Quest " + quest + " is part of " + this + ".");
        }

        if (this.failCondition == event.getQuest()) {
            result = true;
            CubeQuest.getInstance().addStoredMessage("Quest " + quest + " is failCondition of " + this + ".");
        }

        if (this.followupQuest == event.getQuest()) {
            result = true;
            CubeQuest.getInstance().addStoredMessage("Quest " + quest + " is followupQuest of " + this + ".");
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

    public boolean isFollwupRequiredForSuccess() {
        return this.followupRequiredForSuccess;
    }

    public void setFollowupRequiredForSuccess(boolean val) {
        if (val && this.followupQuest == null && isReady()) {
            throw new IllegalStateException("Cannot put quest into illegal state while set ready.");
        }

        this.followupRequiredForSuccess = val;
        updateIfReal();
    }

    public boolean isFailAfterSemiSuccess() {
        return this.failAfterSemiSuccess;
    }

    public void setFailAfterSemiSuccess(boolean val) {
        this.failAfterSemiSuccess = val;
        updateIfReal();
    }

    public boolean isAchievementQuest() {
        return this.achievement;
    }

    public void setAchievementQuest(boolean val) {
        if (val && !Util.isLegalAchievementQuest(this)) {
            throw new IllegalStateException("This is not a legal AchievementQuest.");
        }

        this.achievement = val;
        updateIfReal();
    }

    /**
     * @return partQuests als unmodifiableCollection (live-Object, keine Kopie)
     */
    public Collection<Quest> getSubQuests() {
        return Collections.unmodifiableCollection(this.subQuests);
    }

    public boolean addSubQuest(Quest quest) {
        if (otherQuestWouldCreateCircle(quest)) {
            throw new CircleInQuestGraphException("Adding this quest would create circle in quest-graph.");
        }
        if (this.subQuests.add(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeSubQuest(Quest quest) {
        if (isReady() && this.subQuests.equals(Collections.singleton(quest))) {
            throw new IllegalStateException("Cannot put quest into illegal state while set ready.");
        }

        if (this.subQuests.remove(quest)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearSubQuests() {
        if (isReady()) {
            throw new IllegalStateException("Cannot put quest into illegal state while set ready.");
        }

        this.subQuests.clear();
        updateIfReal();
    }

    public Quest getFollowupQuest() {
        return this.followupQuest;
    }

    public void setFollowupQuest(Quest quest) {
        if (otherQuestWouldCreateCircle(quest)) {
            throw new CircleInQuestGraphException("Adding this quest would create circle in quest-graph.");
        }

        if (quest == null && this.followupRequiredForSuccess && isReady()) {
            throw new IllegalStateException("Cannot put quest into illegal state while set ready.");
        }

        this.followupQuest = quest;
        updateIfReal();
    }

    public Quest getFailCondition() {
        return this.failCondition;
    }

    public void setFailCondition(Quest quest) {
        if (otherQuestWouldCreateCircle(quest)) {
            throw new IllegalArgumentException("Adding this quest would create circle in quest-graph.");
        }
        this.failCondition = quest;
        updateIfReal();
    }

    public boolean update(Player player) {
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        if (!data.isGivenTo(getId())) {
            return false;
        }

        boolean result = false;
        if (this.structure != Structure.RANDOM_BE_DONE) {
            for (Quest quest : this.subQuests) {
                if (CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(quest.getId()) == Status.NOTGIVENTO) {
                    if (!quest.isReady()) {
                        CubeQuest.getInstance().getLogger().log(Level.WARNING,
                                "Quest " + quest + " is not ready, but subquest of " + this + " which is GIVENTO "
                                        + player.getUniqueId() + " (" + player.getName() + ").");
                    } else {
                        quest.giveToPlayer(player);
                        CubeQuest.getInstance().getLogger().log(Level.WARNING, "Had to regive " + quest + " to "
                                + player.getUniqueId() + " (" + player.getName() + ") as subquest of " + this + ".");
                        result = true;
                    }
                }

            }
        } else if (this.subQuests.stream().mapToInt(Quest::getId)
                .allMatch(id -> data.getPlayerStatus(id) == Status.NOTGIVENTO)) {
            Quest quest = RandomUtil.randomElement(this.subQuests.stream().toList());
            quest.giveToPlayer(player);
            CubeQuest.getInstance().getLogger().log(Level.WARNING, "Had to regive random quest (" + quest + ") to "
                    + player.getUniqueId() + " (" + player.getName() + ") as subquest of " + this + ".");
            result = true;
        }

        if (isFailed(data)) {
            onFail(player);
            return true;
        } else if (isSemiSuccessfull(data)) {
            return onSemiSuccess(player, data);
        }

        return result;
    }

    private boolean onSemiSuccess(Player player, PlayerData data) {
        for (Quest q : this.subQuests) {
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(q.getId())) {
                q.onFreeze(player);
            }
        }

        if (this.followupRequiredForSuccess) {
            if (data.getPlayerStatus(this.followupQuest.getId()) == Status.SUCCESS) {
                onSuccess(player);
                return true;
            } else {
                if (!this.failAfterSemiSuccess) {
                    freezeFailCondition(player);
                }
                if (!data.isGivenTo(this.followupQuest.getId())) {
                    giveFollowupToPlayer(player);
                    return true;
                }
            }
        } else {
            onSuccess(player);
            if (this.followupQuest != null) {
                giveFollowupToPlayer(player);
            }
            return true;
        }

        return false;
    }

    private void freezeFailCondition(Player player) {
        if (this.failCondition != null
                && CubeQuest.getInstance().getPlayerData(player).isGivenTo(this.failCondition.getId())) {
            this.failCondition.onFreeze(player);
        }
    }

    private void giveFollowupToPlayer(Player player) {
        if (!this.followupQuest.isReady()) {
            if (this.followupQuest.isLegal()) {
                this.followupQuest.setReady(true);
            }
        }
        this.followupQuest.giveToPlayer(player);
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
                || cQuest.subQuests.stream().anyMatch(q -> otherQuestWouldCreateCircle(q));
    }

    private boolean isSemiSuccessfull(PlayerData data) {
        switch (this.structure) {
            case ALL_TO_BE_DONE:
                for (Quest q : this.subQuests) {
                    if (data.getPlayerStatus(q.getId()) != Status.SUCCESS) {
                        return false;
                    }
                }
                return true;
            case ONE_TO_BE_DONE:
            case RANDOM_BE_DONE:
                for (Quest q : this.subQuests) {
                    if (data.getPlayerStatus(q.getId()) == Status.SUCCESS) {
                        return true;
                    }
                }
                return false;
        }
        throw new NullPointerException(); // structure kann nur noch null sein
    }

    private boolean isFailed(PlayerData data) {
        if (this.followupRequiredForSuccess && isSemiSuccessfull(data)) {
            if (!data.getPlayerStatus(this.followupQuest.getId()).succeedable
                    && data.getPlayerStatus(this.followupQuest.getId()) != Status.NOTGIVENTO) {
                return true;
            }

            if (this.failAfterSemiSuccess) {
                return isSemiFailed(data);
            }

            return false;
        } else {
            return isSemiFailed(data);
        }
    }

    private boolean isSemiFailed(PlayerData data) {
        if (this.failCondition != null && data.getPlayerStatus(this.failCondition.getId()) == Status.SUCCESS) {
            return true;
        }
        switch (this.structure) {
            case ALL_TO_BE_DONE:
                for (Quest q : this.subQuests) {
                    if (!data.getPlayerStatus(q.getId()).succeedable) {
                        return true;
                    }
                }
                return false;
            case ONE_TO_BE_DONE:
            case RANDOM_BE_DONE:
                for (Quest q : this.subQuests) {
                    if (data.getPlayerStatus(q.getId()).succeedable) {
                        return false;
                    }
                }
                return true;
        }
        throw new NullPointerException(); // structure kann nur noch null sein
    }

}
