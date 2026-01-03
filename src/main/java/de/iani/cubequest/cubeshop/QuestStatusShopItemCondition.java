package de.iani.cubequest.cubeshop;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.shopitemconditions.ShopItemCondition;
import de.iani.cubesideutils.ComponentUtilAdventure;
import java.util.Objects;
import org.bukkit.entity.Player;


public class QuestStatusShopItemCondition extends ShopItemCondition {

    private boolean negated;
    private Status status;
    private int questId;
    private String description;

    public QuestStatusShopItemCondition(boolean negated, Status status, int questId, String description) {
        this.negated = negated;
        this.status = Objects.requireNonNull(status);
        this.questId = questId;
        this.description = description;
    }

    public QuestStatusShopItemCondition(String serialized) throws DeserializationException {
        String[] parts = serialized.split("\\;", 4);
        if (parts.length != 4) {
            throw new DeserializationException("illegal syntax of serialized");
        }

        if (parts[0].equals("true")) {
            this.negated = true;
        } else if (parts[0].equals("false")) {
            this.negated = false;
        } else {
            throw new DeserializationException("unknown boolean " + parts[0]);
        }

        this.status = Status.match(parts[1]);
        if (this.status == null) {
            throw new DeserializationException("unknown status " + parts[1]);
        }

        try {
            this.questId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new DeserializationException(e);
        }

        this.description = parts[3].isEmpty() ? null : parts[3];
    }

    public boolean isNegated() {
        return this.negated;
    }

    public Status getStatus() {
        return this.status;
    }

    public int getQuestId() {
        return this.questId;
    }


    @Override
    public QuestStatusShopItemConditionType getType() {
        return QuestStatusShopItemConditionType.getInstance();
    }

    @Override
    public boolean fullfills(Player player) {
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        Status status = data.getPlayerStatus(this.questId);
        return this.negated ? status != this.status : status == this.status;
    }

    @Override
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }

        Quest quest = QuestManager.getInstance().getQuest(this.questId);
        String questName;
        if (quest == null || quest.getDisplayName() == null) {
            questName = String.valueOf(this.questId);
        } else {
            questName = ComponentUtilAdventure.getLegacyComponentSerializer().serialize(quest.getDisplayName());
        }

        return "Status von Quest " + questName + ": " + (this.negated ? "Nicht " : "") + this.status;
    }

    @Override
    public String serialize() {
        return this.negated + ";" + this.status + ";" + this.questId + ";"
                + (this.description == null ? "" : this.description);
    }

}
