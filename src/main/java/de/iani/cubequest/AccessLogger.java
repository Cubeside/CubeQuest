package de.iani.cubequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class AccessLogger {
    
    private static List<UUID> accesses = new ArrayList<>();
    
    public static void log(UUID id) {
        accesses.add(id);
    }
    
    public static void save() {
        List<UUID> toSave = accesses;
        accesses = new ArrayList<>(toSave.size());
        new Thread(() -> fullSave(toSave)).start();
    }
    
    public static void fullSave() {
        List<UUID> toSave = accesses;
        accesses = new ArrayList<>(toSave.size());
        fullSave(toSave);
    }
    
    public static synchronized void fullSave(List<UUID> toSave) {
        int fileIndex = 0;
        File folder = new File(CubeQuest.getInstance().getDataFolder(), "access_logs");
        folder.mkdirs();
        File file;
        do {
            file = new File(folder, "log" + fileIndex + ".txt");
            fileIndex++;
        } while (file.exists());
        
        try (Writer writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            for (UUID id : toSave) {
                writer.write(id.toString());
                writer.write("\n");
            }
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save access log.", e);
        }
    }
    
}
