package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class ServerDependendQuest extends ProgressableQuest {
    
    private int serverId = -1;
    
    public ServerDependendQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward,
            int serverId) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        
        this.serverId = serverId;
    }
    
    public ServerDependendQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, String failMessage, Reward successReward, Reward failReward) {
        super(id, name, displayMessage, giveMessage, successMessage, failMessage, successReward,
                failReward);
        
        this.serverId = CubeQuest.getInstance().getServerId();
    }
    
    public ServerDependendQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward, int serverId) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null);
    }
    
    public ServerDependendQuest(int id, String name, String displayMessage, String giveMessage,
            String successMessage, Reward successReward) {
        this(id, name, displayMessage, giveMessage, successMessage, null, successReward, null);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        this.serverId = yc.getInt("serverId");
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        
        yc.set("serverId", this.serverId);
        
        return super.serializeToString(yc);
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        String serverName = getServerName();
        serverName = serverName == null ? ChatColor.GOLD + "(Name unbekannt)"
                : ChatColor.GREEN + serverName;
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Server: " + serverName
                + ChatColor.GREEN + " [id: " + this.serverId + "] "
                + (isForThisServer() ? ChatColor.GREEN + "(dieser Server)"
                        : ChatColor.GOLD + "(ein anderer Server)")).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == this.serverId;
    }
    
    public int getServerId() {
        return this.serverId;
    }
    
    public String getServerName() {
        try {
            return (isForThisServer() ? CubeQuest.getInstance().getBungeeServerName()
                    : CubeQuest.getInstance().getDatabaseFassade().getServerName(this.serverId));
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load server name for server with id " + this.serverId, e);
            return null;
        }
    }
    
    /**
     * Ruft KEIN QuestCreator#updateQuest auf! Sollte von aufrufender Quest getan werden!
     */
    protected void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }
    
}
