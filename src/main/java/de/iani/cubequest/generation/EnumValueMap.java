package de.iani.cubequest.generation;

import de.iani.cubequest.util.Util;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class EnumValueMap<T extends Enum<T>> extends ValueMap<T> implements ConfigurationSerializable {
    
    private Class<T> enumClass;
    private Map<T, Double> map;
    
    public EnumValueMap(Class<T> enumClass, double defaultValue) {
        super(defaultValue);
        
        this.enumClass = enumClass;
        this.map = new EnumMap<>(enumClass);
    }
    
    @SuppressWarnings("unchecked")
    public EnumValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);
        
        try {
            String className = (String) serialized.get("enumClass");
            this.enumClass = (Class<T>) Class.forName(className);
            
            Map<String, Object> serializedMap = (Map<String, Object>) serialized.get("map");
            this.map = (Map<T, Double>) Util.deserializeEnumMap(this.enumClass, serializedMap);
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("enumClass", this.enumClass.getName());
        result.put("map", Util.serializedEnumMap(this.map));
        
        return result;
    }
    
    @Override
    protected Map<T, Double> getMap() {
        return this.map;
    }
    
}
