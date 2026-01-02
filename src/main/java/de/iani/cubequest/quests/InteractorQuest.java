package de.iani.cubequest.quests;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.bubbles.QuestTargetBubbleTarget;
import de.iani.cubequest.commands.EditQuestCommand;
import de.iani.cubequest.commands.SetDoBubbleCommand;
import de.iani.cubequest.commands.SetInteractorQuestConfirmationMessageCommand;
import de.iani.cubequest.commands.SetOrRemoveQuestInteractorCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.commands.SetRequireConfirmationCommand;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public abstract class InteractorQuest extends ServerDependendQuest implements InteractorProtecting {

    private static final Component[] DEFAULT_CONFIRMATION_MESSAGE =
            new Component[] {Component.text("Quest \""), Component.text("\" abschließen.")};

    private Interactor interactor;
    private Component overwrittenInteractorName;
    private Component confirmationMessage;
    private boolean requireConfirmation;
    private boolean doBubble;

    private boolean updatedSinceEnable = false;

    public InteractorQuest(int id, String name, Component displayMessage, int serverId, Interactor interactor) {
        super(id, name, displayMessage, serverId);

        this.interactor = interactor;
        this.requireConfirmation = true;
        this.doBubble = true;
    }

    public InteractorQuest(int id, String name, Component displayMessage, Interactor interactor) {
        super(id, name, displayMessage);

        this.interactor = interactor;
        this.requireConfirmation = true;
        this.doBubble = true;
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        possiblyRemoveProtecting();
        if (isLegal() && isForThisServer() && isReady()) {
            CubeQuest.getInstance().getBubbleMaker().unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
        }

        super.deserialize(yc);

        this.interactor = yc.contains("interactor") ? (Interactor) yc.get("interactor") : null;
        this.overwrittenInteractorName = getComponentOrConvert(yc, "overwrittenInteractorName");
        this.confirmationMessage = getComponentOrConvert(yc, "confirmationMessage");
        this.requireConfirmation = yc.getBoolean("requireConfirmation", true);
        this.doBubble = yc.getBoolean("doBubble", true);

        possiblyAddProtecting();

        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            if (isForThisServer() && this.doBubble && isReady()) {
                CubeQuest.getInstance().getBubbleMaker().registerBubbleTarget(new QuestTargetBubbleTarget(this));
            }
        }, 1L);

    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("interactor", this.interactor);
        yc.set("overwrittenInteractorName", this.overwrittenInteractorName);
        yc.set("confirmationMessage", this.confirmationMessage);
        yc.set("requireConfirmation", this.requireConfirmation);
        yc.set("doBubble", this.doBubble);

        return super.serializeToString(yc);
    }

    @Override
    public void setReady(boolean val) {
        if (isReady() == val) {
            return;
        }

        if (!isLegal() && val) {
            super.setReady(val);
            return;
        }

        boolean before = isDelayDatabaseUpdate();
        setDelayDatabaseUpdate(true);
        prepareSetReady(val);
        super.setReady(val);
        hasBeenSetReady(val);
        setDelayDatabaseUpdate(before);
    }

    private void prepareSetReady(boolean val) {
        if (isForThisServer()) {
            if (!val) {
                this.interactor.resetAccessible();
                CubeQuest.getInstance().getBubbleMaker().unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
            }
        }
    }

    public void hasBeenSetReady(boolean val) {
        if (!val) {
            return;
        }
        if (!isForThisServer()) {
            return;
        }
        this.interactor.makeAccessible();
        if (this.doBubble) {
            CubeQuest.getInstance().getBubbleMaker().registerBubbleTarget(new QuestTargetBubbleTarget(this));
        }
    }

    @Override
    protected void changeServerToThis() {
        if (this.interactor != null && !this.interactor.isForThisServer()) {
            this.interactor = null;
        }
        super.changeServerToThis();
    }

    @Override
    public boolean onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }
        if (!event.getInteractor().equals(this.interactor)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return this.interactor != null && (!isForThisServer() || this.interactor.isLegal());
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                text("Ziel: ", NamedTextColor.DARK_AQUA).append(ChatAndTextUtil.getInteractorInfo(this.interactor)),
                SetOrRemoveQuestInteractorCommand.FULL_SET_COMMAND));

        result.add(suggest(
                text("Name: ", NamedTextColor.DARK_AQUA)
                        .append(getInteractorName() == null ? text("NULL", NamedTextColor.GOLD) : getInteractorName())
                        .append(text(" "))
                        .append(this.overwrittenInteractorName == null ? text("(automatisch)", NamedTextColor.GOLD)
                                : text("(gesetzt)", NamedTextColor.GREEN)),
                SetOverwrittenNameForSthCommand.SpecificSth.INTERACTOR.fullSetCommand));

        result.add(text("Blubbert: ", NamedTextColor.DARK_AQUA)
                .append(text(String.valueOf(this.doBubble), this.doBubble ? NamedTextColor.GREEN : NamedTextColor.GOLD))
                .clickEvent(ClickEvent.suggestCommand("/" + SetDoBubbleCommand.FULL_COMMAND))
                .hoverEvent(SUGGEST_COMMAND_HOVER_EVENT));

        result.add(empty());

        result.add(suggest(
                text("Erfordert Bestätigung: ", NamedTextColor.DARK_AQUA)
                        .append(text(String.valueOf(this.requireConfirmation),
                                this.requireConfirmation ? NamedTextColor.GREEN : NamedTextColor.GOLD)),
                "/" + SetRequireConfirmationCommand.FULL_COMMAND));

        result.add(suggest(
                text("Bestätigungstext: ", NamedTextColor.DARK_AQUA).append(getConfirmationMessage())
                        .append(this.confirmationMessage == null ? text("(automatisch)", NamedTextColor.GOLD)
                                : text("(gesetzt)", NamedTextColor.GREEN)),
                "/" + SetInteractorQuestConfirmationMessageCommand.FULL_COMMAND));

        result.add(empty());

        return result;
    }

    @Override
    public Component getProtectingInfo() {
        return text(toString()).clickEvent(ClickEvent.runCommand("/" + EditQuestCommand.FULL_COMMAND + " " + getId()))
                .hoverEvent(HoverEvent.showText(text("Show Quest-Info.")));
    }

    @Override
    public Interactor getInteractor() {
        return this.interactor;
    }

    public void setInteractor(Interactor interactor) {
        this.updatedSinceEnable = false;

        possiblyRemoveProtecting();
        if (isForThisServer() && interactor == null) {
            if (isReady()) {
                CubeQuest.getInstance().getBubbleMaker().unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
                setReady(false);
            }
        }

        Location oldLocation = this.interactor != null && isForThisServer() ? this.interactor.getLocation() : null;

        if (interactor != null) {
            if (!interactor.isForThisServer()) {
                throw new IllegalArgumentException("Interactor must be from this server.");
            }
            changeServerToThis();
        }

        this.interactor = interactor;
        updateIfReal();

        possiblyAddProtecting();
        if (isForThisServer() && isReady() && this.doBubble) {
            CubeQuest.getInstance().getBubbleMaker().updateBubbleTarget(new QuestTargetBubbleTarget(this), oldLocation);
        }
    }

    public Component getInteractorName() {
        return this.overwrittenInteractorName != null ? this.overwrittenInteractorName
                : this.interactor != null ? Component.text(this.interactor.getName()) : null;
    }

    public void setInteractorName(Component name) {
        this.overwrittenInteractorName = name;
        updateIfReal();
    }

    public Component getConfirmationMessage() {
        return this.confirmationMessage == null
                ? Component.textOfChildren(DEFAULT_CONFIRMATION_MESSAGE[0], getDisplayName(),
                        DEFAULT_CONFIRMATION_MESSAGE[1]).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                : this.confirmationMessage;
    }

    public void setConfirmationMessage(Component msg) {
        this.confirmationMessage = msg;
        updateIfReal();
    }

    public boolean isRequireConfirmation() {
        return this.requireConfirmation;
    }

    public void setRequireConfirmation(boolean val) {
        this.requireConfirmation = val;
        updateIfReal();
    }

    public boolean isDoBubble() {
        return this.doBubble;
    }

    public void setDoBubble(boolean val) {
        if (this.doBubble == val) {
            return;
        }

        this.doBubble = val;

        if (isForThisServer() && isReady()) {
            if (!val) {
                CubeQuest.getInstance().getBubbleMaker().unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
            } else {
                CubeQuest.getInstance().getBubbleMaker().registerBubbleTarget(new QuestTargetBubbleTarget(this));
            }
        }

        updateIfReal();
    }

    public boolean playerConfirmedInteraction(Player player, QuestState state) {
        if (!isReady()) {
            ChatAndTextUtil.sendErrorMessage(player, "Diese Quest ist derzeit deaktiviert.");
            return false;
        }
        if (!this.fulfillsProgressConditions(player, state.getPlayerData())) {
            List<Component> missingConds = new ArrayList<>();
            missingConds.add(Component.text("Du erfüllst nicht alle Voraussetzungen, um diese Quest abzuschließen:")
                    .color(NamedTextColor.GOLD));
            for (QuestCondition cond : getQuestProgressConditions()) {
                if (cond.isVisible() && !cond.fulfills(player, state.getPlayerData())) {
                    missingConds.add(cond.getConditionInfo());
                }
            }
            if (missingConds.size() == 1) {
                ChatAndTextUtil.sendWarningMessage(player, "Du kannst diese Quest derzeit nicht abschließen.");
            } else {
                ChatAndTextUtil.sendComponents(player, missingConds);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event) {
        if (event.getInteractor().equals(this.interactor)) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @Override
    public void onCacheChanged() {
        if (!this.updatedSinceEnable) {
            updateIfReal();
            this.updatedSinceEnable = true;
        } else {
            CubeQuest.getInstance().addUpdateOnDisable(this);
        }
    }

    private void possiblyAddProtecting() {
        if (isReal() && isLegal() && isForThisServer() && QuestManager.getInstance().getQuest(getId()) == this) {
            CubeQuest.getInstance().addProtecting(this);
        }
    }

    private void possiblyRemoveProtecting() {
        if (isReal() && isLegal() && isForThisServer() && QuestManager.getInstance().getQuest(getId()) == this) {
            CubeQuest.getInstance().removeProtecting(this);
        }
    }

}
