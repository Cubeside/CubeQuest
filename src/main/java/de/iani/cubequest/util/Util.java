package de.iani.cubequest.util;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.Structure;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitForDateQuest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Util {
    
    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String DATE_AND_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";
    
    private static Random ran = new Random();
    private static int MAX_COLOR_VALUE = (1 << 24) - 1;
    
    @SuppressWarnings("deprecation")
    public static EntityType matchEntityType(String from) {
        from = from.replaceAll("\\,", "");
        from = from.toUpperCase(Locale.ENGLISH);
        
        try {
            return EntityType.valueOf(from);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        EntityType res = EntityType.fromName(from);
        if (res != null) {
            return res;
        }
        try {
            return EntityType.fromId(Integer.parseInt(from));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static <T> T randomElement(List<T> list, Random ran) {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        
        return list.get(ran.nextInt(list.size()));
    }
    
    public static Quest addTimeLimit(Quest targetQuest, Date deadline) {
        WaitForDateQuest timeoutQuest =
                CubeQuest.getInstance().getQuestCreator().createQuest(WaitForDateQuest.class);
        timeoutQuest.setDate(deadline);
        timeoutQuest.setReady(true);
        
        try {
            int dailyQuestId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
            Quest result = new ComplexQuest(dailyQuestId, targetQuest.getName(),
                    targetQuest.getDisplayMessage(), null, null, // Messages
                    CubeQuest.PLUGIN_TAG + " " + ChatColor.RED + "Die Zeit für deine Quest \""
                            + targetQuest.getName() + "\" ist leider abgelaufen.",
                    null, null, // Rewards
                    Structure.ALLTOBEDONE, new HashSet<>(Arrays.asList(targetQuest)), timeoutQuest,
                    null);
            QuestManager.getInstance().addQuest(result);
            targetQuest.setDisplayMessage(targetQuest.getDisplayMessage() == null ? null
                    : targetQuest.getDisplayMessage() + "\n\n" + "Diese Quest läuft am "
                            + (new SimpleDateFormat(Util.DATE_AND_TIME_FORMAT_STRING))
                                    .format(deadline)
                            + " ab.");
            result.setReady(true);
            return result;
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not add deadline to quest.", e);
            return null;
        }
    }
    
    public static void assertCitizens() {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            throw new IllegalStateException("Citizens plugin isn't present but required.");
        }
    }
    
    // color null bedeuted bunt.
    public static void spawnColoredDust(Player player, double amount, double x, double y, double z,
            double offsetX, double offsetY, double offsetZ, Color color) {
        
        int intAmount =
                (int) Math.floor(amount) + (Math.random() < amount - Math.floor(amount) ? 1 : 0);
        boolean randomColor = color == null;
        
        for (int i = 0; i < intAmount; i++) {
            double newX = x + (2 * Math.random() * offsetX) - offsetX;
            double newY = y + (2 * Math.random() * offsetY) - offsetY;
            double newZ = z + (2 * Math.random() * offsetZ) - offsetZ;
            
            // geht, aber nicht so schöne farben
            color = randomColor ? Color.fromRGB(ran.nextInt(MAX_COLOR_VALUE)) : color;
            
            double red = color.getRed() == 0 ? Float.MIN_VALUE : (color.getRed() / 255.0);
            double blue = color.getBlue() / 255.0;
            double green = color.getGreen() / 255.0;
            
            player.spawnParticle(Particle.REDSTONE, newX, newY, newZ, 0, red, green, blue, 1.0);
        }
        
    }
    
    // color null bedeuted bunt, numberOfTicks < 0 bedeuted unendlich.
    // returned: taskId (-1 wenn fehlgeschlagen oder numberOfTicks == 0)
    public static int spawnColoredDust(Player player, double amountPerTick, int numberOfTicks,
            double x, double y, double z, double offsetX, double offsetY, double offsetZ,
            Color... colors) {
        
        if (numberOfTicks == 0) {
            return -1;
        }
        
        BukkitTask runnable = new BukkitRunnable() {
            
            private int count = 0;
            
            @Override
            public void run() {
                if (!player.isValid()) {
                    cancel();
                    return;
                }
                
                Color color = (colors == null || colors.length == 0) ? null
                        : colors[ran.nextInt(colors.length)];
                spawnColoredDust(player, amountPerTick, x, y, z, offsetX, offsetY, offsetZ, color);
                
                if (this.count >= 0 && ++this.count >= numberOfTicks) {
                    cancel();
                }
            }
            
        }.runTaskTimer(CubeQuest.getInstance(), 0, 1);
        
        return runnable.getTaskId();
    }
    
    public static Map<String, Object> serializeLocation(Location loc) {
        if (loc == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("world", loc.getWorld().getName());
        result.put("x", loc.getX());
        result.put("y", loc.getY());
        result.put("z", loc.getZ());
        result.put("yaw", loc.getYaw());
        result.put("pitch", loc.getPitch());
        
        return result;
    }
    
    public static Location deserializeLocation(Map<String, Object> serialized) {
        if (serialized == null) {
            return null;
        }
        
        World world = Bukkit.getWorld((String) serialized.get("world"));
        double x = (double) serialized.get("x");
        double y = (double) serialized.get("y");
        double z = (double) serialized.get("z");
        float yaw = (float) (double) serialized.get("yaw");
        float pitch = (float) (double) serialized.get("pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
}
