package de.iani.cubequest.quests;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.wrapper.NPCClickEventWrapper;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class NPCQuest extends ServerDependendQuest {

    private Integer npcId;

    public NPCQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId,
            Integer npcId) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward, serverId);

        this.npcId = npcId;
    }

    public NPCQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward,
            Integer npcId) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.npcId = npcId;
    }

    public NPCQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, int serverId,
            Integer npcId) {
        this(id, name, giveMessage, successMessage, null, successReward, null, serverId, npcId);
    }

    public NPCQuest(int id, String name, String giveMessage, String successMessage, Reward successReward,
            Integer npcId) {
        this(id, name, giveMessage, successMessage, null, successReward, null, npcId);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        npcId = yc.contains("npcId")? yc.getInt("npcId") : null;
    }

    @Override
    protected String serialize(YamlConfiguration yc) {

        yc.set("npcId", npcId);

        return super.serialize(yc);
    }

    @Override
    public boolean onNPCClickEvent(NPCClickEventWrapper event, QuestState state) {
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

        String npcString = ChatColor.DARK_AQUA + "NPC: ";
        if (npcId == null) {
            npcString += ChatColor.RED + "NULL";
        } else {
            npcString += ChatColor.GREEN + "Id: " + npcId;
            if (isForThisServer() && CubeQuest.getInstance().hasCitizensPlugin()) {
                npcString += internalNPCString();
            }
        }

        result.add(new ComponentBuilder(npcString).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    private String internalNPCString() {
        String npcString = "";
        NPC npc = getNPC();
        if (npc == null) {
            npcString += ", " + ChatColor.RED + "EXISTIERT NICHT";
        } else {
            Location loc = npc.isSpawned()? npc.getEntity().getLocation() : npc.getStoredLocation();
            npcString += ", \"" + npc.getFullName() + "\"";
            if (loc != null) {
                npcString += " bei x: " + loc.getX() + ", y:" + loc.getY() + ", z: " + loc.getZ();
            }
        }
        return npcString;
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
        npcId = npc == null? null : npc.getId();
        updateIfReal();
        // Falls eigenes NPCRegistry: UMSCHREIBEN!
    }

}
