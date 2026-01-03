package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.questStates.AmountQuestState;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class AmountQuest extends ProgressableQuest {

    private int amount;

    public AmountQuest(int id, String name, Component displayMessage, int amount) {
        super(id, name, displayMessage);

        this.amount = amount;
    }

    public AmountQuest(int id) {
        this(id, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.amount = yc.getInt("amount");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("amount", this.amount);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return this.amount > 0;
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return getId() < 0 ? null : new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), getId());
    }


    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                Component.text("Zu erreichende Anzahl: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(String.valueOf(this.amount),
                                this.amount > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)),
                "/" + SetQuestAmountCommand.FULL_COMMAND));
        result.add(Component.empty());

        return result;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int val) {
        if (val < 1) {
            throw new IllegalArgumentException("val must not be negative");
        }
        this.amount = val;
        updateIfReal();
    }

}
