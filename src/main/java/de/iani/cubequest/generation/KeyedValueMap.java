package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;

public class KeyedValueMap extends ValueMap<NamespacedKey> implements ConfigurationSerializable {

    private Map<NamespacedKey, Double> map;

    public KeyedValueMap(double defaultValue) {
        super(defaultValue);

        this.map = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public KeyedValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);

        try {
            Map<String, Object> serializedMap = (Map<String, Object>) serialized.get("map");
            this.map = (Map<NamespacedKey, Double>) deserializeKeyedMap(serializedMap, serialized.containsKey("enumClass"));
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

    private Map<String, Object> serializedKeyedMap(Map<NamespacedKey, ?> map) {
        Map<String, Object> serializedMap = new HashMap<>();
        for (NamespacedKey t : map.keySet()) {
            serializedMap.put(t.asMinimalString(), map.get(t));
        }
        return serializedMap;
    }

    private Map<NamespacedKey, ?> deserializeKeyedMap(Map<String, Object> serialized, boolean convertLegacy) {
        Map<NamespacedKey, Object> result = new LinkedHashMap<>();
        for (Entry<String, Object> entry : serialized.entrySet()) {
            String keyString = entry.getKey();
            NamespacedKey key = NamespacedKey.fromString(keyString);
            if (convertLegacy) {
                try {
                    Keyed k = Material.valueOf(keyString);
                    key = k.getKey();
                } catch (IllegalArgumentException e) {
                    try {
                        Keyed k = EntityType.valueOf(keyString);
                        key = k.getKey();
                    } catch (IllegalArgumentException f) { // ignored
                    }
                }
            }
            if (key == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING, "No namespaced key : \"" + keyString + "\"");
                continue;
            }
            result.put(key, entry.getValue());
        }
        return result;
    }

    @Override
    protected Map<NamespacedKey, Double> getMap() {
        return this.map;
    }
}
