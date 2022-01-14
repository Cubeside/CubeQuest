package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubesidestats.api.StatisticKey;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.configuration.InvalidConfigurationException;

public class StatisticValueMap extends ValueMap<StatisticKey> {
    
    private Map<StatisticKey, Double> map;
    
    public StatisticValueMap(double defaultValue) {
        super(defaultValue);
        
        this.map = new HashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    public StatisticValueMap(Map<String, Object> serialized) throws InvalidConfigurationException {
        super(serialized);
        
        try {
            Map<String, Object> serializedMap = (Map<String, Object>) serialized.get("map");
            this.map = new HashMap<>();
            
            for (Entry<String, Object> entry : serializedMap.entrySet()) {
                StatisticKey key =
                        CubeQuest.getInstance().getCubesideStatistics().getStatisticKey(entry.getKey(), false);
                if (key == null) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "StatisticKey with name \"" + entry.getKey()
                            + "\" was missing for statistic values. Now has no value.");
                } else {
                    this.map.put(key, ((Number) entry.getValue()).doubleValue());
                }
            }
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("map",
                this.map.entrySet().stream().map(entry -> new SimpleEntry<>(entry.getKey().getName(), entry.getValue()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        
        return result;
    }
    
    @Override
    protected Map<StatisticKey, Double> getMap() {
        return this.map;
    }
    
}
