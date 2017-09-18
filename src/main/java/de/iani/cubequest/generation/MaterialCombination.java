package de.iani.cubequest.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class MaterialCombination implements ConfigurationSerializable, Comparable<MaterialCombination> {

    public static final Comparator<MaterialCombination> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

    private EnumSet<Material> content;

    public MaterialCombination() {
        content = EnumSet.noneOf(Material.class);
    }

    @SuppressWarnings("unchecked")
    public MaterialCombination(Map<String, Object> serialized) {
        content = EnumSet.noneOf(Material.class);
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList.forEach(materialName -> content.add(Material.valueOf(materialName)));
    }

    public Set<Material> getContent() {
        return Collections.unmodifiableSet(content);
    }

    public boolean addMaterial(Material type) {
        return content.add(type);
    }

    public boolean removeMaterial(Material type) {
        return content.remove(type);
    }

    public void clearMaterials() {
        content.clear();
    }

    public boolean isLegal() {
        return !content.isEmpty();
    }

    @Override
    public int compareTo(MaterialCombination o) {
        int res = 0;
        for (Material m: Material.values()) {
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
        if (!(other instanceof MaterialCombination)) {
            return false;
        }
        return ((MaterialCombination) other).content.equals(content);
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