package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubesideutils.bukkit.KeyedUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class KeyedValueMap<T extends Keyed> extends ValueMap<T> implements ConfigurationSerializable {

    private Map<T, Double> map;

    public KeyedValueMap(double defaultValue) {
        super(defaultValue);

        this.map = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public KeyedValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            Map<String, Object> serializedMap = (Map<String, Object>) serialized.get("map");
            this.map = (Map<T, Double>) deserializeKeyedMap(serializedMap, serialized.containsKey("enumClass"));
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("map", serializedKeyedMap(this.map));

        return result;
    }


    private Map<String, Object> serializedKeyedMap(Map<T, ?> map) {
        Map<String, Object> serializedMap = new HashMap<>();
        for (T t : map.keySet()) {
            serializedMap.put(t.getKey().asString(), map.get(t));
        }
        return serializedMap;
    }

    private Map<T, ?> deserializeKeyedMap(Map<String, Object> serialized, boolean convertLegacy) {
        Map<T, Object> result = new LinkedHashMap<>();
        for (String s : serialized.keySet()) {
            String keyString = s;
            if (convertLegacy) {
                try {
                    Keyed k = Material.valueOf(s);
                    keyString = k.getKey().asString();
                } catch (IllegalArgumentException e) {
                    try {
                        Keyed k = EntityType.valueOf(s);
                        keyString = k.getKey().asString();
                    } catch (IllegalArgumentException f) { // ignored
                    }
                }
            }
            T t = KeyedUtil.getFromRegistry(NamespacedKey.fromString(keyString));
            if (t == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "No keyed object with key \"" + s + "\" found, missing in deserialized ValueMap.");
                continue;
            }
            result.put(t, serialized.get(s));
        }
        return result;

    }

    @Override
    protected Map<T, Double> getMap() {
        return this.map;
    }

}
