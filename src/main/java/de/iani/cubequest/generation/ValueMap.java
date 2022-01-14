package de.iani.cubequest.generation;



import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class ValueMap<T> implements ConfigurationSerializable {
    
    private double defaultValue;
    
    protected abstract Map<T, Double> getMap();
    
    public ValueMap(double defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public ValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            this.defaultValue = (Double) serialized.get("defaultValue");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    public double getValue(T t) {
        return this.getMap().containsKey(t) ? this.getMap().get(t) : this.defaultValue;
    }
    
    public void setValue(T t, double value) {
        this.getMap().put(t, value);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("defaultValue", this.defaultValue);
        
        return result;
    }
    
}
