package de.iani.cubequest;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.cubequest.commands.CommandExecutor;
import de.iani.cubequest.commands.TabCompleter;
import de.iani.cubequest.sql.DatabaseFassade;
import de.iani.cubequest.sql.util.SQLConfig;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.util.YamlStorage;
import net.md_5.bungee.api.ChatColor;

public class CubeQuest extends JavaPlugin {

    public static final String pluginTag = ChatColor.BLUE + "[CubeQuest]";

    private CommandExecutor commandExecutor;
    private DatabaseFassade dbf;
    private NPCRegistry npcReg;

    private int serverId;

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
        dbf = new DatabaseFassade(this);
        if (!dbf.reconnect()) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
        commandExecutor = new CommandExecutor(this);
        this.getCommand("quest").setExecutor(commandExecutor);
        this.getCommand("quest").setTabCompleter(new TabCompleter(this));

        loadNPCs();
        loadServerId();
    }

    private void loadNPCs() {
        npcReg = CitizensAPI.getNamedNPCRegistry("CubeQuestNPCReg");
        if (npcReg == null) {
            npcReg = CitizensAPI.createNamedNPCRegistry("CubeQuestNPCReg", SimpleNPCDataStore.create(new YamlStorage(
                    new File(this.getDataFolder().getPath() + File.separator + "npcs.yml"))));
        }
    }

    private void loadServerId() {
        if (getConfig().contains("serverId")) {
            serverId = getConfig().getInt("serverId");
        } else {
            try {
                serverId = dbf.addServerId();

                getConfig().set("serverId", serverId);
                getDataFolder().mkdirs();
                File configFile = new File(getDataFolder(), "config.yml");
                configFile.createNewFile();
                getConfig().save(configFile);
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Could not create serverId!", e);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Could not save config!", e);
            }
        }
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

    public NPCRegistry getNPCReg() {
        return npcReg;
    }

    public int getServerId() {
        return serverId;
    }

    public SQLConfig getSQLConfigData() {
        // TODO Auto-generated method stub
        return null;
    }

}
