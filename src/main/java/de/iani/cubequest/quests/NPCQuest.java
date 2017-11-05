package de.iani.cubequest.quests;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.BugeeMsgType;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.wrapper.NPCRightClickEventWrapper;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class NPCQuest extends ServerDependendQuest {

    private Integer npcId;
    private boolean wasSpawned;

    public NPCQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId,
            Integer npcId) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward, failReward, serverId);

        this.npcId = npcId;
        this.wasSpawned = true;
    }

    public NPCQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Integer npcId) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward, failReward);

        this.npcId = npcId;
        this.wasSpawned = true;
    }

    public NPCQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward, int serverId,
            Integer npcId) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null, serverId, npcId);
    }

    public NPCQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward,
            Integer npcId) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null, npcId);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        npcId = yc.contains("npcId")? yc.getInt("npcId") : null;
        wasSpawned = yc.contains("wasSpawned")? yc.getBoolean("wasSpawned") : true;
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("npcId", npcId);
        yc.set("wasSpawned", wasSpawned);

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
                wasSpawned = internalIsNPCSpawned();
                this.updateIfReal();
                if (!wasSpawned) {
                    getNPC().spawn(getNPC().getStoredLocation());
                }
            } else {
                if (!wasSpawned) {
                    getNPC().despawn();
                }
            }
        } else {
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            try {
                msgout.writeInt(BugeeMsgType.NPC_QUEST_SETREADY.ordinal());
                msgout.write(getId());
                msgout.writeBoolean(val);
            } catch (IOException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
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

    private boolean internalIsNPCSpawned() {
        return getNPC().isSpawned();
    }

    @Override
    protected void changeServerToThis() {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            throw new IllegalStateException("This server doesn't have the CitizensPlugin!");
        }
        super.changeServerToThis();
    }

    @Override
    public boolean onNPCRightClickEvent(NPCRightClickEventWrapper event, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }
        if (event.getOriginal().getNPC().getId() != npcId.intValue()) {
            return false;
        }
        if (!CubeQuest.getInstance().getPlayerData(event.getOriginal().getClicker()).isGivenTo(this.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return npcId != null && (!isForThisServer() || CubeQuest.getInstance().hasCitizensPlugin() && internalGetNPC() != null);
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "NPC: " + ChatAndTextUtil.getNPCInfoString(isForThisServer(), npcId)).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public NPC getNPC() {
        if (npcId == null || !isForThisServer()) {
            return null;
        }
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            throw new IllegalStateException("Citizens-Plugin isn't installed!");
        }
        return internalGetNPC();
    }

    private NPC internalGetNPC() {
        return CubeQuest.getInstance().getNPCReg().getById(npcId);
    }

    public void setNPC(Integer npcId) {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            throw new IllegalStateException("Citizens-Plugin isn't installed!");
        }
        internalSetNPC(npcId);
    }

    private void internalSetNPC(Integer npcId) {
        NPC npc = CubeQuest.getInstance().getNPCReg().getById(npcId);
        changeServerToThis();
        this.npcId = npc == null? null : npc.getId();
        updateIfReal();
        // Falls eigenes NPCRegistry: UMSCHREIBEN!
    }

}
