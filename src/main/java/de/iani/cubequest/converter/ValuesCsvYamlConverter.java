package de.iani.cubequest.converter;

import de.iani.cubequest.generation.QuestGenerator.EntityValueOption;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
import de.iani.cubequest.generation.ValueMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class ValuesCsvYamlConverter {
    
    public static void main(String[] args) throws IOException {
        File materialsInput = new File("conversion/Values3.csv");
        List<String> lines = new ArrayList<>();
        Scanner scanner = new Scanner(materialsInput);
        while (scanner.hasNext()) {
            lines.add(scanner.next());
        }
        scanner.close();
        
        ValueMap<Material>[] maps = parseMaterialValues(lines);
        YamlConfiguration config = new YamlConfiguration();
        for (MaterialValueOption option : MaterialValueOption.values()) {
            config.set(option.name(), maps[option.ordinal()]);
        }
        
        String result = config.saveToString();
        File materialsOutput = new File("conversion/MaterialsResult.yml");
        try (PrintWriter out = new PrintWriter(materialsOutput)) {
            out.print(result);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static ValueMap<Material>[] parseMaterialValues(List<String> input) {
        ValueMap<Material>[] result = new ValueMap[MaterialValueOption.values().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ValueMap<>(Material.class, 0.0025);
        }
        
        for (String entry : input) {
            String[] data = entry.split(";");
            String materialName = data[0];
            if (materialName.isEmpty()) {
                continue;
            }
            
            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown material: " + e);
                continue;
            }
            
            try {
                for (int i = 0; i < result.length; i++) {
                    String valueString = data[i + 1];
                    if (valueString.isEmpty() || valueString.equals("-")) {
                        continue;
                    }
                    
                    double value;
                    try {
                        value = Double.parseDouble(valueString);
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal value for material " + material + " and option "
                                + MaterialValueOption.values()[i] + ": " + valueString);
                        continue;
                    }
                    
                    result[i].setValue(material, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception for entry: " + entry, e);
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private static ValueMap<EntityType>[] parseEntityValues(List<String> input) {
        ValueMap<EntityType>[] result = new ValueMap[EntityValueOption.values().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ValueMap<>(EntityType.class, 0.1);
        }
        
        for (String entry : input) {
            String[] data = entry.split(";");
            String entityTypeName = data[0];
            if (entityTypeName.isEmpty()) {
                continue;
            }
            
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(entityTypeName);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown entity type: " + e);
                continue;
            }
            
            try {
                for (int i = 0; i < result.length; i++) {
                    String valueString = data[i + 1];
                    if (valueString.isEmpty() || valueString.equals("-")) {
                        continue;
                    }
                    
                    double value;
                    try {
                        value = Double.parseDouble(valueString);
                    } catch (NumberFormatException e) {
                        System.out.println(
                                "Illegal value for entity type " + entityType + " and option "
                                        + EntityValueOption.values()[i] + ": " + valueString);
                        continue;
                    }
                    
                    result[i].setValue(entityType, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception for entry: " + entry, e);
            }
        }
        
        return result;
    }
    
}
