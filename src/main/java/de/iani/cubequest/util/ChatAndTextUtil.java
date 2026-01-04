package de.iani.cubequest.util;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.Component.translatable;

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
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.FontUtilAdventure;
import de.iani.cubesideutils.bukkit.ChatUtilBukkit;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.serialization.SerializableAdventureComponent;
import de.iani.cubesideutils.commands.ArgsParser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

public class ChatAndTextUtil {

    public static final int PAGE_LENGTH = 10;
    public static final int MAX_BOOK_LENGTH = 50;

    public static Component DOUBLE_NEW_LINE = text("\n\n");

    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String TIME_FORMAT_STRING = "HH:mm";
    public static final String TIME_SECONDS_FORMAT_STRING = "HH:mm:ss";
    public static final String DATE_AND_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm";
    public static final String DATE_AND_TIME_SECONDS_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
    private static final DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
    private static final DateFormat timeSecondsFormat = new SimpleDateFormat(TIME_SECONDS_FORMAT_STRING);

    private static final TreeMap<Integer, String> romanNumberMap;

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

    @Deprecated
    public static void sendMessage(CommandSender recipient, List<Component> msg) {
        msg.forEach(c -> sendMessage(recipient, c));
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

    public static void sendMessagesPaged(CommandSender sender, List<Component> list, int i, String name,
            String openPageCommandPrefix) {
        ChatUtilBukkit.sendMessagesPaged(sender, ChatUtilBukkit.componentToBukkitSendableList(list), i, text(name),
                openPageCommandPrefix, CubeQuest.PLUGIN_TAG);
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
                    if (sender instanceof Player player) {
                        Component hover = text(hoverTextPreId + q.getId() + hoverTextPostId);

                        ClickEvent click = ClickEvent.runCommand(
                                commandOnSelectionByClickingPreId + q.getId() + commandOnSelectionByClickingPostId);

                        Component msg = CubeQuest.PLUGIN_TAG.append(space())
                                .append(text(q.getTypeName() + " " + q.getId(), NamedTextColor.GOLD));

                        if (!q.getInternalName().isEmpty()) {
                            msg = msg.append(text(" (" + q.getInternalName() + ")", NamedTextColor.GOLD));
                        }

                        player.sendMessage(msg.hoverEvent(HoverEvent.showText(hover)).clickEvent(click));
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

    public static Component getNPCInfoComponent(UUID npcId) {
        return getNPCInfoComponent(CubeQuest.getInstance().getServerId(), npcId);
    }

    public static Component getNPCInfoComponent(int serverId, UUID npcId) {
        boolean forThisServer = serverId == CubeQuest.getInstance().getServerId();

        if (npcId == null) {
            return text("NULL", NamedTextColor.RED);
        }

        Component c = text("Id: ").append(text(npcId.toString()));

        if (forThisServer && CubeQuest.getInstance().hasCubesideNPCsPlugin()) {
            c = c.append(internalNPCInfoComponent(npcId));
        } else {
            c = c.append(text(", steht auf Server " + serverId));
        }

        return c.color(NamedTextColor.GREEN);
    }

    private static Component internalNPCInfoComponent(UUID npcId) {
        SpawnedNPCData npc = CubeQuest.getInstance().getNPCReg().getById(npcId);

        if (npc == null) {
            return text(", EXISTIERT NICHT", NamedTextColor.RED);
        }

        Component c = text(", \"").append(npc.getNameAsComponent()).append(text("\""));

        Location loc = npc.getLastKnownLocation();
        if (loc != null) {
            loc = roundLocation(loc, 1);
            c = c.append(text(" bei x: " + loc.getX() + ", y: " + loc.getY() + ", z: " + loc.getZ()));
        }

        return c.color(NamedTextColor.GREEN);
    }

    public static Component getEntityInfoComponent(UUID entityId) {
        return getEntityInfoComponent(CubeQuest.getInstance().getServerId(), entityId);
    }

    public static Component getEntityInfoComponent(int serverId, UUID entityId) {
        boolean forThisServer = serverId == CubeQuest.getInstance().getServerId();

        if (entityId == null) {
            return text("NULL", NamedTextColor.RED);
        }

        Component c = text("Id: ").append(text(entityId.toString()));

        if (forThisServer) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity == null) {
                c = c.append(textOfChildren(text(", "), text("(nicht existent/geladen)", NamedTextColor.RED)));
            } else {
                Location loc = roundLocation(entity.getLocation(), 1);

                c = c.append(text(", \"")).append(text(entity.getName())).append(text("\""));

                if (loc != null && loc.getWorld() != null) {
                    c = c.append(text(" in Welt ")).append(text(loc.getWorld().getName())).append(text(" bei x: "))
                            .append(text(String.valueOf(loc.getX()))).append(text(", y: "))
                            .append(text(String.valueOf(loc.getY()))).append(text(", z: "))
                            .append(text(String.valueOf(loc.getZ())));
                }
            }
        } else {
            c = c.append(text(", steht auf Server " + serverId));
        }

        return c.color(NamedTextColor.GREEN);
    }

    public static Component getLocationInfo(GlobalLocation location) {
        return getLocationInfo(location, null);
    }

    public static Component getLocationInfo(GlobalLocation location, Double tolerance) {
        return location == null ? getLocationInfo(null, 0, 0, 0)
                : getLocationInfo(location.getServer(), location.getWorld(), location.getX(), location.getY(),
                        location.getZ(), tolerance);
    }

    public static Component getLocationInfo(Location location) {
        return getLocationInfo(location, null);
    }

    public static Component getLocationInfo(Location location, Double tolerance) {
        return getLocationInfo(location == null ? null : new SafeLocation(location), tolerance);
    }

    public static Component getLocationInfo(SafeLocation location) {
        return getLocationInfo(location, null);
    }

    public static Component getLocationInfo(SafeLocation location, Double tolerance) {
        return location == null ? getLocationInfo(null, 0, 0, 0)
                : getLocationInfo(location.getServerId(), location.getWorld(), location.getX(), location.getY(),
                        location.getZ(), tolerance);
    }

    public static Component getLocationInfo(String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static Component getLocationInfo(String world, double x, double y, double z, Double tolerance) {
        return getLocationInfo(CubeQuest.getInstance().getServerId(), world, x, y, z, tolerance);
    }

    public static Component getLocationInfo(int serverId, String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static Component getLocationInfo(int serverId, String world, double x, double y, double z,
            Double tolerance) {
        return getLocationInfo(String.valueOf(serverId), world, x, y, z, tolerance);
    }

    public static Component getLocationInfo(String serverId, String world, double x, double y, double z) {
        return getLocationInfo(world, x, y, z, null);
    }

    public static Component getLocationInfo(String serverId, String world, double x, double y, double z,
            Double tolerance) {
        if (world == null) {
            return text("NULL", NamedTextColor.RED);
        }

        Component c = text("ServerId: ", NamedTextColor.DARK_AQUA)
                .append(text(String.valueOf(serverId), NamedTextColor.GREEN))
                .append(text(" Welt: ", NamedTextColor.DARK_AQUA)).append(text(world, NamedTextColor.GREEN))
                .append(text(" x: ", NamedTextColor.DARK_AQUA)).append(text(String.valueOf(x), NamedTextColor.GREEN))
                .append(text(" y: ", NamedTextColor.DARK_AQUA)).append(text(String.valueOf(y), NamedTextColor.GREEN))
                .append(text(" z: ", NamedTextColor.DARK_AQUA)).append(text(String.valueOf(z), NamedTextColor.GREEN));

        if (tolerance != null) {
            c = c.append(text(" ±", NamedTextColor.DARK_AQUA))
                    .append(text(String.valueOf(tolerance), NamedTextColor.GREEN));
        }

        return c;
    }

    public static Component getToleranceInfo(double tolarance) {
        return text("Toleranz: ", NamedTextColor.DARK_AQUA)
                .append(text(String.valueOf(tolarance), tolarance >= 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
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

    public static Component getInteractorInfo(Interactor interactor) {
        if (interactor == null) {
            return text("NULL").color(NamedTextColor.RED);
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
        Component result = empty();
        for (int i = 0; i < times; i++) {
            result = result.append(arg);
        }
        return result;
    }

    public static Component multipleMaterialsComponent(Collection<Material> types) {
        Component result = empty();

        int index = 0;
        for (Material material : types) {
            result = result.append(ItemStacks.toComponent(material));
            if (index + 2 < types.size()) {
                result = result.append(text(", "));
            } else if (index + 1 < types.size()) {
                result = result.append(text(" und/oder "));
            }
            index++;
        }

        return result;
    }

    public static Component multipleEntityTypesComponent(Collection<EntityType> types) {
        Component result = empty();

        int index = 0;
        for (EntityType type : types) {
            result = result.append(translatable(type));
            if (index + 2 < types.size()) {
                result = result.append(text(", "));
            } else if (index + 1 < types.size()) {
                result = result.append(text(" und/oder "));
            }
            index++;
        }

        return result;
    }

    public static Component getStateStringStartingToken(QuestState state) {
        return getStateStringStartingToken(state == null ? Status.NOTGIVENTO : state.getStatus());
    }

    public static Component getStateStringStartingToken(Status status) {
        return switch (status) {
            case SUCCESS -> text("✔", status.color);
            case FAIL -> text("✕", status.color);
            case GIVENTO -> text("➽", status.color);
            case NOTGIVENTO -> text("➽", status.color);
            case FROZEN -> text("✕", status.color);
            default -> throw new NullPointerException();
        };
    }

    public static Component getTrueFalseToken(Boolean value) {
        if (value == null) {
            return text("✕", NamedTextColor.DARK_GRAY);
        }
        return value ? text("✔", NamedTextColor.GREEN) : text("✕", NamedTextColor.RED);
    }

    public static boolean writeIntoBook(BookMeta into, List<Component> text) {
        return writeIntoBook(into, text, MAX_BOOK_LENGTH);
    }

    public static boolean writeIntoBook(BookMeta into, List<Component> text, int maxNumOfPages) {
        List<Component> pages = new ArrayList<>();

        int done = 0;
        while (done < text.size()) {
            List<Component> currentPage = new ArrayList<>();

            int minToFit = 1;
            int maxToFit = text.size();

            while (minToFit < maxToFit) {
                int toTry = (maxToFit + minToFit + 1) / 2;
                Iterator<Component> it = text.listIterator(done);
                for (int i = 0; i < toTry; i++) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Component c = it.next();

                    if (c == null) {
                        if (i != 0 && i != toTry - 1) {
                            currentPage.add(DOUBLE_NEW_LINE);
                        }
                    } else {
                        currentPage.add(c);
                    }
                }

                if (FontUtilAdventure.fitsSingleBookPage(currentPage)) {
                    minToFit = toTry;
                } else {
                    maxToFit = toTry - 1;
                }

                currentPage.clear();
            }

            assert minToFit >= 1;

            Iterator<Component> it = text.listIterator(done);
            for (int i = 0; i < minToFit; i++) {
                if (!it.hasNext()) {
                    break;
                }
                Component c = it.next();

                if (c == null) {
                    if (i != 0 && i != minToFit - 1) {
                        currentPage.add(DOUBLE_NEW_LINE);
                    }
                } else {
                    // if (c[j].getColor() == ChatColor.RESET || c[j].getColor() == ChatColor.WHITE) {
                    // BaseComponent newBc = c[j].duplicate();
                    // newBc.setColor(ChatColor.BLACK);
                    // c[j] = newBc;
                    // }
                    currentPage.add(c);
                }
            }

            for (int i = 0; i < currentPage.size(); i++) {
                currentPage.set(i, currentPage.get(i).compact());
            }
            pages.add(textOfChildren(currentPage.toArray(new Component[currentPage.size()])));
            done += minToFit;
        }

        if (into.getPageCount() + pages.size() > maxNumOfPages) {
            return false;
        }

        for (Component page : pages) {
            into.addPages(page);
        }
        return true;
    }

    public static List<Component> getQuestDescription(Quest quest) {
        return getQuestDescription(quest, false, null);
    }

    public static List<Component> getQuestDescription(Quest quest, boolean teaser, Player forPlayer) {
        List<Component> result = new ArrayList<>();

        result.add(quest.getDisplayName().decorate(TextDecoration.BOLD));
        result.add(null);

        if (teaser) {
            PlayerData data = (forPlayer == null) ? null : CubeQuest.getInstance().getPlayerData(forPlayer);
            List<QuestCondition> conds = quest.getVisibleGivingConditions();

            result.add(text("Vergabebedingung" + (conds.size() == 1 ? "" : "en") + ":\n")
                    .decorate(TextDecoration.UNDERLINED));
            result.add(null);

            for (QuestCondition cond : conds) {
                Boolean ok = (forPlayer == null) ? null : cond.fulfills(forPlayer, data);

                result.add(ChatAndTextUtil.getTrueFalseToken(ok).append(text(" "))
                        .append(ChatAndTextUtil.stripEvents(cond.getConditionInfo())).append(text("\n")));
            }
        } else if (quest.getDisplayMessage() != null) {
            result.addAll(ComponentUtilAdventure.splitBySpaces(quest.getDisplayMessage()));
            result.add(null);
        }

        return result;
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

    public static Component getComponentOrConvert(ConfigurationSection config, String path) {
        Object val = config.get(path);
        return getComponentOrConvert(val);
    }

    public static Component getComponentOrConvert(Map<String, Object> serialized, String key) {
        Object val = serialized.get(key);
        return getComponentOrConvert(val);
    }

    public static Component getComponentOrConvert(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String s) {
            return ComponentUtilAdventure.getLegacyComponentSerializer().deserialize(s);
        }
        return ((SerializableAdventureComponent) val).getComponent();
    }

}
