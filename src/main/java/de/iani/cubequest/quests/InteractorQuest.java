package de.iani.cubequest.quests;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.BugeeMsgType;
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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public abstract class InteractorQuest extends ServerDependendQuest {
    
    private static final String[] DEFAULT_CONFIRMATION_MESSAGE = new String[] {
            ChatColor.translateAlternateColorCodes('&', "&6&LQuest \""), "\" abschließen."};
    
    private Interactor interactor;
    
    private String confirmationMessage;
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            int serverId, Interactor interactor) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward, serverId);
        
        this.interactor = interactor;
    }
    
    public InteractorQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            Interactor interactor) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        
        this.interactor = interactor;
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
        
        this.interactor = yc.contains("interactor") ? (Interactor) yc.get("interactor") : null;
        this.confirmationMessage =
                yc.contains("confirmationMessage") ? (String) yc.get("confirmationMessage") : null;
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            if (isReady()) {
                CubeQuest.getInstance().getBubbleMaker()
                        .registerQuestTargetBubbleMaker(new QuestTargetBubbleTarget(this));
            }
        }, 1L);
        
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("interactor", this.interactor);
        yc.set("confirmationMessage", this.confirmationMessage);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public void setReady(boolean val) {
        if (isReady() == val) {
            return;
        }
        
        super.setReady(val);
        hasBeenSetReady(val);
    }
    
    public void hasBeenSetReady(boolean val) {
        if (isForThisServer()) {
            if (val) {
                this.interactor.makeAccessible();
                updateIfReal();
                CubeQuest.getInstance().getBubbleMaker()
                        .registerQuestTargetBubbleMaker(new QuestTargetBubbleTarget(this));
            } else {
                this.interactor.resetAccessible();
                CubeQuest.getInstance().getBubbleMaker()
                        .unregisterQuestTargetBubbleMaker(new QuestTargetBubbleTarget(this));
            }
        } else {
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            try {
                msgout.writeInt(BugeeMsgType.NPC_QUEST_SETREADY.ordinal());
                msgout.write(getId());
                msgout.writeBoolean(val);
            } catch (IOException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "IOException trying to send PluginMessage!", e);
                return;
            }
            byte[] msgarry = msgbytes.toByteArray();
            
            CubeQuest.getInstance().addWaitingForPlayer(() -> {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Forward");
                out.writeUTF(getServerName());
                out.writeUTF("CubeQuest");
                out.writeShort(msgarry.length);
                out.write(msgarry);
                
                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                player.sendPluginMessage(CubeQuest.getInstance(), "BungeeCord", out.toByteArray());
            });
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
        if (interactor != null) {
            interactor.changeServerToThis();
            changeServerToThis();
        }
        this.interactor = interactor;
        updateIfReal();
    }
    
    public void setConfirmationMessage(String msg) {
        this.confirmationMessage = msg;
        updateIfReal();
    }
    
    public String getConfirmationMessage() {
        return this.confirmationMessage == null
                ? DEFAULT_CONFIRMATION_MESSAGE[0] + getName() + DEFAULT_CONFIRMATION_MESSAGE[1]
                : this.confirmationMessage;
    }
    
    public abstract boolean playerConfirmedInteraction(QuestState state);
    
}
