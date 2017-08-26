package de.iani.cubequest.util;

import java.util.Set;

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
            return Iterables.getFirst(quests, null);
        }
    }

}
