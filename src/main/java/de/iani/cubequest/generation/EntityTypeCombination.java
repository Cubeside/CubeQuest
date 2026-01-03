package de.iani.cubequest.generation;

import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class EntityTypeCombination
        implements Iterable<EntityType>, ConfigurationSerializable, Comparable<EntityTypeCombination> {

    public static final Comparator<EntityTypeCombination> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

    private HashSet<EntityType> content;

    public EntityTypeCombination() {
        this.content = new HashSet<>();
    }

    public EntityTypeCombination(Collection<EntityType> copyOf) {
        this.content = new HashSet<>(copyOf);
    }

    public EntityTypeCombination(EntityTypeCombination copyOf) {
        this.content = new HashSet<>(copyOf.content);
    }

    @SuppressWarnings("unchecked")
    public EntityTypeCombination(Map<String, Object> serialized) {
        this.content = new HashSet<>();
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList.forEach(materialName -> {
            if (materialName.equals("PIG_ZOMBIE")) {
                this.content.add(EntityType.ZOMBIFIED_PIGLIN);
            } else {
                this.content.add(EntityType.valueOf(DataUpdater.updateEntityTypeName(materialName)));
            }
        });
    }

    public Set<EntityType> getContent() {
        return Collections.unmodifiableSet(this.content);
    }

    public boolean add(EntityType type) {
        return this.content.add(type);
    }

    public boolean remove(EntityType type) {
        return this.content.remove(type);
    }

    public void clear() {
        this.content.clear();
    }

    @Override
    public Iterator<EntityType> iterator() {
        return this.content.iterator();
    }

    public int size() {
        return this.content.size();
    }

    public boolean isEmpty() {
        return this.content.isEmpty();
    }

    public boolean contains(Object o) {
        return this.content.contains(o);
    }

    public Object[] toArray() {
        return this.content.toArray();
    }

    public boolean removeAll(Collection<?> c) {
        return this.content.removeAll(c);
    }

    public <T> T[] toArray(T[] a) {
        return this.content.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return this.content.containsAll(c);
    }

    public boolean addAll(Collection<? extends EntityType> c) {
        return this.content.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return this.content.retainAll(c);
    }

    @Override
    public String toString() {
        return this.content.toString();
    }

    @Override
    public EntityTypeCombination clone() {
        return new EntityTypeCombination(this.content);
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

        return (other instanceof EntityTypeCombination) && this.content.equals(((EntityTypeCombination) other).content);
    }

    public Component getSpecificationInfo() {
        return Component.text(String.valueOf(this.content)).color(NamedTextColor.GREEN);
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
