package de.iani.cubequest.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class EntityTypeCombination
        implements ConfigurationSerializable, Comparable<EntityTypeCombination> {
    
    public static final Comparator<EntityTypeCombination> COMPARATOR =
            (o1, o2) -> (o1.compareTo(o2));
    
    private EnumSet<EntityType> content;
    
    public EntityTypeCombination() {
        this.content = EnumSet.noneOf(EntityType.class);
    }
    
    @SuppressWarnings("unchecked")
    public EntityTypeCombination(Map<String, Object> serialized) {
        this.content = EnumSet.noneOf(EntityType.class);
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList
                .forEach(materialName -> this.content.add(EntityType.valueOf(materialName)));
    }
    
    public Set<EntityType> getContent() {
        return Collections.unmodifiableSet(this.content);
    }
    
    public boolean addMaterial(EntityType type) {
        return this.content.add(type);
    }
    
    public boolean removeMaterial(EntityType type) {
        return this.content.remove(type);
    }
    
    public void clearMaterials() {
        this.content.clear();
    }
    
    public boolean isLegal() {
        return !this.content.isEmpty();
    }
    
    @Override
    public int compareTo(EntityTypeCombination o) {
        int res = 0;
        for (EntityType m : EntityType.values()) {
            if (this.content.contains(m)) {
                res++;
            }
            if (o.content.contains(m)) {
                res--;
            }
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }
    
    @Override
    public int hashCode() {
        return this.content.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Set<?>) {
            return this.content.equals(other);
        }
        
        return (other instanceof EntityTypeCombination)
                && this.content.equals(((EntityTypeCombination) other).content);
    }
    
    public BaseComponent[] getSpecificationInfo() {
        return new ComponentBuilder(ChatColor.GREEN + this.content.toString()).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        List<String> materialNameList = new ArrayList<>();
        this.content.forEach(material -> materialNameList.add(material.name()));
        result.put("content", materialNameList);
        return result;
    }
    
}
