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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Util {
    
    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String DATE_AND_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";
    
    private static Random ran = new Random();
    
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
            
            color = randomColor ? Color.fromBGR(ran.nextInt(65536)) : color; // TODO: test
            
            double red = color.getRed() == 0 ? Float.MIN_VALUE : (color.getRed() / 255.0);
            double blue = color.getBlue() / 255.0;
            double green = color.getGreen() / 255.0;
            
            player.spawnParticle(Particle.REDSTONE, newX, newY, newZ, 0, red, blue, green, 1.0);
        }
        
    }
    
    // color null bedeuted bunt, numberOfTicks < 0 bedeuted unendlich.
    // returned: taskId (-1 wenn fehlgeschlagen oder numberOfTicks == 0)
    public static int spawnColoredDust(Player player, double amountPerTick, int numberOfTicks,
            double x, double y, double z, double offsetX, double offsetY, double offsetZ,
            Color color) {
        
        if (numberOfTicks == 0) {
            return -1;
        }
        
        BukkitTask runnable = new BukkitRunnable() {
            
            private int count = numberOfTicks;
            
            @Override
            public void run() {
                spawnColoredDust(player, amountPerTick, x, y, z, offsetX, offsetY, offsetZ, color);
                
                if (this.count >= 0 && ++this.count >= numberOfTicks) {
                    cancel();
                }
            }
            
        }.runTaskTimer(CubeQuest.getInstance(), 0, 1);
        
        return runnable.getTaskId();
    }
    
}
