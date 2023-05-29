package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubesideutils.bukkit.KeyedUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

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
            this.map = (Map<T, Double>) deserializeKeyedMap(serializedMap);
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

    private Map<T, ?> deserializeKeyedMap(Map<String, Object> serialized) {
        Map<T, Object> result = new LinkedHashMap<>();
        for (String keyString : serialized.keySet()) {
            T t = KeyedUtil.getFromRegistry(NamespacedKey.fromString(keyString));
            if (t == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "No keyed object with key \"" + keyString + "\" found, missing in deserialized ValueMap.");
                continue;
            }
            result.put(t, serialized.get(keyString));
        }
        return result;
    }

    @Override
    protected Map<T, Double> getMap() {
        return this.map;
    }

}
