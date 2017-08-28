package de.iani.cubequest.quests;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public abstract class ServerDependendQuest extends Quest {

    private int serverId;

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward, int serverId) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = serverId;
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, giveMessage, successMessage, failMessage, successReward, failReward);

        this.serverId = CubeQuest.getInstance().getServerId();
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, int serverId) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    public ServerDependendQuest(int id, String name, String giveMessage, String successMessage, Reward successReward) {
        this(id, name, giveMessage, successMessage, null, successReward, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        serverId = yc.getInt("serverId");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {

        yc.set("serverId", serverId);

        return super.serialize(yc);
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        String serverName = null;
        try {
            serverName = ChatColor.GREEN + (isForThisServer()? CubeQuest.getInstance().getBungeeServerName() : CubeQuest.getInstance().getDatabaseFassade().getServerName(serverId));
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load server name for server with id " + serverId, e);
        }
        serverName = serverName == null? ChatColor.GOLD + "(Name unbekannt)" : serverName;

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Server: " + serverName + ChatColor.GREEN + " [id: " + serverId + "] " + (isForThisServer()? ChatColor.GREEN + "(dieser Server)" : ChatColor.GOLD + "(ein anderer Server)")).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == serverId;
    }

    public int getServerId() {
        return serverId;
    }

    /**
     * Ruft KEIN QuestCreator#updateQuest auf! Sollte von aufrufender Quest getan werden!
     */
    protected void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }

}
