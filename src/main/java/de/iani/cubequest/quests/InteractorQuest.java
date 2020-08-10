package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.bubbles.QuestTargetBubbleTarget;
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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public abstract class InteractorQuest extends ServerDependendQuest implements InteractorProtecting {
    
    private static final String[] DEFAULT_CONFIRMATION_MESSAGE =
            new String[] {ChatColor.translateAlternateColorCodes('&', "&6&LQuest \""),
                    ChatColor.translateAlternateColorCodes('&', "&6\" abschließen.")};
    
    private Interactor interactor;
    private String overwrittenInteractorName;
    private String confirmationMessage;
    private boolean requireConfirmation;
    private boolean doBubble;
    
    private boolean updatedSinceEnable = false;
    
    public InteractorQuest(int id, String name, String displayMessage, int serverId, Interactor interactor) {
        super(id, name, displayMessage, serverId);
        
        this.interactor = interactor;
        this.requireConfirmation = true;
        this.doBubble = true;
    }
    
    public InteractorQuest(int id, String name, String displayMessage, Interactor interactor) {
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
        this.overwrittenInteractorName =
                yc.contains("overwrittenInteractorName") ? yc.getString("overwrittenInteractorName") : null;
        this.confirmationMessage = yc.contains("confirmationMessage") ? yc.getString("confirmationMessage") : null;
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
        
        if (!isLegal()) {
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
        if (isForThisServer()) {
            if (val) {
                this.interactor.makeAccessible();
                if (this.doBubble) {
                    CubeQuest.getInstance().getBubbleMaker().registerBubbleTarget(new QuestTargetBubbleTarget(this));
                }
            }
        } else {
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            try {
                msgout.writeInt(GlobalChatMsgType.NPC_QUEST_SETREADY.ordinal());
                msgout.writeInt(getId());
                msgout.writeBoolean(val);
            } catch (IOException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
                return;
            }
            
            byte[] msgarry = msgbytes.toByteArray();
            CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
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
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Target: ")
                .append(TextComponent.fromLegacyText(ChatAndTextUtil.getInteractorInfoString(this.interactor)))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetOrRemoveQuestInteractorCommand.FULL_SET_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN)
                .append(TextComponent.fromLegacyText(getInteractorName()))
                .append(" " + (this.overwrittenInteractorName == null ? ChatColor.GOLD + "(automatisch)"
                        : ChatColor.GREEN + "(gesetzt)"))
                .event(new ClickEvent(Action.SUGGEST_COMMAND,
                        "/" + SetOverwrittenNameForSthCommand.SpecificSth.INTERACTOR.fullSetCommand))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Blubbert: " + (this.doBubble ? ChatColor.GREEN : ChatColor.GOLD) + this.doBubble)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetDoBubbleCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Erfordert Bestätigung: "
                + (this.requireConfirmation ? ChatColor.GREEN : ChatColor.GOLD) + this.requireConfirmation)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetRequireConfirmationCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bestätigungstext: ")
                .event(new ClickEvent(Action.SUGGEST_COMMAND,
                        "/" + SetInteractorQuestConfirmationMessageCommand.FULL_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).append("").retain(FormatRetention.EVENTS)
                .append(TextComponent.fromLegacyText(getConfirmationMessage())).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
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
    
    public String getInteractorName() {
        return this.overwrittenInteractorName != null ? this.overwrittenInteractorName
                : this.interactor != null ? this.interactor.getName() : null;
    }
    
    public void setInteractorName(String name) {
        this.overwrittenInteractorName = name;
        updateIfReal();
    }
    
    public String getConfirmationMessage() {
        return this.confirmationMessage == null
                ? DEFAULT_CONFIRMATION_MESSAGE[0] + getDisplayName() + DEFAULT_CONFIRMATION_MESSAGE[1]
                : this.confirmationMessage;
    }
    
    public void setConfirmationMessage(String msg) {
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
        if (!this.fulfillsProgressConditions(player, state.getPlayerData())) {
            List<BaseComponent[]> missingConds = new ArrayList<>();
            missingConds
                    .add(new ComponentBuilder("Du erfüllst nicht alle Voraussetzungen, um diese Quest abzuschließen:")
                            .color(ChatColor.GOLD).create());
            for (QuestCondition cond : getQuestProgressConditions()) {
                if (cond.isVisible() && !cond.fulfills(player, state.getPlayerData())) {
                    missingConds.add(cond.getConditionInfo());
                }
            }
            if (missingConds.size() == 1) {
                ChatAndTextUtil.sendWarningMessage(player, "Du kannst diese Quest derzeit nicht abschließen.");
            } else {
                ChatAndTextUtil.sendBaseComponent(player, missingConds);
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
