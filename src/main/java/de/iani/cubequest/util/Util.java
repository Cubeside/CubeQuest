package de.iani.cubequest.util;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.ServerSpecific;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.Structure;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitForDateQuest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Util {
    
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
            ComplexQuest result = new ComplexQuest(dailyQuestId, targetQuest.getName(),
                    targetQuest.getDisplayMessage(), null, null, // Messages
                    CubeQuest.PLUGIN_TAG + " " + ChatColor.RED + "Die Zeit für deine Quest \""
                            + targetQuest.getName() + "\" ist leider abgelaufen.",
                    null, null, // Rewards
                    Structure.ALL_TO_BE_DONE, new HashSet<>(Arrays.asList(targetQuest)),
                    timeoutQuest, null);
            QuestManager.getInstance().addQuest(result);
            
            result.setDelayDatabaseUpdate(true);
            targetQuest.setDelayDatabaseUpdate(true);
            
            result.setDisplayMessage((targetQuest.getDisplayMessage() == null ? ""
                    : (targetQuest.getDisplayMessage() + "\n\n")) + "Diese Quest läuft am "
                    + ChatAndTextUtil.formatDate(deadline) + " ab.");
            
            result.setGiveMessage(targetQuest.getGiveMessage());
            targetQuest.setGiveMessage(null);
            
            result.setSuccessMessage(targetQuest.getSuccessMessage());
            targetQuest.setSuccessMessage(null);
            
            result.setSuccessReward(targetQuest.getSuccessReward());
            targetQuest.setSuccessReward(null);
            
            result.setFailReward(targetQuest.getFailReward());
            targetQuest.setFailReward(null);
            
            if (targetQuest.isVisible()) {
                result.setVisible(true);
                targetQuest.setVisible(false);
            }
            
            result.setOnDeleteCascade(true);
            result.setReady(true);
            
            result.setDelayDatabaseUpdate(false);
            targetQuest.setDelayDatabaseUpdate(false);
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
    
    public static void assertForThisServer(ServerSpecific arg) {
        if (!arg.isForThisServer()) {
            throw new IllegalStateException(
                    "Operation can only be performed on the server that this ServerSpecific belongs to, which isn't this server.");
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
            
            player.spawnParticle(Particle.REDSTONE, newX, newY, newZ, 1, 0.0f, 0.0f, 0.0f, 1.0,
                    new DustOptions(color, 1.0f));
            
            // double red = color.getRed() == 0 ? Float.MIN_VALUE : (color.getRed() / 255.0);
            // double blue = color.getBlue() / 255.0;
            // double green = color.getGreen() / 255.0;
            
            // player.spawnParticle(Particle.REDSTONE, newX, newY, newZ, 1, red, blue, green, 1.0,
            // new DustOptions(color, 1.0f));
            
            // player.spawnParticle(Particle.REDSTONE, newX, newY, newZ, 0, red, green, blue, 1.0);
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
    
    public static <T extends Enum<T>> Map<String, Object> serializedEnumMap(Map<T, ?> map) {
        Map<String, Object> serializedMap = new HashMap<>();
        for (Enum<T> t : map.keySet()) {
            serializedMap.put(t.name(), map.get(t));
        }
        return serializedMap;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumMap<T, ?> deserializeEnumMap(Class<T> enumClass,
            Map<String, Object> serialized) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        EnumMap<T, Object> result = new EnumMap<>(enumClass);
        Method getter = enumClass.getMethod("valueOf", String.class);
        for (String name : serialized.keySet()) {
            T t = (T) getter.invoke(null, name);
            result.put(t, serialized.get(name));
        }
        return result;
    }
    
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
        return () -> {
            return new Iterator<T>() {
                
                private Iterator<T>[] iterators =
                        Arrays.stream(iterables).map(iterable -> iterable.iterator())
                                .toArray(i -> new Iterator[iterables.length]);
                private int index = 0;
                
                @Override
                public boolean hasNext() {
                    if (this.index >= this.iterators.length) {
                        return false;
                    }
                    if (this.iterators[this.index].hasNext()) {
                        return true;
                    } else {
                        this.index++;
                        return hasNext();
                    }
                }
                
                @Override
                public T next() {
                    try {
                        return this.iterators[this.index].next();
                    } catch (NoSuchElementException e) {
                        if (hasNext()) {
                            return next();
                        } else {
                            throw e;
                        }
                    }
                }
                
            };
        };
    }
    
    @SuppressWarnings("unchecked")
    public static <T> void addAll(Collection<? super T> collection, T... array) {
        for (T t : array) {
            collection.add(t);
        }
    }
    
    public static byte[] byteArray(long from) {
        byte[] result = new byte[8];
        
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (from >>> (8 * i));
        }
        
        return result;
    }
    
    public static long fromBytes(byte[] from) {
        long result = 0;
        long mask = 0xFF;
        
        for (int i = 0; i < 8; i++) {
            byte current = i < from.length ? from[i] : 0;
            result |= (((long) current) << (8 * i)) & mask;
            mask <<= 8;
        }
        
        return result;
    }
    
    public static boolean isSafeGiverName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_' && c != '&'
                    && c != '.') {
                return false;
            }
        }
        return true;
    }
    
    public static <T> T[] arrayConcat(T t, T[] array) {
        T[] result = Arrays.copyOf(array, array.length + 1);
        result[0] = t;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static <T> T[] arrayConcat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    
}
