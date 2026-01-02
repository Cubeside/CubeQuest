package de.iani.cubequest.util;

import static net.kyori.adventure.text.Component.text;

import com.google.common.collect.Iterables;
import de.cubeside.connection.util.GlobalLocation;
import de.cubeside.npcs.data.SpawnedNPCData;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestType;
import de.iani.cubesideutils.FontUtil;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.ChatUtilBukkit;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import de.iani.cubesideutils.commands.ArgsParser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

public class ChatAndTextUtil {

    public static final int PAGE_LENGTH = 10;
    public static final int MAX_BOOK_LENGTH = 50;

    public static BaseComponent[] DOUBLE_NEW_LINE = new ComponentBuilder("\n\n").create();

    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String TIME_FORMAT_STRING = "HH:mm";
    public static final String TIME_SECONDS_FORMAT_STRING = "HH:mm:ss";
    public static final String DATE_AND_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm";
    public static final String DATE_AND_TIME_SECONDS_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
    private static final DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
    private static final DateFormat timeSecondsFormat = new SimpleDateFormat(TIME_SECONDS_FORMAT_STRING);

    private static final Pattern COLOR_CODES_PATTERN =
            Pattern.compile("\\Q" + ChatColor.COLOR_CHAR + "\\E([0-9]|[a-f]|[A-F]|[k-o]|[K-O]|r|R)");

    private static final TreeMap<Integer, String> romanNumberMap;

    private static final Map<Color, String> constantColors;

    private static final Map<Enchantment, String> enchantmentToName;

    private static final Predicate<Object> acceptEverything = o -> true;

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

        for (DyeColor dc : DyeColor.values()) {
            constantColors.put(dc.getColor(), dc.name().replaceAll(Pattern.quote("_"), " ").toLowerCase());
        }

        enchantmentToName = new HashMap<>();

