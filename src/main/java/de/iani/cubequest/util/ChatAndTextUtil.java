package de.iani.cubequest.util;

import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.commands.ArgsParser;
import de.iani.cubequest.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ChatAndTextUtil {

    public static final String ID_PLACEHOLDER = "֎#ID#֎";   // seltenes Unicode-Symbol, damit der Platzhalter praktisch eindeutig ist.

    private static TreeMap<Integer, String> romanNumberMap;

    static {
        romanNumberMap = new TreeMap<Integer, String>();
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

    public static Quest getQuest(CommandSender sender, ArgsParser args, String commandOnSelectionByClicking, String hoverText) {
        if (!commandOnSelectionByClicking.startsWith("/")) {
            commandOnSelectionByClicking = "/" + commandOnSelectionByClicking;
        }

        String questString = args.getAll("");
        try {
            int id = Integer.parseInt(questString);
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
                return null;
            }
            return quest;
        } catch (NumberFormatException e) {
            Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " + questString + ".");
                return null;
            } else if (quests.size() > 1) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText.replace(ID_PLACEHOLDER, "" + q.getId())).create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandOnSelectionByClicking.replace(ID_PLACEHOLDER, "" + q.getId()));
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return null;
            }
            Bukkit.dispatchCommand(sender, commandOnSelectionByClicking.substring(1).replace(ID_PLACEHOLDER, "" + Iterables.getFirst(quests, null).getId()));
            return null;
        }
    }

    public static String replaceLast(String in, String sequence, String replacement) {
        int index = in.lastIndexOf(sequence);
        if (index < 0) {
            return in;
        }

        return in.substring(0, index) + replacement + in.substring(index + sequence.length(), in.length());
    }

}
