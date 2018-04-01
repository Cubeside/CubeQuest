package de.iani.cubequest.sql;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.sql.util.MySQLConnection;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.sql.util.SQLConnection;
import de.iani.cubequest.util.Pair;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;

public class DatabaseFassade {
    
    private CubeQuest plugin;
    
    private QuestDatabase questDB;
    private PlayerDatabase playerDB;
    
    private SQLConnection connection;
    
    private String tablePrefix;
    
    private String addServerIdString;
    private String setServerNameString;
    private String getServerNameString;
    private String getOtherBungeeServerNamesString;
    // private String setGenerateDailyQuestsString;
    private String getServersToGenerateDailyQuestsOn;
    private String setLegalQuestSpecificationCountString;
    
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
            this.addServerIdString = "INSERT INTO `" + this.tablePrefix + "_servers` () VALUES ()";
            this.setServerNameString =
                    "UPDATE `" + this.tablePrefix + "_servers` SET `name`=? WHERE `id`=?";
            this.getServerNameString =
                    "SELECT `name` FROM `" + this.tablePrefix + "_servers` WHERE `id`=?";
            this.getOtherBungeeServerNamesString =
                    "SELECT `name` FROM `" + this.tablePrefix + "_servers` WHERE NOT `id`=?";
            // setGenerateDailyQuestsString = "UPDATE `" + tablePrefix + "_servers` SET
            // `generateDailyQuestsOn`=? WHERE `id`=?";
            this.getServersToGenerateDailyQuestsOn =
                    "SELECT `name`, `legalQuestSpecifications` FROM `" + this.tablePrefix
                            + "_servers` WHERE `legalQuestSpecifications`>0";
            this.setLegalQuestSpecificationCountString = "UPDATE `" + this.tablePrefix
                    + "_servers` SET `legalQuestSpecifications`=? WHERE `id`=?";
            
            this.connection = new MySQLConnection(sqlconf.getHost(), sqlconf.getDatabase(),
                    sqlconf.getUser(), sqlconf.getPassword());
            
            this.questDB = new QuestDatabase(this.connection, this.tablePrefix);
            this.playerDB = new PlayerDatabase(this.connection, this.tablePrefix);
            
            createTables();
            // alterTables();
        } catch (Throwable e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not initialize database!", e);
            return false;
        }
        return true;
    }
    
    private void createTables() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.tablePrefix + "_servers")) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.tablePrefix + "_servers` ("
                        + "`id` INT NOT NULL AUTO_INCREMENT," + "`name` TINYTEXT,"
                // + "`generateDailyQuestsOn` BIT(1) NOT NULL DEFAULT 0,"
                        + "`legalQuestSpecifications` INT NOT NULL DEFAULT 0,"
                        + "PRIMARY KEY ( `id` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
        this.questDB.createTable();
        this.playerDB.createTables();
    }
    
    /*
     * private void alterTables() throws SQLException { questDB.alterTable();
     * playerDB.alterTables(); }
     */
    
    public int addServerId() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.addServerIdString,
                    Statement.RETURN_GENERATED_KEYS);
            smt.executeUpdate();
            int rv = 0;
            ResultSet rs = smt.getGeneratedKeys();
            if (rs.next()) {
                rv = rs.getInt(1);
            }
            rs.close();
            return rv;
        });
    }
    
    public void setServerName() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.setServerNameString);
            smt.setString(1, CubeQuest.getInstance().getBungeeServerName());
            smt.setInt(2, CubeQuest.getInstance().getServerId());
            smt.executeUpdate();
            return null;
        });
    }
    
    public String getServerName(int serverId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getServerNameString);
            smt.setInt(1, serverId);
            ResultSet rs = smt.executeQuery();
            String result = null;
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            return result;
        });
    }
    
    public String[] getOtherBungeeServerNames() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.getOtherBungeeServerNamesString);
            smt.setInt(1, CubeQuest.getInstance().getServerId());
            ResultSet rs = smt.executeQuery();
            ArrayList<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            rs.close();
            return result.toArray(new String[0]);
        });
    }
    
    /*
     * public void setGenerateDailyQuestsOnThisServer(boolean generate) throws SQLException {
     * connection.runCommands((connection, sqlConnection) -> { PreparedStatement smt =
     * sqlConnection.getOrCreateStatement(setGenerateDailyQuestsString); smt.setBoolean(1,
     * generate); smt.setInt(2, CubeQuest.getInstance().getServerId()); smt.executeUpdate(); return
     * null; }); }
     */
    
    public Map<String, Integer> getServersToGenerateDailyQuestOn() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.getServersToGenerateDailyQuestsOn);
            ResultSet rs = smt.executeQuery();
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            rs.close();
            return result;
        });
    }
    
    public void setLegalQuestSpecificationCount(int count) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.setLegalQuestSpecificationCountString);
            smt.setInt(1, count);
            smt.setInt(2, CubeQuest.getInstance().getServerId());
            smt.executeUpdate();
            return null;
        });
    }
    
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
    
    public Pair<Integer, Integer> getPlayerData(UUID id) throws SQLException {
        return this.playerDB.getPlayerData(id);
    }
    
    public void setPlayerData(UUID id, int questPoints, int xp) throws SQLException {
        this.playerDB.setPlayerData(id, questPoints, xp);
    }
    
    public int countPlayersGivenTo(int questId) throws SQLException {
        return this.playerDB.countPlayersGivenTo(questId);
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
    
    public List<Reward> getAndDeleteRewardsToDeliver(UUID playerId)
            throws SQLException, InvalidConfigurationException {
        return this.playerDB.getAndDeleteRewardsToDeliver(playerId);
    }
    
    public void addRewardToDeliver(Reward reward, UUID playerId) throws SQLException {
        this.playerDB.addRewardToDeliver(reward, playerId);
    }
    
    protected QuestDatabase getQuestDB() {
        return this.questDB;
    }
    
    protected PlayerDatabase getPlayerDatabase() {
        return this.playerDB;
    }
    
}
