package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.quests.ClickInteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;

public class ClickInteractorQuestSpecification extends DifficultyQuestSpecification implements InteractorProtecting {

    private ClickInteractorQuest dataStorageQuest;

    public ClickInteractorQuestSpecification() {
        super();

        this.dataStorageQuest = new ClickInteractorQuest(-1);
    }

    public ClickInteractorQuestSpecification(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            this.dataStorageQuest = (ClickInteractorQuest) serialized.get("dataStorageQuest");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public ClickInteractorQuest createGeneratedQuest(Component questName, Reward successReward) {
        int questId;
        try {
            questId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create generated GotoQuest!", e);
            return null;
        }

        ClickInteractorQuest result = new ClickInteractorQuest(questId, questName, null, getInteractor());
        result.setDelayDatabaseUpdate(true);
        result.setDisplayMessage(getGiveMessage());
        result.addGiveAction(getGiveMessageAction());
        result.addSuccessAction(new RewardAction(successReward));
        if (!(result.getInteractorName().equals(getInteractorName()))) {
            result.setInteractorName(getInteractorName());
        }
        QuestManager.getInstance().addQuest(result);
        result.setDelayDatabaseUpdate(false);

        return result;
    }

    @Override
    public Interactor getInteractor() {
        return this.dataStorageQuest.getInteractor();
    }

    public void setInteractor(Interactor interactor) {
        CubeQuest.getInstance().removeProtecting(this);
        this.dataStorageQuest.setInteractor(interactor);
        CubeQuest.getInstance().addProtecting(this);
        interactor.getName();
        interactor.getLocation();
        update();
    }

    public Component getInteractorName() {
        return this.dataStorageQuest.getInteractorName();
    }

    public void setInteractorName(Component name) {
        this.dataStorageQuest.setInteractorName(name == null || Component.empty().equals(name) ? null : name);
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
    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event) {
        return this.dataStorageQuest.onInteractorDamagedEvent(event);
    }

    @Override
    public void onCacheChanged() {
        // nothing
    }

    @Override
    public Component getSpecificationInfo() {
        Component c = Component.empty().append(super.getSpecificationInfo()).append(Component.text(" Interactor: "))
                .append(ChatAndTextUtil.getInteractorInfo(this.dataStorageQuest.getInteractor()))
                .append(Component.text(" Name: ")).append(getInteractorName().colorIfAbsent(NamedTextColor.GREEN))
                .append(Component.text(" Vergabenachricht: "));

        Component give = getGiveMessage();
        c = c.append(
                give == null ? Component.text("NULL", NamedTextColor.GOLD) : give.colorIfAbsent(NamedTextColor.GREEN));

        return c.color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Component getProtectingInfo() {
        return getSpecificationInfo();
    }

    @Override
    public int compareTo(QuestSpecification other) {
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        ClickInteractorQuestSpecification ciqs = (ClickInteractorQuestSpecification) other;

        int i1 = getInteractor() == null ? 0 : 1;
        int i2 = ciqs.getInteractor() == null ? 0 : 1;

        if (i1 != i2) {
            return i1 - i2;
        }

        return i1 == 0 ? 0 : getInteractor().compareTo(ciqs.getInteractor());
    }

    @Override
    public int hashCode() {
        long diffBits = Double.doubleToLongBits(getDifficulty());
        int result = (int) (diffBits ^ (diffBits >>> 32));
        result = getInteractor().hashCode() + 31 * result;
        return result;
    }

    @Override
    public boolean isLegal() {
        return getInteractor() != null && getInteractor().isLegal() && getGiveMessage() != null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("dataStorageQuest", this.dataStorageQuest);
        return result;
    }

}
