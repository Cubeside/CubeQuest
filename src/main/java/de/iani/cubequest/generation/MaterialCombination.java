package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class MaterialCombination
        implements Iterable<Material>, ConfigurationSerializable, Comparable<MaterialCombination> {

    public static final Comparator<MaterialCombination> COMPARATOR = (o1, o2) -> (o1.compareTo(o2));

    private HashSet<Material> content;

    public MaterialCombination() {
        this.content = new HashSet<>();
    }

    public MaterialCombination(Collection<Material> copyOf) {
        this.content = new HashSet<>(copyOf);
    }

    public MaterialCombination(MaterialCombination copyOf) {
        this.content = new HashSet<>(copyOf.content);
    }

    public MaterialCombination(ItemStack[] everyMaterialOccuringInThis) {
        this();
        for (ItemStack stack : everyMaterialOccuringInThis) {
            if (stack != null) {
                this.content.add(stack.getType());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public MaterialCombination(Map<String, Object> serialized) {
        this.content = new HashSet<>();
        List<String> materialNameList = (List<String>) serialized.get("content");
        materialNameList.forEach(materialName -> {
            try {
                this.content.add(Material.valueOf(materialName));
            } catch (IllegalArgumentException e) {
                Material mat = Objects.requireNonNull(Material.matchMaterial(materialName, true));
                if (mat != null) {
                    this.content.add(mat);
                } else {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Material with name \"" + materialName
                            + "\" could not be converted for some unknown quest or quest specification! Now removed from that. Good luck.");
                }
            }
        });
    }

    public Set<Material> getContent() {
        return Collections.unmodifiableSet(this.content);
    }

    public boolean add(Material type) {
        return this.content.add(type);
    }

    public boolean remove(Object type) {
        return this.content.remove(type);
    }

    public void clear() {
        this.content.clear();
    }

    @Override
    public Iterator<Material> iterator() {
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

    public boolean addAll(Collection<? extends Material> c) {
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
    public MaterialCombination clone() {
        return new MaterialCombination(this.content);
    }

    public boolean isLegal() {
        return !this.content.isEmpty();
    }

    @Override
    public int compareTo(MaterialCombination o) {
        int res = 0;
        for (Material m : Material.values()) {
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

        return (other instanceof MaterialCombination) && this.content.equals(((MaterialCombination) other).content);
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
