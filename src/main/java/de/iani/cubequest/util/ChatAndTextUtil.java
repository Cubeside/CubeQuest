package de.iani.cubequest.util;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.commands.ArgsParser;
import de.iani.cubequest.quests.Quest;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ChatAndTextUtil {

    // public static final String ID_PLACEHOLDER = "֎#ID#֎";   // seltenes Unicode-Symbol, damit der Platzhalter praktisch eindeutig ist.

    private static TreeMap<Integer, String> romanNumberMap;

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

    public static String formatTimespan(long timespan) {
        long days = timespan / (1000*60*60*24);
        long hours = (timespan / (1000*60*60)) % (1000*60*60*24);
        long minutes = (timespan / (1000*60)) % (1000*60*60);
        long seconds = (timespan / 1000) % (1000*60);

        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    public static String toRomanNumber(int arg) {
        int i =  romanNumberMap.floorKey(arg);
        if (arg == i) {
            return romanNumberMap.get(arg);
        }
        return romanNumberMap.get(i) + toRomanNumber(arg-i);
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
            String commandOnSelectionByClickingPreId, String commandOnSelectionByClickingPostId, String hoverTextPreId, String hoverTextPostId) {
//        return getQuest(sender, args, commandOnSelectionByClickingPreId, commandOnSelectionByClickingPostId, hoverTextPreId, hoverTextPostId, false);
//    }
//
//    public static Quest getQuest(CommandSender sender, ArgsParser args,
//            String commandOnSelectionByClickingPreId, String commandOnSelectionByClickingPostId, String hoverTextPreId, String hoverTextPostId,
//            boolean prioritizeId) {
        if (!commandOnSelectionByClickingPreId.startsWith("/")) {
            commandOnSelectionByClickingPreId = "/" + commandOnSelectionByClickingPreId;
        }

        String idString = args.getNext("");
        try {
            int id = Integer.parseInt(idString);
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
                return null;
            }
            return quest;
        } catch (NumberFormatException e) {
            String questString = args.hasNext()? idString + " " + args.getAll("") : idString;
            Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " + questString + ".");
                return null;
            } else if (quests.size() > 1) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverTextPreId + q.getId() + hoverTextPostId).create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,commandOnSelectionByClickingPreId + q.getId() + commandOnSelectionByClickingPostId);
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return null;
            }
            Bukkit.dispatchCommand(sender, commandOnSelectionByClickingPreId.substring(1) + Iterables.getFirst(quests, null).getId() + commandOnSelectionByClickingPostId);
            return null;
        }
    }

    public static Location getLocation(CommandSender sender, ArgsParser args, boolean noPitchOrYaw, boolean roundToBlock) {
        Location result;

        if (args.remaining() < 4) {
            if (!args.hasNext() && sender instanceof Player) {
                result = ((Player) sender).getLocation();
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Welt und die x-, y- und z-Koordinate des Orts an.");
                return null;
            }
        } else {
            String worldString = args.getNext();
            World world = Bukkit.getWorld(worldString);
            if (world == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Welt " + worldString + " nicht gefunden.");
                return null;
            }
            int x, y, z;
            float pitch = 0.0f, yaw = 0.0f;
            try {
                x = Integer.parseInt(args.getNext());
                y = Integer.parseInt(args.getNext());
                z = Integer.parseInt(args.getNext());
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die x- y- und z-Koordinate des Orts als ganze Zahlen an.");
                return null;
            }
            if (!noPitchOrYaw && args.remaining() > 1) {
                if (args.remaining() < 2) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib entweder nur x, y und z oder x, y, z, pitch und yaw an.");
                    return null;
                }
                try {
                    pitch = Float.parseFloat(args.getNext());
                    yaw = Float.parseFloat(args.getNext());
                } catch (NumberFormatException e) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib pitch und yaw des Orts als Gleitkommazahlen an.");
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

    public static boolean sendBaseComponent(CommandSender sender, List<BaseComponent[]> components) {
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

        return in.substring(0, index) + replacement + in.substring(index + sequence.length(), in.length());
    }

    public static String getNPCInfoString(Integer npcId) {
        return getNPCInfoString(true, npcId);
    }

    public static String getNPCInfoString(boolean forThisServer, Integer npcId) {
        String npcString = "";
        if (npcId == null) {
            npcString += ChatColor.RED + "NULL";
        } else {
            npcString += ChatColor.GREEN + "Id: " + npcId;
            if (forThisServer && CubeQuest.getInstance().hasCitizensPlugin()) {
                npcString += internalNPCInfoString(npcId);
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
            Location loc = npc.isSpawned()? npc.getEntity().getLocation() : npc.getStoredLocation();
            npcString += ", \"" + npc.getFullName() + "\"";
            if (loc != null) {
                npcString += " bei x: " + loc.getX() + ", y: " + loc.getY() + ", z: " + loc.getZ();
            }
        }
        return npcString;
    }

    public static String getLocationInfo(Location location) {
        return location == null? getLocationInfo(null, 0, 0, 0) : getLocationInfo(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    public static String getLocationInfo(String world, double x, double y, double z) {
        if (world == null) {
            return ChatColor.RED + "NULL";
        } else {
            return ChatColor.DARK_AQUA + "Welt: " + ChatColor.GREEN + world + ChatColor.DARK_AQUA + " x: " + ChatColor.GREEN + x + ChatColor.DARK_AQUA +
                    " y: " + ChatColor.GREEN + y + ChatColor.DARK_AQUA + " z: " + ChatColor.GREEN + z;
        }
    }

    public static String getToleranceInfo(double tolarance) {
        return ChatColor.DARK_AQUA + "Toleranz: " + (tolarance >= 0? ChatColor.GREEN : ChatColor.RED) + tolarance;
    }

    public static BaseComponent[] headline1(String content) {
        return new ComponentBuilder("--- " + content + " ---").color(ChatColor.DARK_GREEN).underlined(true).create();
    }

    public static BaseComponent[] headline2(String content) {
        return new ComponentBuilder(content).color(ChatColor.DARK_AQUA).bold(true).create();
    }

}
