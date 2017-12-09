package de.iani.cubequest.interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class EntityInteractor extends Interactor {

    private UUID entityId;

    public EntityInteractor(Entity entity) {
        this.entityId = entity == null? null : entity.getUniqueId();
    }

    public EntityInteractor(UUID entityId) {
        this.entityId = entityId;
    }

    public EntityInteractor(Map<String, Object> serialized) {
        String idString = (String) serialized.get("entityId");
        entityId = idString == null? null : entityId;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("entityId", entityId == null? null : entityId.toString());
        return result;
    }

    @Override
    public boolean isLegal() {
        return entityId != null && (isForThisServer() || Bukkit.getEntity(entityId) != null);
    }

    @Override
    public String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
