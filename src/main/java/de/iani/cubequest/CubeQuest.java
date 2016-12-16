package de.iani.cubequest;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    public CubeQuest() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

}
