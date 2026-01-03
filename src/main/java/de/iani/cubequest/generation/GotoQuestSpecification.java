package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;

public class GotoQuestSpecification extends DifficultyQuestSpecification {

    private GotoQuest dataStorageQuest;

    public GotoQuestSpecification() {
        super();

        this.dataStorageQuest = new GotoQuest(-1);
    }

    public GotoQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            this.dataStorageQuest = (GotoQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public GotoQuest createGeneratedQuest(Component questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated GotoQuest!", e);
            return null;
        }

        GotoQuest result = new GotoQuest(questId, questName, null, getLocation(), getTolerance());
        result.setDisplayMessage(getGiveMessage());
        result.addGiveAction(getGiveMessageAction());
        result.addSuccessAction(new RewardAction(successReward));
        if (!(result.getLocationName().equals(getLocationName()))) {
            result.setLocationName(getLocationName());
        }
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);

        return result;
    }

    public Location getLocation() {
        return this.dataStorageQuest.getTargetLocation();
    }

    public void setLocation(Location location) {
        this.dataStorageQuest.setLocation(location);
        update();
    }

    public double getTolerance() {
        return this.dataStorageQuest.getTolarance();
    }

    public void setTolerance(double tolerance) {
        this.dataStorageQuest.setTolarance(tolerance);
        update();
    }

    public Component getLocationName() {
        return this.dataStorageQuest.getLocationName();
    }

    public void setLocationName(Component name) {
        this.dataStorageQuest.setLocationName(name == null || Component.empty().equals(name) ? null : name);
    }

    public ChatMessageAction getGiveMessageAction() {
        for (int i = 0; i < this.dataStorageQuest.getGiveActions().size(); i++) {
            if (this.dataStorageQuest.getGiveActions().get(i) instanceof ChatMessageAction) {
                return ((ChatMessageAction) this.dataStorageQuest.getGiveActions().get(i));
            }
        }
        return null;
    }

    public Component getGiveMessage() {
        ChatMessageAction action = getGiveMessageAction();
        return action == null ? null : action.getMessage();
    }

    public void setGiveMessage(Component giveMessage) {
        giveMessage = giveMessage.colorIfAbsent(NamedTextColor.GOLD);
        for (int i = 0; i < this.dataStorageQuest.getGiveActions().size(); i++) {
            if (this.dataStorageQuest.getGiveActions().get(i) instanceof ChatMessageAction) {
                this.dataStorageQuest.removeGiveAction(i);
                break;
            }
        }
        this.dataStorageQuest.addGiveAction(new ChatMessageAction(giveMessage));
        update();
    }

    @Override
    public Component getSpecificationInfo() {
        Component c = Component.empty().append(super.getSpecificationInfo()).append(Component.text(" "))
                .append(ChatAndTextUtil.getLocationInfo(this.dataStorageQuest.getTargetLocation()))
                .append(Component.text(" "))
                .append(ChatAndTextUtil.getToleranceInfo(this.dataStorageQuest.getTolarance()))
                .append(Component.text(" Name: ")).append(getLocationName().colorIfAbsent(NamedTextColor.GREEN))
                .append(Component.text(" Vergabenachricht: "));

        Component give = getGiveMessage();
        c = c.append(
                give == null ? Component.text("NULL", NamedTextColor.GOLD) : give.colorIfAbsent(NamedTextColor.GREEN));

        return c.color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        GotoQuestSpecification ogqs = (GotoQuestSpecification) other;

        result = getLocation().getWorld().getName().compareTo(ogqs.getLocation().getWorld().getName());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getX(), ogqs.getLocation().getX());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getY(), ogqs.getLocation().getY());
        if (result != 0) {
            return result;
        }

        result = Double.compare(getLocation().getZ(), ogqs.getLocation().getZ());
        if (result != 0) {
            return result;
        }

        return 0;
    }

    @Override
    public boolean isLegal() {
        return getLocation() != null && getTolerance() >= 0 && getGiveMessage() != null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", this.dataStorageQuest);
        return result;
    }

}
