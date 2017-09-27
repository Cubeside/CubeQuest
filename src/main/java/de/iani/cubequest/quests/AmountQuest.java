package de.iani.cubequest.quests;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.AmountQuestState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class AmountQuest extends Quest {

    private int amount;

    public AmountQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            int amount) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward);

        this.amount = amount;
    }

    public AmountQuest(int id) {
        this(id, null, null, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        amount = yc.getInt("amount");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("amount", amount);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return amount > 0;
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return this.getId() < 0? null : new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), this.getId());
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Zu erreichende Anzahl: " + (amount > 0? ChatColor.GREEN : ChatColor.RED) + amount).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must not be negative");
        }
        this.amount = val;
        updateIfReal();
    }

}
