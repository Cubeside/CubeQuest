package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.Reward;
import de.iani.cubequest.bubbles.QuestTargetBubbleTarget;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class InteractorQuest extends ServerDependendQuest {
    
    private static final String[] DEFAULT_CONFIRMATION_MESSAGE = new String[] {
            ChatColor.translateAlternateColorCodes('&', "&6&LQuest \""), "\" abschließen."};
    
    private Interactor interactor;
    private String overwrittenInteractorName;
    private String confirmationMessage;
    private boolean doBubble;
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            int serverId, Interactor interactor) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward, serverId);
        
        this.interactor = interactor;
        this.doBubble = true;
    }
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            Interactor interactor) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        
        this.interactor = interactor;
        this.doBubble = true;
    }
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, int serverId, Interactor interactor) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                serverId, interactor);
    }
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, Interactor interactor) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null,
                interactor);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        if (this.interactor != null && isReady()) {
            CubeQuest.getInstance().getBubbleMaker()
                    .unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
        }
        
        this.interactor = yc.contains("interactor") ? (Interactor) yc.get("interactor") : null;
        this.overwrittenInteractorName =
                yc.contains("overwrittenInteractorName") ? yc.getString("overwrittenInteractorName")
                        : null;
        this.confirmationMessage =
                yc.contains("confirmationMessage") ? yc.getString("confirmationMessage") : null;
        this.doBubble = yc.getBoolean("doBubble", true);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            if (isForThisServer() && this.doBubble && isReady()) {
                CubeQuest.getInstance().getBubbleMaker()
                        .registerBubbleTarget(new QuestTargetBubbleTarget(this));
            }
        }, 1L);
        
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("interactor", this.interactor);
        yc.set("overwrittenInteractorName", this.overwrittenInteractorName);
        yc.set("confirmationMessage", this.confirmationMessage);
        yc.set("duBubble", this.doBubble);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public void setReady(boolean val) {
        if (isReady() == val) {
            return;
        }
        
        setDelayDatabseUpdate(true);
        prepareSetReady(val);
        super.setReady(val);
        hasBeenSetReady(val);
        setDelayDatabseUpdate(false);
    }
    
    private void prepareSetReady(boolean val) {
        if (isForThisServer()) {
            if (!val) {
                this.interactor.resetAccessible();
                CubeQuest.getInstance().getBubbleMaker()
                        .unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
            }
        }
    }
    
    public void hasBeenSetReady(boolean val) {
        if (isForThisServer()) {
            if (val) {
                this.interactor.makeAccessible();
                if (this.doBubble) {
                    CubeQuest.getInstance().getBubbleMaker()
                            .registerBubbleTarget(new QuestTargetBubbleTarget(this));
                }
            }
        } else {
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            try {
                msgout.writeInt(GlobalChatMsgType.NPC_QUEST_SETREADY.ordinal());
                msgout.write(getId());
                msgout.writeBoolean(val);
            } catch (IOException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "IOException trying to send PluginMessage!", e);
                return;
            }
            
            byte[] msgarry = msgbytes.toByteArray();
            CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
        }
    }
    
    @Override
    protected void changeServerToThis() {
        if (this.interactor != null) {
            this.interactor.changeServerToThis();
        }
        super.changeServerToThis();
    }
    
    @Override
    public boolean onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent event,
            QuestState state) {
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
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Target: "
                + ChatAndTextUtil.getInteractorInfoString(this.interactor)).create());
        result.add(
                new ComponentBuilder(
                        ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + getInteractorName() + " "
                                + (this.overwrittenInteractorName == null
                                        ? ChatColor.GOLD + "(automatisch)"
                                        : ChatColor.GREEN + "(gesetzt)")).create());
        result.add(new ComponentBuilder("").create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Bestätigungstext: " + ChatColor.RESET
                + getConfirmationMessage()).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public Interactor getInteractor() {
        return this.interactor;
    }
    
    public void setInteractor(Interactor interactor) {
        if (isReady() && interactor == null) {
            CubeQuest.getInstance().getBubbleMaker()
                    .unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
        }
        
        Location oldLocation =
                this.interactor != null && isForThisServer() ? interactor.getLocation() : null;
        
        if (interactor != null) {
            if (!interactor.isForThisServer()) {
                throw new IllegalArgumentException("Interactor must be from this server.");
            }
            changeServerToThis();
        }
        
        this.interactor = interactor;
        updateIfReal();
        
        
        if (isReady() && this.doBubble) {
            CubeQuest.getInstance().getBubbleMaker()
                    .updateBubbleTarget(new QuestTargetBubbleTarget(this), oldLocation);
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
                ? DEFAULT_CONFIRMATION_MESSAGE[0] + getName() + DEFAULT_CONFIRMATION_MESSAGE[1]
                : this.confirmationMessage;
    }
    
    public void setConfirmationMessage(String msg) {
        this.confirmationMessage = msg;
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
        
        if (!val) {
            CubeQuest.getInstance().getBubbleMaker()
                    .unregisterBubbleTarget(new QuestTargetBubbleTarget(this));
        } else if (isReady()) {
            CubeQuest.getInstance().getBubbleMaker()
                    .registerBubbleTarget(new QuestTargetBubbleTarget(this));
        }
    }
    
    public abstract boolean playerConfirmedInteraction(QuestState state);
    
}
