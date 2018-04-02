package de.iani.cubequest.util;

import com.google.common.collect.Iterables;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.commands.ArgsParser;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ChatAndTextUtil {
    
    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String TIME_FORMAT_STRING = "HH:mm";
    public static final String TIME_SECONDS_FORMAT_STRING = "HH:mm:ss";
    public static final String DATE_AND_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm";
    public static final String DATE_AND_TIME_SECONDS_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";
    
    private static DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
    private static DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
    private static DateFormat timeSecondsFormat = new SimpleDateFormat(TIME_SECONDS_FORMAT_STRING);
    // private static DateFormat dateAndTimeFormat = new
    // SimpleDateFormat(DATE_AND_TIME_FORMAT_STRING);
    // private static DateFormat dateAndTimeSecondsFormat =
    // new SimpleDateFormat(DATE_AND_TIME_SECONDS_FORMAT_STRING);
    
    private static TreeMap<Integer, String> romanNumberMap;
    
    private static Map<Color, String> constantColors;
    
    private static Map<Enchantment, String> enchantmentToName;
    
    private static Predicate<Object> acceptEverything = o -> true;
    
    static {
        romanNumberMap = new TreeMap<>();
        romanNumberMap.put(1000, "M");
        romanNumberMap.put(900, "CM");
        romanNumberMap.put(500, "D");
        romanNumberMap.put(400, "CD");
        romanNumberMap.put(100, "C");
        romanNumberMap.put(90, "XC");
        romanNumberMap.put(50, "L");
        romanNumberMap.put(40, "XL");
        romanNumberMap.put(10, "X");
        romanNumberMap.put(9, "IX");
        romanNumberMap.put(5, "V");
        romanNumberMap.put(4, "IV");
        romanNumberMap.put(1, "I");
        
        constantColors = new LinkedHashMap<>();
        constantColors.put(Color.AQUA, "aqua");
        constantColors.put(Color.BLACK, "black");
        constantColors.put(Color.BLUE, "blue");
        constantColors.put(Color.FUCHSIA, "fuchsia");
        constantColors.put(Color.GRAY, "gray");
        constantColors.put(Color.GREEN, "greeen");
        constantColors.put(Color.LIME, "lime");
        constantColors.put(Color.MAROON, "maroon");
        constantColors.put(Color.NAVY, "navy");
        constantColors.put(Color.OLIVE, "olive");
        constantColors.put(Color.ORANGE, "orange");
        constantColors.put(Color.PURPLE, "purple");
        constantColors.put(Color.RED, "Aqua");
        constantColors.put(Color.SILVER, "red");
        constantColors.put(Color.TEAL, "teal");
        constantColors.put(Color.WHITE, "white");
        constantColors.put(Color.YELLOW, "yellow");
        
        for (DyeColor dc: DyeColor.values()) {
            constantColors.put(dc.getColor(),
                    dc.name().replaceAll(Pattern.quote("_"), " ").toLowerCase());
        }
        
        enchantmentToName = new HashMap<>();
        
        enchantmentToName.put(Enchantment.ARROW_DAMAGE, "Power");
        enchantmentToName.put(Enchantment.ARROW_FIRE, "Flame");
        enchantmentToName.put(Enchantment.ARROW_INFINITE, "Infinity");
        enchantmentToName.put(Enchantment.ARROW_KNOCKBACK, "Punch");
        enchantmentToName.put(Enchantment.BINDING_CURSE, ChatColor.RED + "Curse of Binding");
        enchantmentToName.put(Enchantment.DAMAGE_ALL, "Sharpness");
        enchantmentToName.put(Enchantment.DAMAGE_ARTHROPODS, "Bane of Anthropods");
        enchantmentToName.put(Enchantment.DAMAGE_UNDEAD, "Smite");
        enchantmentToName.put(Enchantment.DIG_SPEED, "Efficiency");
        enchantmentToName.put(Enchantment.DURABILITY, "Unbreaking");
        enchantmentToName.put(Enchantment.LOOT_BONUS_BLOCKS, "Fortune");
        enchantmentToName.put(Enchantment.LOOT_BONUS_MOBS, "Looting");
        enchantmentToName.put(Enchantment.LUCK, "Luck of the Sea");
        enchantmentToName.put(Enchantment.OXYGEN, "Respiration");
        enchantmentToName.put(Enchantment.PROTECTION_ENVIRONMENTAL, "Protection");
        enchantmentToName.put(Enchantment.PROTECTION_EXPLOSIONS, "Blast Protection");
        enchantmentToName.put(Enchantment.PROTECTION_FALL, "Feather Falling");
        enchantmentToName.put(Enchantment.PROTECTION_FIRE, "Fire Protection");
        enchantmentToName.put(Enchantment.PROTECTION_PROJECTILE, "Projectile Protection");
        enchantmentToName.put(Enchantment.VANISHING_CURSE, ChatColor.RED + "Curse of Vanishing");
        enchantmentToName.put(Enchantment.WATER_WORKER, "Aqua Affinity");
    }
    
    public static void sendNormalMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(CubeQuest.PLUGIN_TAG + " " + ChatColor.GREEN + msg);
    }
    
    public static void sendWarningMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + msg);
    }
    
    public static void sendErrorMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(CubeQuest.PLUGIN_TAG + " " + ChatColor.RED + msg);
    }
    
    public static void sendMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(CubeQuest.PLUGIN_TAG + " " + msg);
    }
    
    public static void sendNoPermissionMessage(CommandSender recipient) {
        sendErrorMessage(recipient, "Dazu fehlt dir die Berechtigung!");
    }
    
    public static void sendXpAndQuestPointsMessage(CommandSender recipient, int xp,
            int questPoints) {
        String pointsString = "Du hast ";
        
        boolean first = true;
        if (xp != 0) {
            pointsString += xp + " Quest-XP";
            first = false;
        }
        if (questPoints != 0) {
            pointsString += (first ? "" : " und ") + questPoints + " Quest-Punkte";
            first = false;
        }
        
        if (!first) {
            pointsString += " erhalten.";
            sendNormalMessage(recipient, pointsString);
        }
    }
    
    public static String formatTimespan(long ms) {
        return formatTimespan(ms, "d", "h", "m", "s", "", "");
    }
    
    public static String formatTimespan(long ms, String d, String h, String m, String s,
            String delimiter, String lastDelimiter) {
        return formatTimespan(ms, d, h, m, s, delimiter, lastDelimiter, true);
    }
    
    public static String formatTimespan(long ms, String d, String h, String m, String s,
            String delimiter, String lastDelimiter, boolean dropAllLowerIfZero) {
        return formatTimespan(ms, d, h, m, s, delimiter, lastDelimiter, dropAllLowerIfZero, false);
    }
    
    public static String formatTimespan(long ms, String d, String h, String m, String s,
            String delimiter, String lastDelimiter, boolean dropAllLowerIfZero,
            boolean forceMinutesAndTwoDigitsForTime) {
        long days = ms / (1000L * 60L * 60L * 24L);
        ms -= days * (1000L * 60L * 60L * 24L);
        long hours = ms / (1000L * 60L * 60L);
        ms -= hours * (1000L * 60L * 60L);
        long minutes = ms / (1000L * 60L);
        ms -= minutes * (1000L * 60L);
        long seconds = ms / 1000L;
        ms -= seconds * 1000L;
        double lessThanSeconds = (ms / 1000.0);
        
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        boolean allNext = false;
        
        if (days != 0) {
            first = false;
            allNext = !dropAllLowerIfZero;
            
            builder.append(days);
            builder.append(d);
        }
        if (allNext || hours != 0) {
            if (!first) {
                if (allNext || minutes != 0 || seconds != 0 || lessThanSeconds != 0) {
                    builder.append(delimiter);
                } else {
                    builder.append(lastDelimiter);
                }
            }
            
            first = false;
            allNext = !dropAllLowerIfZero;
            
            if (forceMinutesAndTwoDigitsForTime && hours < 10) {
                builder.append('0');
            }
            builder.append(hours);
            builder.append(h);
        }
        if (allNext || forceMinutesAndTwoDigitsForTime || minutes != 0) {
            if (!first) {
                if (allNext || seconds != 0 || lessThanSeconds != 0) {
                    builder.append(delimiter);
                } else {
                    builder.append(lastDelimiter);
                }
            }
            
            first = false;
            allNext = !dropAllLowerIfZero;
            
            if (forceMinutesAndTwoDigitsForTime && minutes < 10) {
                builder.append('0');
            }
            builder.append(minutes);
            builder.append(m);
        }
        if (allNext || seconds != 0 || lessThanSeconds != 0) {
            if (!first) {
                builder.append(lastDelimiter);
            }
            
            first = false;
            allNext = !dropAllLowerIfZero;
            
            if (forceMinutesAndTwoDigitsForTime && seconds < 10) {
                builder.append('0');
            }
            builder.append(seconds);
            if (lessThanSeconds != 0) {
                builder.append(".");
                String lessThanSecondsString = "" + lessThanSeconds;
                lessThanSecondsString =
                        lessThanSecondsString.substring(lessThanSecondsString.indexOf('.'));
                builder.append(lessThanSecondsString);
            }
            builder.append(s);
        }
        
        String result = builder.toString().trim();
        if (!result.equals("")) {
            return result;
        }
        
        return ("0" + s).trim();
    }
    
    public static synchronized String formatDate(long date) {
        return formatDate(new Date(date));
    }
    
    public static synchronized String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        String result = dateFormat.format(date);
        
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if (hour == 0 && minute == 0 && second == 0) {
            return result;
        }
        
        result += " " + (second == 0 ? timeFormat.format(date) : timeSecondsFormat.format(date))
                + " Uhr";
        return result;
    }
    
    public static String toRomanNumber(int arg) {
        int i = romanNumberMap.floorKey(arg);
        if (arg == i) {
            return romanNumberMap.get(arg);
        }
        return romanNumberMap.get(i) + toRomanNumber(arg - i);
    }
    
    public static String capitalize(String s, boolean replaceUnderscores) {
        char[] cap = s.toCharArray();
        boolean lastSpace = true;
        for (int i = 0; i < cap.length; i++) {
            if (cap[i] == '_') {
                if (replaceUnderscores) {
                    cap[i] = ' ';
                }
                lastSpace = true;
            } else if (cap[i] >= '0' && cap[i] <= '9') {
                lastSpace = true;
            } else if (cap[i] == ' ') {
                lastSpace = true;
            } else {
                if (lastSpace) {
                    cap[i] = Character.toUpperCase(cap[i]);
                } else {
                    cap[i] = Character.toLowerCase(cap[i]);
                }
                lastSpace = false;
            }
        }
        return new String(cap);
    }
    
    public static Quest getQuest(CommandSender sender, ArgsParser args,
            String commandOnSelectionByClickingPreId, String commandOnSelectionByClickingPostId,
            String hoverTextPreId, String hoverTextPostId) {
        return getQuest(sender, args, acceptEverything, false, commandOnSelectionByClickingPreId,
                commandOnSelectionByClickingPostId, hoverTextPreId, hoverTextPostId);
    }
    
    public static Quest getQuest(CommandSender sender, ArgsParser args,
            Predicate<? super Quest> questFilter, boolean considerNonVisibleInErrorMessage,
            String commandOnSelectionByClickingPreId, String commandOnSelectionByClickingPostId,
            String hoverTextPreId, String hoverTextPostId) {
        
        if (!commandOnSelectionByClickingPreId.startsWith("/")) {
            commandOnSelectionByClickingPreId = "/" + commandOnSelectionByClickingPreId;
        }
        
        String idString = args.getNext("");
        try {
            int id = Integer.parseInt(idString);
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null || !questFilter.test(quest)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id
                        + (considerNonVisibleInErrorMessage ? ", die für dich sichtbar ist" : "")
                        + ".");
                return null;
            }
            return quest;
        } catch (NumberFormatException e) {
            String questString = args.hasNext() ? idString + " " + args.getAll("") : idString;
            System.out.println(QuestManager.getInstance().getQuests(questString));
            List<Quest> quests = QuestManager.getInstance().getQuests(questString).stream()
                    .filter(questFilter).collect(Collectors.toList());
            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen \""
                        + questString + "\""
                        + (considerNonVisibleInErrorMessage ? ", die für dich sichtbar ist" : "")
                        + ".");
                return null;
            } else if (quests.size() > 1) {
                quests.sort(Quest.QUEST_LIST_COMPARATOR);
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(hoverTextPreId + q.getId() + hoverTextPostId)
                                        .create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                commandOnSelectionByClickingPreId + q.getId()
                                        + commandOnSelectionByClickingPostId);
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName()
                                + " " + q.getId();
                        ComponentBuilder cb =
                                new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return null;
            }
            Bukkit.dispatchCommand(sender,
                    commandOnSelectionByClickingPreId.substring(1)
                            + Iterables.getFirst(quests, null).getId()
                            + commandOnSelectionByClickingPostId);
            return null;
        }
    }
    
    public static Location getLocation(CommandSender sender, ArgsParser args, boolean noPitchOrYaw,
            boolean roundToBlock) {
        Location result;
        
        if (args.remaining() < 4) {
            if (!args.hasNext() && sender instanceof Player) {
                result = ((Player) sender).getLocation();
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Welt und die x-, y- und z-Koordinate des Orts an.");
                return null;
            }
        } else {
            String worldString = args.getNext();
            World world = Bukkit.getWorld(worldString);
            if (world == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Welt " + worldString + " nicht gefunden.");
                return null;
            }
            int x, y, z;
            float pitch = 0.0f, yaw = 0.0f;
            try {
                x = Integer.parseInt(args.getNext());
                y = Integer.parseInt(args.getNext());
                z = Integer.parseInt(args.getNext());
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die x- y- und z-Koordinate des Orts als ganze Zahlen an.");
                return null;
            }
            if (!noPitchOrYaw && args.remaining() > 1) {
                if (args.remaining() < 2) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "Bitte gib entweder nur x, y und z oder x, y, z, pitch und yaw an.");
                    return null;
                }
                try {
                    pitch = Float.parseFloat(args.getNext());
                    yaw = Float.parseFloat(args.getNext());
                } catch (NumberFormatException e) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "Bitte gib pitch und yaw des Orts als Gleitkommazahlen an.");
                    return null;
                }
            }
            result = new Location(world, x, y, z, pitch, yaw);
        }
        
        if (roundToBlock) {
            result = result.getBlock().getLocation();
        } else if (noPitchOrYaw) {
            result.setPitch(0);
            result.setYaw(0);
        }
        
        return result;
    }
    
    public static boolean sendBaseComponent(CommandSender sender, BaseComponent[]... components) {
        return sendBaseComponent(sender, Arrays.asList(components));
    }
    
    public static boolean sendBaseComponent(CommandSender sender,
            List<BaseComponent[]> components) {
        if (sender instanceof Player) {
            for (BaseComponent[] bc: components) {
                ((Player) sender).spigot().sendMessage(bc);
            }
            return true;
        } else {
            for (BaseComponent[] bca: components) {
                String msg = "";
                for (BaseComponent bc: bca) {
                    msg += bc.toPlainText() + " ";
                }
                sender.sendMessage(msg);
            }
            return false;
        }
    }
    
    public static String replaceLast(String in, String sequence, String replacement) {
        int index = in.lastIndexOf(sequence);
        if (index < 0) {
            return in;
        }
        
        return in.substring(0, index) + replacement
                + in.substring(index + sequence.length(), in.length());
    }
    
    public static String getNPCInfoString(Integer npcId) {
        return getNPCInfoString(CubeQuest.getInstance().getServerId(), npcId);
    }
    
    public static String getNPCInfoString(int serverId, Integer npcId) {
        boolean forThisServer = serverId == CubeQuest.getInstance().getServerId();
        String npcString = "";
        if (npcId == null) {
            npcString += ChatColor.RED + "NULL";
        } else {
            npcString += ChatColor.GREEN + "Id: " + npcId;
            if (forThisServer && CubeQuest.getInstance().hasCitizensPlugin()) {
                npcString += internalNPCInfoString(npcId);
            } else {
                npcString += ", steht auf Server " + serverId;
            }
        }
        return npcString;
    }
    
    private static String internalNPCInfoString(int npcId) {
        String npcString = "";
        NPC npc = CubeQuest.getInstance().getNPCReg().getById(npcId);
        if (npc == null) {
            npcString += ", " + ChatColor.RED + "EXISTIERT NICHT";
        } else {
            Location loc =
                    npc.isSpawned() ? npc.getEntity().getLocation() : npc.getStoredLocation();
            npcString += ", \"" + npc.getFullName() + "\"";
            if (loc != null) {
                loc = roundLocation(loc, 1);
                npcString += " bei x: " + loc.getX() + ", y: " + loc.getY() + ", z: " + loc.getZ();
            }
        }
        return npcString;
    }
    
    public static String getEntityInfoString(UUID entityId) {
        return getEntityInfoString(CubeQuest.getInstance().getServerId(), entityId);
    }
    
    public static String getEntityInfoString(int serverId, UUID entityId) {
        boolean forThisServer = serverId == CubeQuest.getInstance().getServerId();
        String entityString = "";
        if (entityId == null) {
            entityString += ChatColor.RED + "NULL";
        } else {
            entityString += ChatColor.GREEN + "Id: " + entityId;
            if (forThisServer) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity == null) {
                    entityString += ", " + ChatColor.RED + "EXISTIERT NICHT";
                } else {
                    Location loc = entity.getLocation();
                    entityString += ", \"" + entity.getName() + "\"";
                    if (loc != null) {
                        entityString += " bei x: " + loc.getX() + ", y: " + loc.getY() + ", z: "
                                + loc.getZ();
                    }
                }
            } else {
                entityString += ", steht auf Server " + serverId;
            }
        }
        return entityString;
    }
    
    public static String getLocationInfo(Location location) {
        return location == null ? getLocationInfo(null, 0, 0, 0)
                : getLocationInfo(location.getWorld().getName(), location.getX(), location.getY(),
                        location.getZ());
    }
    
    public static String getLocationInfo(String world, double x, double y, double z) {
        if (world == null) {
            return ChatColor.RED + "NULL";
        } else {
            return ChatColor.DARK_AQUA + "Welt: " + ChatColor.GREEN + world + ChatColor.DARK_AQUA
                    + " x: " + ChatColor.GREEN + x + ChatColor.DARK_AQUA + " y: " + ChatColor.GREEN
                    + y + ChatColor.DARK_AQUA + " z: " + ChatColor.GREEN + z;
        }
    }
    
    public static String getToleranceInfo(double tolarance) {
        return ChatColor.DARK_AQUA + "Toleranz: "
                + (tolarance >= 0 ? ChatColor.GREEN : ChatColor.RED) + tolarance;
    }
    
    public static Location roundLocation(Location loc, int digits) {
        World world = loc.getWorld();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        
        double factor = Math.pow(10, digits);
        x = Math.round(x * factor) / factor;
        y = Math.round(y * factor) / factor;
        z = Math.round(z * factor) / factor;
        yaw = (float) (Math.round(yaw * factor) / factor);
        pitch = (float) (Math.round(pitch * factor) / factor);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public static BaseComponent[] headline1(String content) {
        return new ComponentBuilder("--- " + content + " ---").color(ChatColor.DARK_GREEN)
                .underlined(true).create();
    }
    
    public static BaseComponent[] headline2(String content) {
        return new ComponentBuilder(content).color(ChatColor.DARK_AQUA).bold(true).create();
    }
    
    public static String getInteractorInfoString(Interactor interactor) {
        String result = "";
        if (interactor == null) {
            result += ChatColor.RED + "NULL";
        } else {
            result +=
                    (interactor.isLegal() ? ChatColor.RED : ChatColor.GREEN) + interactor.getInfo();
        }
        return result;
    }
    
    public static List<String> polishTabCompleteList(List<String> raw, String lastTypedArg) {
        String arg = lastTypedArg.toLowerCase(Locale.ENGLISH);
        raw.removeIf(s -> !s.toLowerCase(Locale.ENGLISH).contains(arg));
        raw.sort((s1, s2) -> {
            int res = 0;
            if (s1.toLowerCase().startsWith(arg)) {
                res++;
            }
            if (s2.toLowerCase().startsWith(arg)) {
                res--;
            }
            if (res != 0) {
                return res;
            }
            
            return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
        });
        
        return raw;
    }
    
    public static String exceptionToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
    public static String repeat(String arg, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(arg);
        }
        return builder.toString();
    }
    
    public static String multipleBlockString(Collection<Material> types) {
        String result = "";
        
        for (Material material: types) {
            result += ItemStackUtil.toNiceString(material) + "-";
            result += ", ";
        }
        
        result = ChatAndTextUtil.replaceLast(result, "-", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");
        
        result += "blöcke";
        
        return result;
    }
    
    public static String multiplieFishablesString(Collection<Material> types) {
        String result = "";
        
        for (Material material: types) {
            result += ItemStackUtil.toNiceString(material);
            result += (result.endsWith("ish") ? "es" : "s") + ", ";
        }
        
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");
        
        return result;
    }
    
    public static String multipleMobsString(Collection<EntityType> types) {
        String result = "";
        
        for (EntityType type: types) {
            result += ChatAndTextUtil.capitalize(type.name(), true) + "-";
            result += ", ";
        }
        
        result = ChatAndTextUtil.replaceLast(result, "-", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");
        
        result += "mobs";
        return result;
    }
    
    public static String convertColors(String text) {
        if (text == null) {
            return null;
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current != '&') {
                builder.append(current);
                continue;
            }
            
            if ((i + 1) >= text.length()) {
                builder.append(current);
                continue;
            }
            
            char next = text.charAt(i + 1);
            if (next == '&') {
                builder.append(current);
                i++;
                continue;
            }
            
            if (isColorChar(next)) {
                builder.append(ChatColor.COLOR_CHAR);
                continue;
            }
            
            builder.append(current);
        }
        
        return builder.toString();
    }
    
    private static boolean isColorChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'k' && c <= 'o')
                || (c >= 'A' && c <= 'F') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }
    
    public static String toNiceString(Color color) {
        if (constantColors.containsKey(color)) {
            return constantColors.get(color);
        }
        
        double lowestDiff = Double.MAX_VALUE;
        String bestMatch = null;
        
        for (Color other: constantColors.keySet()) {
            double diff = diff(color, other);
            if (diff < lowestDiff) {
                lowestDiff = diff;
                bestMatch = constantColors.get(other);
            }
        }
        
        String hexString = Integer.toHexString(color.asRGB()).toUpperCase();
        int zerosMissing = 6 - hexString.length();
        
        StringBuilder builder = new StringBuilder("roughly ");
        builder.append(bestMatch).append(" (#");
        for (int i = 0; i < zerosMissing; i++) {
            builder.append('0');
        }
        builder.append(hexString).append(")");
        
        return builder.toString();
    }
    
    private static double diff(Color c1, Color c2) {
        return Math.sqrt(
                Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2)
                        + Math.pow(c1.getGreen() - c2.getGreen(), 2));
    }
    
    public static String getName(Enchantment enchantment) {
        if (enchantment == null) {
            return null;
        }
        String name = enchantmentToName.get(enchantment);
        if (name != null) {
            return name;
        }
        return capitalize(enchantment.getName(), true);
    }
    
}
