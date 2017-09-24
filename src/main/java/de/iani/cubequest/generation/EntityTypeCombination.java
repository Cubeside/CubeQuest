package de.iani.cubequest.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class EntityTypeCombination implements ConfigurationSerializable, Comparable<EntityTypeCombination> {

    public static final Comparator<EntityTypeCombination> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

    private EnumSet<EntityType> content;

    public EntityTypeCombination() {
        content = EnumSet.noneOf(EntityType.class);
    }

    @SuppressWarnings("unchecked")
    public EntityTypeCombination(Map<String, Object> serialized) {
        content = EnumSet.noneOf(EntityType.class);
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList.forEach(materialName -> content.add(EntityType.valueOf(materialName)));
    }

    public Set<EntityType> getContent() {
        return Collections.unmodifiableSet(content);
    }

    public boolean addMaterial(EntityType type) {
        return content.add(type);
    }

    public boolean removeMaterial(EntityType type) {
        return content.remove(type);
    }

    public void clearMaterials() {
        content.clear();
    }

    public boolean isLegal() {
        return !content.isEmpty();
    }

    @Override
    public int compareTo(EntityTypeCombination o) {
        int res = 0;
        for (EntityType m: EntityType.values()) {
            if (content.contains(m)) {
                res ++;
            }
            if (o.content.contains(m)) {
                res --;
            }
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EntityTypeCombination)) {
            return false;
        }
        return ((EntityTypeCombination) other).content.equals(content);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<String> materialNameList = new ArrayList<String>();
        content.forEach(material -> materialNameList.add(material.name()));
        result.put("content", materialNameList);
        return result;
    }

}
