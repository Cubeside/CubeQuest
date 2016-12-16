package de.iani.cubequest;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.cubequest.quests.QuestManager;
import net.md_5.bungee.api.ChatColor;

public class CubeQuest extends JavaPlugin {

    private QuestManager questManager;
    private EventListener eventListener;

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

    public CubeQuest() {
        questManager = new QuestManager();
        eventListener = new EventListener(this);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public QuestManager getQuestManager() {
        return questManager;
    }

}
