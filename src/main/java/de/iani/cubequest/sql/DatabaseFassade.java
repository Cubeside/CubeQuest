package de.iani.cubequest.sql;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.generation.DailyQuestData;
import de.iani.cubequest.generation.DelegatedGenerationData;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.sql.util.MySQLConnection;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.sql.util.SQLConnection;
import de.iani.cubequest.util.Pair;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;

public class DatabaseFassade {
    
    private CubeQuest plugin;
    
    private ServerDatabase serverDB;
    private QuestDatabase questDB;
    private DailyQuestDatabase dailyDB;
    private PlayerDatabase playerDB;
    
    private SQLConnection connection;
    
    private String tablePrefix;
    
    public DatabaseFassade() {
        this.plugin = CubeQuest.getInstance();
    }
    
    public boolean reconnect() {
        if (this.connection != null) {
            this.connection.disconnect();
            this.connection = null;
        }
        try {
            SQLConfig sqlconf = this.plugin.getSQLConfigData();
            
            this.tablePrefix = sqlconf.getTablePrefix();
            this.connection = new MySQLConnection(sqlconf.getHost(), sqlconf.getDatabase(), sqlconf.getUser(),
                    sqlconf.getPassword());
            
            this.serverDB = new ServerDatabase(this.connection, this.tablePrefix);
            this.questDB = new QuestDatabase(this.connection, this.tablePrefix);
            this.dailyDB = new DailyQuestDatabase(this.connection, this.tablePrefix);
            this.playerDB = new PlayerDatabase(this.connection, this.tablePrefix);
            
            this.serverDB.createTables();
            this.questDB.createTables();
            this.dailyDB.createTables();
            this.playerDB.createTables();
        } catch (Throwable e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not initialize database!", e);
            return false;
        }
        return true;
    }
    
    // ServerDatabase methods
    
    public int addServerId() throws SQLException {
        return this.serverDB.addServerId();
    }
    
    public void setServerName() throws SQLException {
        this.serverDB.setServerName();
    }
    
    public String getServerName(int serverId) throws SQLException {
        return this.serverDB.getServerName(serverId);
    }
    
    public int getServerId(String serverName) throws SQLException {
        return this.serverDB.getServerId(serverName);
    }
    
    public String[] getOtherBungeeServerNames() throws SQLException {
        return this.serverDB.getOtherBungeeServerNames();
    }
    
    public Map<String, Integer> getServersToGenerateDailyQuestOn() throws SQLException {
        return this.serverDB.getServersToGenerateDailyQuestOn();
    }
    
    public void setLegalQuestSpecificationCount(int count) throws SQLException {
        this.serverDB.setLegalQuestSpecificationCount(count);
    }
    
    // QuestDatabase methods
    
    public int reserveNewQuest() throws SQLException {
        return this.questDB.reserveNewQuest();
    }
    
    public void deleteQuest(int id) throws SQLException {
        this.questDB.deleteQuest(id);
    }
    
    public String getSerializedQuest(int id) throws SQLException {
        return this.questDB.getSerializedQuest(id);
    }
    
    public Map<Integer, String> getSerializedQuests() throws SQLException {
        return this.questDB.getSerializedQuests();
    }
    
    public void updateQuest(int id, String serialized) throws SQLException {
        this.questDB.updateQuest(id, serialized);
    }
    
    // PlayerDatabase methods
    
    public Pair<Integer, Integer> getPlayerData(UUID id) throws SQLException {
        return this.playerDB.getPlayerData(id);
    }
    
    public void setPlayerData(UUID id, int questPoints, int xp) throws SQLException {
        this.playerDB.setPlayerData(id, questPoints, xp);
    }
    
    public int changePlayerXp(UUID id, boolean set, int value) throws SQLException {
        return this.playerDB.changeXp(id, set, value);
    }
    
    public int changePlayerQuestPoints(UUID id, boolean set, int value) throws SQLException {
        return this.playerDB.changeQuestPoints(id, set, value);
    }
    
    public int countPlayersGivenTo(int questId) throws SQLException {
        return this.playerDB.countPlayersGivenTo(questId);
    }
    
    public Set<UUID> getPlayersWithState(int questId, Status status) throws SQLException {
        return this.playerDB.getPlayersWithState(questId, status);
    }
    
    public Map<Integer, QuestState> getQuestStates(UUID playerId) throws SQLException {
        return this.playerDB.getQuestStates(playerId);
    }
    
    public QuestState getPlayerState(int questId, UUID playerId) throws SQLException {
        return this.playerDB.getPlayerState(questId, playerId);
    }
    
    public void setPlayerState(int questId, UUID playerId, QuestState state) throws SQLException {
        this.playerDB.setPlayerState(questId, playerId, state);
    }
    
    public List<String> getSerializedRewardsToDeliver(UUID playerId) throws SQLException {
        return this.playerDB.getSerializedRewardsToDeliver(playerId);
    }
    
    public List<Reward> getAndDeleteRewardsToDeliver(UUID playerId) throws SQLException, InvalidConfigurationException {
        return this.playerDB.getAndDeleteRewardsToDeliver(playerId);
    }
    
    public void addRewardToDeliver(Reward reward, UUID playerId) throws SQLException {
        this.playerDB.addRewardToDeliver(reward, playerId);
    }
    
    public void transferPlayer(UUID oldId, UUID newId) throws SQLException {
        this.playerDB.transferPlayer(oldId, newId);
    }
    
    // DailyQuestDatabase methods
    
    public int reserveNewDailyQuestData() throws SQLException {
        return this.dailyDB.reserveNewDailyQuestData();
    }
    
    public void updateDailyQuestData(DailyQuestData data) throws SQLException {
        this.dailyDB.updateDailyQuestData(data);
    }
    
    public void deleteDailyQuestData(DailyQuestData data) throws SQLException {
        this.dailyDB.deleteDailyQuestData(data);
    }
    
    public List<DailyQuestData> getDailyQuestData() throws SQLException {
        return this.dailyDB.getDailyQuestData();
    }
    
    public void addDelegatedQuestGeneration(String server, DelegatedGenerationData data) throws SQLException {
        this.dailyDB.addDelegatedQuestGeneration(server, data);
    }
    
    public List<DelegatedGenerationData> popDelegatedQuestGenerations() throws SQLException {
        return this.dailyDB.popDelegatedQuestGenerations();
    }
    
    // package-scoped getters
    
    ServerDatabase getServerDB() {
        return this.serverDB;
    }
    
    QuestDatabase getQuestDB() {
        return this.questDB;
    }
    
    PlayerDatabase getPlayerDB() {
        return this.playerDB;
    }
    
}