        enchantmentToName.put(Enchantment.POWER, "Power");
        enchantmentToName.put(Enchantment.FLAME, "Flame");
        enchantmentToName.put(Enchantment.INFINITY, "Infinity");
        enchantmentToName.put(Enchantment.PUNCH, "Punch");
        enchantmentToName.put(Enchantment.BINDING_CURSE, ChatColor.RED + "Curse of Binding");
        enchantmentToName.put(Enchantment.SHARPNESS, "Sharpness");
        enchantmentToName.put(Enchantment.BANE_OF_ARTHROPODS, "Bane of Anthropods");
        enchantmentToName.put(Enchantment.SMITE, "Smite");
        enchantmentToName.put(Enchantment.EFFICIENCY, "Efficiency");
        enchantmentToName.put(Enchantment.UNBREAKING, "Unbreaking");
        enchantmentToName.put(Enchantment.FORTUNE, "Fortune");
        enchantmentToName.put(Enchantment.LOOTING, "Looting");
        enchantmentToName.put(Enchantment.LUCK_OF_THE_SEA, "Luck of the Sea");
        enchantmentToName.put(Enchantment.RESPIRATION, "Respiration");
        enchantmentToName.put(Enchantment.PROTECTION, "Protection");
        enchantmentToName.put(Enchantment.BLAST_PROTECTION, "Blast Protection");
        enchantmentToName.put(Enchantment.FEATHER_FALLING, "Feather Falling");
        enchantmentToName.put(Enchantment.FIRE_PROTECTION, "Fire Protection");
        enchantmentToName.put(Enchantment.PROJECTILE_PROTECTION, "Projectile Protection");
        enchantmentToName.put(Enchantment.VANISHING_CURSE, ChatColor.RED + "Curse of Vanishing");
        enchantmentToName.put(Enchantment.AQUA_AFFINITY, "Aqua Affinity");
    }

    public static interface Sendable {

        public void send(CommandSender receiver);
    }

    public static class StringMsg implements Sendable {

        public final String msg;

        public StringMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public void send(CommandSender recipient) {
            recipient.sendMessage(this.msg);
        }
    }

    public static class StringQuestMsg extends StringMsg {

        public StringQuestMsg(String msg) {
            super(msg);
        }

        @Override
        public void send(CommandSender recipient) {
            ChatAndTextUtil.sendMessage(recipient, this.msg);
        }
    }

    public static class ComponentMsg implements Sendable {

        public final BaseComponent[] msg;

        public ComponentMsg(BaseComponent[] msg) {
            this.msg = msg;
        }

        @Override
        public void send(CommandSender recipient) {
            ChatAndTextUtil.sendBaseComponent(recipient, this.msg);
        }
    }

    public static void sendNormalMessage(CommandSender recipient, Object... msg) {
        ChatUtilBukkit.sendMessage(recipient, CubeQuest.PLUGIN_TAG, Style.style(NamedTextColor.GREEN), msg);
    }

    public static void sendWarningMessage(CommandSender recipient, Object... msg) {
        ChatUtilBukkit.sendMessage(recipient, CubeQuest.PLUGIN_TAG, Style.style(NamedTextColor.GOLD), msg);
    }

    public static void sendErrorMessage(CommandSender recipient, Object... msg) {
        ChatUtilBukkit.sendMessage(recipient, CubeQuest.PLUGIN_TAG, Style.style(NamedTextColor.RED), msg);
    }

    public static void sendMessage(CommandSender recipient, Object... msg) {
        ChatUtilBukkit.sendMessage(recipient, CubeQuest.PLUGIN_TAG, null, msg);
    }

    public static void sendNoPermissionMessage(CommandSender recipient) {
        sendErrorMessage(recipient, "Dazu fehlt dir die Berechtigung!");
    }

    public static void sendNotEditingQuestMessage(CommandSender recipient) {
        sendWarningMessage(recipient, "Du bearbeitest derzeit keine Quest!");
    }

    public static void sendXpAndQuestPointsMessage(CommandSender recipient, int xp, int questPoints) {
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

    public static String formatTimespan(long ms, String d, String h, String m, String s, String delimiter,
            String lastDelimiter) {
        return formatTimespan(ms, d, h, m, s, delimiter, lastDelimiter, true);
    }

    public static String formatTimespan(long ms, String d, String h, String m, String s, String delimiter,
            String lastDelimiter, boolean dropAllLowerIfZero) {
        return formatTimespan(ms, d, h, m, s, delimiter, lastDelimiter, dropAllLowerIfZero, false);
    }

    public static String formatTimespan(long ms, String d, String h, String m, String s, String delimiter,
            String lastDelimiter, boolean dropAllLowerIfZero, boolean forceMinutesAndTwoDigitsForTime) {
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
                lessThanSecondsString = lessThanSecondsString.substring(lessThanSecondsString.indexOf('.') + 1);
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

        result += " " + (second == 0 ? timeFormat.format(date) : timeSecondsFormat.format(date)) + " Uhr";
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

    public static Quest getQuest(CommandSender sender, ArgsParser args, String commandOnSelectionByClickingPreId,
            String commandOnSelectionByClickingPostId, String hoverTextPreId, String hoverTextPostId) {
        return getQuest(sender, args, acceptEverything, false, commandOnSelectionByClickingPreId,
                commandOnSelectionByClickingPostId, hoverTextPreId, hoverTextPostId);
    }

    public static Quest getQuest(CommandSender sender, ArgsParser args, Predicate<? super Quest> questFilter,
            boolean considerNonVisibleInErrorMessage, String commandOnSelectionByClickingPreId,
            String commandOnSelectionByClickingPostId, String hoverTextPreId, String hoverTextPostId) {

        if (!commandOnSelectionByClickingPreId.startsWith("/")) {
            commandOnSelectionByClickingPreId = "/" + commandOnSelectionByClickingPreId;
        }

        String idString = args.getNext("");
        try {
            int id = Integer.parseInt(idString);
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null || !questFilter.test(quest)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id
                        + (considerNonVisibleInErrorMessage ? ", die für dich sichtbar ist" : "") + ".");
                return null;
            }
            return quest;
        } catch (NumberFormatException e) {
            String questString = args.hasNext() ? idString + " " + args.getAll("") : idString;
            List<Quest> quests = QuestManager.getInstance().getQuests(questString).stream().filter(questFilter)
                    .collect(Collectors.toList());
            if (quests.isEmpty()) {
                quests = QuestManager.getInstance().searchQuests(questString).stream().filter(questFilter)
                        .collect(Collectors.toList());
            }

            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen \"" + questString + "\""
                        + (considerNonVisibleInErrorMessage ? ", die für dich sichtbar ist" : "") + ".");
                return null;
            } else if (quests.size() > 1) {
                quests.sort(Quest.QUEST_LIST_COMPARATOR);
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q : quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text(hoverTextPreId + q.getId() + hoverTextPostId));
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                commandOnSelectionByClickingPreId + q.getId() + commandOnSelectionByClickingPostId);
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName() + " " + q.getId()
                                + (q.getInternalName().isEmpty() ? ""
                                        : (" (" + q.getInternalName() + ChatColor.RESET + ChatColor.GOLD + ")"));
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return null;
            }
            Bukkit.dispatchCommand(sender, commandOnSelectionByClickingPreId.substring(1)
                    + Iterables.getFirst(quests, null).getId() + commandOnSelectionByClickingPostId);
            return null;
        }
    }

    public static Location getLocation(CommandSender sender, ArgsParser args, boolean noPitchOrYaw,
            boolean roundToBlock) {
        SafeLocation result = getSafeLocation(sender, args, noPitchOrYaw, roundToBlock);
        return result == null ? null : result.getLocation();
    }

    public static SafeLocation getSafeLocation(CommandSender sender, ArgsParser args, boolean noPitchOrYaw,
            boolean roundToBlock) {
        SafeLocation result = null;

        String world;
        int serverId;
        if (args.remaining() < 4) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.remaining() == 3) {
                    world = player.getWorld().getName();
                    serverId = CubeQuest.getInstance().getServerId();
                } else if (args.hasNext()) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die x-, y- und z-Koordinate des Orts an.");
                    return null;
                } else {
                    result = new SafeLocation(player.getLocation());
                    world = result.getWorld();
                    serverId = result.getServerId();
                }
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Welt und die x-, y- und z-Koordinate des Orts an.");
                return null;
            }
        } else {
            if (args.remaining() < 5) {
                serverId = CubeQuest.getInstance().getServerId();
            } else {
                serverId = args.getNext(-1);
                if (serverId < 0) {
                    sendWarningMessage(sender, "Bitte gib die Server-ID des Orts als nicht-negative Ganzzahl an.");
                    return null;
                }
            }
            world = args.getNext();
            if (serverId == CubeQuest.getInstance().getServerId() && Bukkit.getWorld(world) == null) {
                sendWarningMessage(sender, "Welt " + world + " nicht gefunden.");
                return null;
            }
        }

        if (result == null) {
            double x, y, z;
            float pitch = 0.0f, yaw = 0.0f;
            try {
                x = Double.parseDouble(args.getNext());
                y = Double.parseDouble(args.getNext());
                z = Double.parseDouble(args.getNext());
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die x- y- und z-Koordinate des Orts als Kommazahlen (mit . statt ,) an.");
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
                            "Bitte gib pitch und yaw des Orts als Kommazahlen (mit . statt ,) an.");
                    return null;
                }
            }
            result = new SafeLocation(serverId, world, x, y, z, pitch, yaw);
        }

        if (roundToBlock) {
            result = new SafeLocation(result.getServerId(), result.getWorld(), result.getBlockX(), result.getBlockY(),
                    result.getBlockZ(), 0.0f, 0.0f);
        } else if (noPitchOrYaw) {
            result = new SafeLocation(result.getServerId(), result.getWorld(), result.getX(), result.getY(),
                    result.getZ(), 0.0f, 0.0f);
        }

        return result;
    }

    public static void sendComponents(CommandSender sender, Component... components) {
        for (Component c : components) {
            sender.sendMessage(c);
        }
    }

    public static void sendComponents(CommandSender sender, List<Component> components) {
        for (Component c : components) {
            sender.sendMessage(c);
        }
    }

    public static String replaceLast(String in, String sequence, String replacement) {
        int index = in.lastIndexOf(sequence);
        if (index < 0) {
            return in;
        }

        return in.substring(0, index) + replacement + in.substring(index + sequence.length(), in.length());
    }

    public static String getNPCInfoString(UUID npcId) {
        return getNPCInfoString(CubeQuest.getInstance().getServerId(), npcId);
    }

    public static String getNPCInfoString(int serverId, UUID npcId) {
        boolean forThisServer = serverId == CubeQuest.getInstance().getServerId();
        String npcString = "";
        if (npcId == null) {
            npcString += ChatColor.RED + "NULL";
        } else {
            npcString += ChatColor.GREEN + "Id: " + npcId;
            if (forThisServer && CubeQuest.getInstance().hasCubesideNPCsPlugin()) {
                npcString += internalNPCInfoString(npcId);
            } else {
                npcString += ", steht auf Server " + serverId;
            }
        }
        return npcString;
    }

    private static String internalNPCInfoString(UUID npcId) {
        String npcString = "";
        SpawnedNPCData npc = CubeQuest.getInstance().getNPCReg().getById(npcId);
        if (npc == null) {
            npcString += ", " + ChatColor.RED + "EXISTIERT NICHT";
        } else {
            Location loc = npc.getLastKnownLocation();
            npcString += ", \"" + npc.getNpcNameString() + "\"";
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
                    entityString += ", " + ChatColor.RED + "(nicht existent/geladen)";
                } else {
                    Location loc = roundLocation(entity.getLocation(), 1);
                    entityString += ", \"" + entity.getName() + ChatColor.GREEN + "\"";
                    if (loc != null) {
                        entityString += " in Welt " + loc.getWorld().getName() + " bei x: " + loc.getX() + ", y: "
                                + loc.getY() + ", z: " + loc.getZ();
                    }
                }
            } else {
                entityString += ", steht auf Server " + serverId;
            }
        }
        return entityString;
    }

    public static String getLocationInfo(GlobalLocation location) {
        return getLocationInfo(location, null);
    }

    public static String getLocationInfo(GlobalLocation location, Double tolerance) {
        return location == null ? getLocationInfo(null, 0, 0, 0)
                : getLocationInfo(location.getServer(), location.getWorld(), location.getX(), location.getY(),
                        location.getZ(), tolerance);
    }

    public static String getLocationInfo(Location location) {
        return getLocationInfo(location, null);
    }

    public static String getLocationInfo(Location location, Double tolerance) {
        return getLocationInfo(location == null ? null : new SafeLocation(location), tolerance);
    }

    public static String getLocationInfo(SafeLocation location) {
        return getLocationInfo(location, null);
    }

    public static String getLocationInfo(SafeLocation location, Double tolerance) {
        return location == null ? getLocationInfo(null, 0, 0, 0)
                : getLocationInfo(location.getServerId(), location.getWorld(), location.getX(), location.getY(),
                        location.getZ(), tolerance);
    }

    public static String getLocationInfo(String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static String getLocationInfo(String world, double x, double y, double z, Double tolerance) {
        return getLocationInfo(CubeQuest.getInstance().getServerId(), world, x, y, z, tolerance);
    }

    public static String getLocationInfo(int serverId, String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static String getLocationInfo(int serverId, String world, double x, double y, double z, Double tolerance) {
        return getLocationInfo(String.valueOf(serverId), world, x, y, z, tolerance);
    }

    public static String getLocationInfo(String serverId, String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static String getLocationInfo(String serverId, String world, double x, double y, double z,
            Double tolerance) {
        if (world == null) {
            return ChatColor.RED + "NULL";
        } else {
            String result = ChatColor.DARK_AQUA + "ServerId: " + ChatColor.GREEN + serverId + ChatColor.DARK_AQUA
                    + " Welt: " + ChatColor.GREEN + world + ChatColor.DARK_AQUA + " x: " + ChatColor.GREEN + x
                    + ChatColor.DARK_AQUA + " y: " + ChatColor.GREEN + y + ChatColor.DARK_AQUA + " z: "
                    + ChatColor.GREEN + z;
            if (tolerance != null) {
                result += ChatColor.DARK_AQUA + " ±" + ChatColor.GREEN + tolerance;
            }
            return result;
        }
    }

    public static String getToleranceInfo(double tolarance) {
        return ChatColor.DARK_AQUA + "Toleranz: " + (tolarance >= 0 ? ChatColor.GREEN : ChatColor.RED) + tolarance;
    }

    public static SafeLocation roundLocation(SafeLocation loc, int digits) {
        int serverId = loc.getServerId();
        String world = loc.getWorld();
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

        return new SafeLocation(serverId, world, x, y, z, yaw, pitch);
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
        return new ComponentBuilder("--- " + content + " ---").color(ChatColor.DARK_GREEN).underlined(true).create();
    }

    public static BaseComponent[] headline2(String content) {
        return new ComponentBuilder(content).color(ChatColor.DARK_AQUA).bold(true).create();
    }

    public static Component getInteractorInfo(Interactor interactor) {
        if (interactor == null) {
            return Component.text("NULL").color(NamedTextColor.RED);
        } else {
            return interactor.getInfo().color(interactor.isLegal() ? NamedTextColor.GREEN : NamedTextColor.RED);
        }
    }

    public static List<String> polishTabCompleteList(Collection<String> raw, String lastTypedArg) {
        if (raw == null) {
            return null;
        }

        List<String> list = raw instanceof List<?> ? (List<String>) raw : new ArrayList<>(raw);
        String arg = lastTypedArg.toLowerCase(Locale.ENGLISH);

        try {
            return polishTabCompleteListInternal(list, arg);
        } catch (UnsupportedOperationException e) {
            return polishTabCompleteListInternal(new ArrayList<>(list), arg);
        }
    }

    private static List<String> polishTabCompleteListInternal(List<String> list, String arg) {
        list.removeIf(s -> !s.toLowerCase(Locale.ENGLISH).contains(arg));
        list.sort((s1, s2) -> {
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

        return list;
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

    public static Component repeat(Component arg, int times) {
        Component result = Component.empty();
        for (int i = 0; i < times; i++) {
            result = result.append(arg);
        }
        return result;
    }

    public static String multipleMaterialsString(Collection<Material> types) {
        return multipleMaterialsString(types, true);
    }

    public static String multipleMaterialsString(Collection<Material> types, boolean tryPlurals) {
        String result = "";

        for (Material material : types) {
            result += tryPlurals ? StringUtil.tryPlural(ItemsAndStrings.toNiceString(material))
                    : ItemsAndStrings.toNiceString(material);
            result += ", ";
        }

        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");

        return result;
    }

    public static String multipleEntityTypesString(Collection<EntityType> types) {
        return multipleEntityTypesString(types, true);
    }

    public static String multipleEntityTypesString(Collection<EntityType> types, boolean tryPlurals) {
        String result = "";

        for (EntityType type : types) {
            result += tryPlurals ? StringUtil.tryPlural(ChatAndTextUtil.capitalize(type.name(), true))
                    : ChatAndTextUtil.capitalize(type.name(), true);
            result += ", ";
        }

        result = ChatAndTextUtil.replaceLast(result, ", ", "");
        result = ChatAndTextUtil.replaceLast(result, ", ", " und/oder ");

        return result;
    }

    public static String toNiceString(Color color) {
        if (constantColors.containsKey(color)) {
            return constantColors.get(color);
        }

        double lowestDiff = Double.MAX_VALUE;
        String bestMatch = null;

        for (Color other : constantColors.keySet()) {
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
        return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2)
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
        return capitalize(enchantment.getKey().getKey(), true);
    }

    public static List<Sendable> stringToSendableList(List<String> msges) {
        ArrayList<Sendable> result = new ArrayList<>(msges.size());
        for (String msg : msges) {
            result.add(new StringMsg(msg));
        }
        return result;
    }

    public static List<Sendable> bcToSendableList(List<BaseComponent[]> msges) {
        ArrayList<Sendable> result = new ArrayList<>(msges.size());
        for (BaseComponent[] msg : msges) {
            result.add(new ComponentMsg(msg));
        }
        return result;
    }

    public static void sendMessagesPaged(CommandSender receiver, List<? extends Sendable> messages, int page,
            String name, String openPageCommandPrefix) {
        if (!openPageCommandPrefix.startsWith("/")) {
            openPageCommandPrefix = "/" + openPageCommandPrefix;
        }

        int numPages = (int) Math.ceil(messages.size() / (double) PAGE_LENGTH);

        if (page >= numPages) {
            sendWarningMessage(receiver, name + " hat keine Seite " + (page + 1));
            return;
        }

        if (numPages > 1) {
            sendNormalMessage(receiver, name + " (Seite " + (page + 1) + "/" + numPages + "):");
        } else {
            sendNormalMessage(receiver, name + ":");
        }

        int index = page * PAGE_LENGTH;
        for (int i = 0; i < PAGE_LENGTH && index < messages.size();) {
            messages.get(index).send(receiver);

            i++;
            index++;
        }

        if (numPages > 1) {
            sendNormalMessage(receiver, "Seite x anzeigen: " + openPageCommandPrefix + " x");
            ComponentBuilder builder = new ComponentBuilder("<< vorherige");
            if (page > 0) {
                builder.color(ChatColor.BLUE);

                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Seite " + page + " anzeigen"));
                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, openPageCommandPrefix + " " + page);

                builder.event(he).event(ce);
            } else {
                builder.color(ChatColor.GRAY);

                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Bereits auf Seite 1"));

                builder.event(he);
            }

            builder.append("   ").reset().append("nächste >>");

            if (page + 1 < numPages) {
                builder.color(ChatColor.BLUE);

                HoverEvent he =
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Seite " + (page + 2) + " anzeigen"));
                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, openPageCommandPrefix + " " + (page + 2));

                builder.event(he).event(ce);
            } else {
                builder.color(ChatColor.GRAY);

                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Bereits auf Seite " + numPages));

                builder.event(he);
            }

            sendBaseComponent(receiver, builder.create());
        }
    }

    public static Component getStateStringStartingToken(QuestState state) {
        return getStateStringStartingToken(state == null ? Status.NOTGIVENTO : state.getStatus());
    }

    public static Component getStateStringStartingToken(Status status) {
        return switch (status) {
            case SUCCESS -> text("✔", NamedTextColor.GREEN);
            case FAIL -> text("✕", NamedTextColor.RED);
            case GIVENTO -> text("➽", NamedTextColor.AQUA);
            case NOTGIVENTO -> text("➽", NamedTextColor.DARK_AQUA);
            case FROZEN -> text("✕", NamedTextColor.DARK_GRAY);
            default -> throw new NullPointerException();
        };
    }

    public static Component getTrueFalseToken(Boolean value) {
        if (value == null) {
            return text("✕", NamedTextColor.DARK_GRAY);
        }
        return value ? text("✔", NamedTextColor.GREEN) : text("✕", NamedTextColor.RED);
    }

    public static List<BaseComponent> consolidateComponents(List<BaseComponent> components) {
        List<BaseComponent> result = new ArrayList<>();
        BaseComponent current = null;

        for (BaseComponent bc : components) {
            if (current == null) {
                current = bc;
                continue;
            }

            if (!(current instanceof TextComponent && bc instanceof TextComponent)
                    || !similarComponents((TextComponent) current, (TextComponent) bc)) {
                result.add(current);
                current = bc;
                continue;
            }

            TextComponent copy = new TextComponent((TextComponent) current);
            copy.setText((copy).getText() + ((TextComponent) bc).getText());
            current = copy;
        }

        if (current != null) {
            result.add(current);
        }

        return result;
    }

    public static boolean similarComponents(TextComponent b1, TextComponent b2) {
        if (b1.getExtra() != null || b2.getExtra() != null) {
            return false;
        }

        if (b1.getClickEvent() != b2.getClickEvent()) {
            return false;
        }

        if (b1.getColor() != b2.getColor()) {
            return false;
        }

        if (b1.isBold() != b2.isBold()) {
            return false;
        }

        if (b1.isItalic() != b2.isItalic()) {
            return false;
        }

        if (b1.isObfuscated() != b2.isObfuscated()) {
            return false;
        }

        if (b1.isStrikethrough() != b2.isStrikethrough()) {
            return false;
        }

        if (b1.isUnderlined() != b2.isUnderlined()) {
            return false;
        }

        return true;
    }

    public static boolean writeIntoBook(BookMeta into, List<BaseComponent[]> text) {
        return writeIntoBook(into, text, MAX_BOOK_LENGTH);
    }

    public static boolean writeIntoBook(BookMeta into, List<BaseComponent[]> text, int maxNumOfPages) {
        List<BaseComponent[]> pages = new ArrayList<>();

        int done = 0;
        while (done < text.size()) {
            List<BaseComponent> currentPage = new ArrayList<>();

            int minToFit = 1;
            int maxToFit = text.size();

            while (minToFit < maxToFit) {
                int toTry = (maxToFit + minToFit + 1) / 2;
                Iterator<BaseComponent[]> it = text.listIterator(done);
                for (int i = 0; i < toTry; i++) {
                    if (!it.hasNext()) {
                        break;
                    }
                    BaseComponent[] bcs = it.next();

                    if (bcs == null) {
                        if (i != 0 && i != toTry - 1) {
                            Util.addAll(currentPage, DOUBLE_NEW_LINE);
                        }
                    } else {
                        Util.addAll(currentPage, bcs);
                    }
                }

                if (FontUtil.fitsSingleBookPage(currentPage.toArray(new BaseComponent[currentPage.size()]))) {
                    minToFit = toTry;
                } else {
                    maxToFit = toTry - 1;
                }

                currentPage.clear();
            }

            assert minToFit >= 1;

            Iterator<BaseComponent[]> it = text.listIterator(done);
            for (int i = 0; i < minToFit; i++) {
                if (!it.hasNext()) {
                    break;
                }
                BaseComponent[] bcs = it.next();

                if (bcs == null) {
                    if (i != 0 && i != minToFit - 1) {
                        Util.addAll(currentPage, DOUBLE_NEW_LINE);
                    }
                } else {
                    for (int j = 0; j < bcs.length; j++) {
                        if (bcs[j].getColor() == ChatColor.RESET || bcs[j].getColor() == ChatColor.WHITE) {
                            BaseComponent newBc = bcs[j].duplicate();
                            newBc.setColor(ChatColor.BLACK);
                            bcs[j] = newBc;
                        }
                    }
                    Util.addAll(currentPage, bcs);
                }
            }

            currentPage = consolidateComponents(currentPage);
            pages.add(currentPage.toArray(new BaseComponent[currentPage.size()]));
            done += minToFit;
        }

        if (into.getPageCount() + pages.size() > maxNumOfPages) {
            return false;
        }

        for (BaseComponent[] page : pages) {
            into.spigot().addPage(page);
        }
        return true;
    }

    public static List<BaseComponent[]> getQuestDescription(Quest quest) {
        return getQuestDescription(quest, false, null);
    }

    public static List<BaseComponent[]> getQuestDescription(Quest quest, boolean teaser, Player forPlayer) {
        List<BaseComponent[]> result = new ArrayList<>();

        ComponentBuilder builder = new ComponentBuilder("");
        builder.bold(true).append(TextComponent.fromLegacyText(quest.getDisplayName()));
        result.add(builder.create());
        result.add(null);

        if (teaser) {
            PlayerData data = CubeQuest.getInstance().getPlayerData(forPlayer);
            List<QuestCondition> conds = quest.getVisibleGivingConditions();
            builder =
                    new ComponentBuilder("Vergabebedingung" + (conds.size() == 1 ? "" : "en") + ":\n").underlined(true);
            result.add(builder.create());
            result.add(null);
            for (QuestCondition cond : conds) {
                result.add(new ComponentBuilder("")
                        .append(ChatAndTextUtil
                                .getTrueFalseToken(forPlayer == null ? null : cond.fulfills(forPlayer, data)))
                        .append(" ").append(stripEvents(cond.getConditionInfo())).append("\n").create());
            }
        } else if (quest.getDisplayMessage() != null) {
            String[] words = quest.getDisplayMessage().split(Pattern.quote(" "));
            String lastFormat = "";
            for (int i = 0; i < words.length; i++) {
                String word = lastFormat + ((i < words.length - 1) ? words[i] + " " : words[i]);
                result.add(TextComponent.fromLegacyText(word));
                lastFormat = org.bukkit.ChatColor.getLastColors(word);
            }
            result.add(null);
        }

        return result;
    }

    public static String stripColors(String input) {
        return ChatAndTextUtil.COLOR_CODES_PATTERN.matcher(input).replaceAll("");
    }

    public static BaseComponent[] stripEvents(BaseComponent[] bcs) {
        BaseComponent[] result = bcs.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = stripEvents(result[i]);
        }
        return result;
    }

    public static BaseComponent stripEvents(BaseComponent bc) {
        if (bc.getClickEvent() == null && bc.getHoverEvent() == null) {
            return bc;
        }
        BaseComponent result = bc.duplicate();
        result.setClickEvent(null);
        result.setHoverEvent(null);
        return result;
    }

    public static Component stripEvents(Component component) {
        if (component == null) {
            return null;
        }

        Component stripped = component.clickEvent((ClickEvent) null).hoverEvent((HoverEvent<?>) null);

        if (stripped.children().isEmpty()) {
            return stripped;
        }

        return stripped.children(stripped.children().stream().map(ChatAndTextUtil::stripEvents).toList());
    }

    public static OfflinePlayer parseOfflinePlayer(String playerString) {
        try {
            UUID playerId = UUID.fromString(playerString);
            return CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerId);
        } catch (IllegalArgumentException e) {
            return CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerString);
        }
    }

    public static String getPlayerString(OfflinePlayer player) {
        return getPlayerString(player.getUniqueId());
    }

    public static String getPlayerString(UUID playerId) {
        OfflinePlayer player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerId);
        if (player == null || player.getName() == null) {
            return playerId.toString();
        }
        return player.getName();
    }

}
