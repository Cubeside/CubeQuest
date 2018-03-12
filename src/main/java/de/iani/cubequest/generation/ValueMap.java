package de.iani.cubequest.generation;

import de.iani.cubequest.util.Util;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ValueMap<T extends Enum<T>> implements ConfigurationSerializable {
    
    private Class<T> enumClass;
    private Map<T, Double> map;
    private double defaultValue;
    
    public ValueMap(Class<T> enumClass, double defaultValue) {
        this.enumClass = enumClass;
        this.map = new EnumMap<>(enumClass);
        this.defaultValue = defaultValue;
    }
    
    @SuppressWarnings("unchecked")
    public ValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            String className = (String) serialized.get("enumClass");
            this.enumClass = (Class<T>) Class.forName(className);
            
            this.defaultValue = (Double) serialized.get("defaultValue");
            
            Map<String, Object> serializedMap = (Map<String, Object>) serialized.get("map");
            this.map = (Map<T, Double>) Util.deserializeEnumMap(this.enumClass, serializedMap);
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    public double getValue(T t) {
        return this.map.containsKey(t) ? this.map.get(t) : this.defaultValue;
    }
    
    public void setValue(T t, double value) {
        this.map.put(t, value);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("enumClass", this.enumClass.getName());
        result.put("defaultValue", this.defaultValue);
        
        result.put("map", Util.serializedEnumMap(this.map));
        
        return result;
    }
    
}
