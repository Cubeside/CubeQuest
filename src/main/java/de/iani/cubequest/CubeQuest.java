package de.iani.cubequest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.cubequest.quests.QuestManager;
import net.md_5.bungee.api.ChatColor;

public class CubeQuest extends JavaPlugin {

    public static final String pluginTag = ChatColor.BLUE + "[CubeQuest]";

    public static void sendNormalMessage(Player player, String msg) {
        player.sendMessage(pluginTag + " " + ChatColor.GREEN + msg);
    }

    public static void sendWarningMessage(Player player, String msg) {
        player.sendMessage(pluginTag + " " + ChatColor.GOLD + msg);
    }

    public static void sendErrorMessage(Player player, String msg) {
        player.sendMessage(pluginTag + " " + ChatColor.RED + msg);
    }

    public static void sendMessage(Player player, String msg) {
        player.sendMessage(pluginTag + " " + msg);
    }

    public static CubeQuest getInstance() {
        return CubeQuest.getPlugin(CubeQuest.class);
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

    public CubeQuest() {
        EventListener eventListener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(eventListener, this);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public QuestManager getQuestManager() {
        return QuestManager.getInstance();
    }

}
