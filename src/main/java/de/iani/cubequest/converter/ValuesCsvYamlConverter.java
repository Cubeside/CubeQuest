package de.iani.cubequest.converter;

import de.iani.cubequest.generation.EnumValueMap;
import de.iani.cubequest.generation.QuestGenerator.EntityValueOption;
import de.iani.cubequest.generation.QuestGenerator.MaterialValueOption;
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
        // materials
        
        File materialsInput = new File("conversion/CubesideMaterialValuesInput.csv");
        List<String> materialsLines = new ArrayList<>();
        Scanner materialsScanner = new Scanner(materialsInput);
        while (materialsScanner.hasNext()) {
            materialsLines.add(materialsScanner.next());
        }
        materialsScanner.close();
        
        if (!materialsLines.isEmpty()) {
            String first = materialsLines.get(0);
            // BOM
            if (first.charAt(0) == '\ufeff') {
                first = first.substring(1);
            }
            materialsLines.set(0, first);
        }
        
        EnumValueMap<Material>[] materialsMaps = parseMaterialValues(materialsLines);
        YamlConfiguration materialsConfig = new YamlConfiguration();
        for (MaterialValueOption option : MaterialValueOption.values()) {
            materialsConfig.set("generator.materialValues." + option.name(), materialsMaps[option.ordinal()]);
        }
        
        String materialsResult = materialsConfig.saveToString();
        File materialsOutput = new File("conversion/CubesideMaterialValuesResult.yml");
        try (PrintWriter out = new PrintWriter(materialsOutput)) {
            out.print(materialsResult);
        }
        
        // entities
        
        File entitiesInput = new File("conversion/CubesideEntityValuesInput.csv");
        List<String> entitiesLines = new ArrayList<>();
        Scanner entitiesScanner = new Scanner(entitiesInput);
        while (entitiesScanner.hasNext()) {
            entitiesLines.add(entitiesScanner.next());
        }
        entitiesScanner.close();
        
        if (!entitiesLines.isEmpty()) {
            String first = entitiesLines.get(0);
            if (first.charAt(0) == '\ufeff') {
                first = first.substring(1);
            }
            entitiesLines.set(0, first);
        }
        
        EnumValueMap<EntityType>[] entityMaps = parseEntityValues(entitiesLines);
        YamlConfiguration entitiesConfig = new YamlConfiguration();
        for (EntityValueOption option : EntityValueOption.values()) {
            entitiesConfig.set("generator.entityValues." + option.name(), entityMaps[option.ordinal()]);
        }
        
        String entitiesResult = entitiesConfig.saveToString();
        File entitiesOutput = new File("conversion/CubesideEntityValuesResult.yml");
        try (PrintWriter out = new PrintWriter(entitiesOutput)) {
            out.print(entitiesResult);
        }
        
        System.out.println("DONE.");
    }
    
    @SuppressWarnings("unchecked")
    private static EnumValueMap<Material>[] parseMaterialValues(List<String> input) {
        EnumValueMap<Material>[] result = new EnumValueMap[MaterialValueOption.values().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new EnumValueMap<>(Material.class, 0.0025);
        }
        
        for (String entry : input) {
            String[] data = entry.split(";", -1);
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
    private static EnumValueMap<EntityType>[] parseEntityValues(List<String> input) {
        EnumValueMap<EntityType>[] result = new EnumValueMap[EntityValueOption.values().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new EnumValueMap<>(EntityType.class, 0.1);
        }
        
        for (String entry : input) {
            String[] data = entry.split(";", -1);
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
                        System.out.println("Illegal value for entity type " + entityType + " and option "
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
