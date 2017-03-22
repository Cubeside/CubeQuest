package de.iani.cubequest;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.cubequest.commands.CommandExecutor;
import de.iani.cubequest.commands.TabCompleter;
import de.iani.cubequest.quests.QuestManager;
import net.md_5.bungee.api.ChatColor;

public class CubeQuest extends JavaPlugin {

    public static final String pluginTag = ChatColor.BLUE + "[CubeQuest]";

    private CommandExecutor commandExecutor;

    public static void sendNormalMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(pluginTag + " " + ChatColor.GREEN + msg);
    }

    public static void sendWarningMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(pluginTag + " " + ChatColor.GOLD + msg);
    }

    public static void sendErrorMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(pluginTag + " " + ChatColor.RED + msg);
    }

    public static void sendMessage(CommandSender recipient, String msg) {
        recipient.sendMessage(pluginTag + " " + msg);
    }

    public static void sendNoPermissionMessage(CommandSender recipient) {
        sendErrorMessage(recipient, "Dazu fehlt dir die Berechtigung!");
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

    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        commandExecutor = new CommandExecutor(this);
        this.getCommand("quest").setExecutor(commandExecutor);
        this.getCommand("quest").setTabCompleter(new TabCompleter(this));

    }

    @Override
    public void onDisable() {

    }

    public QuestManager getQuestManager() {
        return QuestManager.getInstance();
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

}
