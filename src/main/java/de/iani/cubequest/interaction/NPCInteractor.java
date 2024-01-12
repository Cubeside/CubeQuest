package de.iani.cubequest.interaction;

import de.cubeside.npcs.data.SpawnedNPCData;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubequest.util.Util;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class NPCInteractor extends Interactor {

    private UUID npcId;
    private SafeLocation cachedLocation;

    public NPCInteractor(UUID npcId) {
        if (npcId == null) {
            throw new NullPointerException();
        }

        this.npcId = npcId;
    }

    public NPCInteractor(Map<String, Object> serialized) {
        super(serialized);

        this.npcId = UUID.fromString((String) serialized.get("npcId"));
        this.cachedLocation = (SafeLocation) serialized.get("cachedLocation");
    }

    public static class NPCWrapper {

        public final SpawnedNPCData npc;

        private NPCWrapper(SpawnedNPCData npc) {
            this.npc = npc;
        }
    }

    public NPCWrapper getNPC() {
        Util.assertForThisServer(this);
        Util.assertCubesideNPCs();

        return getNPCInternal();
    }

    private NPCWrapper getNPCInternal() {
        return new NPCWrapper(CubeQuest.getInstance().getNPCReg().getById(this.npcId));
    }

    @Override
    public UUID getIdentifier() {
        return this.npcId;
    }

    @Override
    protected String getUncachedName() {
        if (!isForThisServer()) {
            return null;
        }

        return CubeQuest.getInstance().hasCubesideNPCsPlugin() ? getUncachedNameInternal() : null;
    }

    private String getUncachedNameInternal() {
        SpawnedNPCData npc = getNPCInternal().npc;
        return npc == null ? null : npc.getNpcNameString();
    }

    @Override
    public boolean isLegal() {
        if (!isForThisServer()) {
            return true;
        }

        if (!CubeQuest.getInstance().hasCubesideNPCsPlugin()) {
            return false;
        }

        return isLegalInternal();
    }

    private boolean isLegalInternal() {
        SpawnedNPCData npc = getNPCInternal().npc;
        return npc != null;
    }

    @Override
    public String getInfo() {
        return ChatAndTextUtil.getNPCInfoString(getServerId(), this.npcId);
    }

    @Override
    public Location getLocation(boolean ignoreCache) {
        Util.assertForThisServer(this);

        Location loc = CubeQuest.getInstance().hasCubesideNPCsPlugin() ? getNonCachedLocationInternal() : null;
        if (loc != null) {
            SafeLocation oldCachedLocation = this.cachedLocation;
            this.cachedLocation = new SafeLocation(loc);

            if (oldCachedLocation == null || !oldCachedLocation.isSimilar(loc)) {
                cacheChanged();
            }
        } else if (!ignoreCache && this.cachedLocation != null) {
            loc = this.cachedLocation.getLocation();
        }
        return loc;
    }

    private Location getNonCachedLocationInternal() {
        SpawnedNPCData npc = getNPC().npc;
        if (npc == null) {
            return null;
        }

        return npc.getLastKnownLocation();
    }

    @Override
    public double getHeight() {
        Util.assertForThisServer(this);
        Util.assertCubesideNPCs();

        return getHeightInternal();
    }

    private double getHeightInternal() {
        SpawnedNPCData npc = getNPCInternal().npc;
        Entity entity = npc != null ? npc.getLoadedEntity() : null;
        return entity != null ? entity.getHeight() : 2;
    }

    @Override
    public double getWidth() {
        Util.assertForThisServer(this);
        Util.assertCubesideNPCs();

        return getWidthInternal();
    }

    private double getWidthInternal() {
        SpawnedNPCData npc = getNPCInternal().npc;
        Entity entity = npc != null ? npc.getLoadedEntity() : null;
        return entity != null ? entity.getWidth() : 1;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("npcId", this.npcId.toString());
        result.put("cachedLocation", this.cachedLocation);
        return result;
    }

    @Override
    public int compareTo(Interactor o) {
        int result = super.compareTo(o);

        if (result != 0) {
            return result;
        }
        assert (this.getClass() == o.getClass());

        return getIdentifier().compareTo((UUID) o.getIdentifier());
    }

}
