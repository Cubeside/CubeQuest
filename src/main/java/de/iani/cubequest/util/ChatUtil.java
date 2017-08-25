package de.iani.cubequest.util;

import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import net.md_5.bungee.api.ChatColor;

public class ChatUtil {

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

}
