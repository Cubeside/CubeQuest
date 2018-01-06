package de.iani.cubequest.sql;

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
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.sql.util.MySQLConnection;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.sql.util.SQLConnection;
import javafx.util.Pair;

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
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        try {
            SQLConfig sqlconf = plugin.getSQLConfigData();
            
            tablePrefix = sqlconf.getTablePrefix();
            addServerIdString = "INSERT INTO `" + tablePrefix + "_servers` () VALUES ()";
            setServerNameString = "UPDATE `" + tablePrefix + "_servers` SET `name`=? WHERE `id`=?";
            getServerNameString = "SELECT `name` FROM `" + tablePrefix + "_servers` WHERE `id`=?";
            getOtherBungeeServerNamesString =
                    "SELECT `name` FROM `" + tablePrefix + "_servers` WHERE NOT `id`=?";
            // setGenerateDailyQuestsString = "UPDATE `" + tablePrefix + "_servers` SET
            // `generateDailyQuestsOn`=? WHERE `id`=?";
            getServersToGenerateDailyQuestsOn = "SELECT `name`, `legalQuestSpecifications` FROM `"
                    + tablePrefix + "_servers` WHERE `legalQuestSpecifications`>0";
            setLegalQuestSpecificationCountString = "UPDATE `" + tablePrefix
                    + "_servers` SET `legalQuestSpecifications`=? WHERE `id`=?";
            
            connection = new MySQLConnection(sqlconf.getHost(), sqlconf.getDatabase(),
                    sqlconf.getUser(), sqlconf.getPassword());
            
            questDB = new QuestDatabase(connection, tablePrefix);
            playerDB = new PlayerDatabase(connection, tablePrefix);
            
            createTables();
            // alterTables();
        } catch (Throwable e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize database!", e);
            return false;
        }
        return true;
    }
    
    private void createTables() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(tablePrefix + "_servers")) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + tablePrefix + "_servers` ("
                        + "`id` INT NOT NULL AUTO_INCREMENT," + "`name` TINYTEXT,"
                // + "`generateDailyQuestsOn` BIT(1) NOT NULL DEFAULT 0,"
                        + "`legalQuestSpecifications` INT NOT NULL DEFAULT 0,"
                        + "PRIMARY KEY ( `id` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
        questDB.createTable();
        playerDB.createTables();
    }
    
    /*
     * private void alterTables() throws SQLException { questDB.alterTable();
     * playerDB.alterTables(); }
     */
    
    public int addServerId() throws SQLException {
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(addServerIdString,
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
        connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(setServerNameString);
            smt.setString(1, CubeQuest.getInstance().getBungeeServerName());
            smt.setInt(2, CubeQuest.getInstance().getServerId());
            smt.executeUpdate();
            return null;
        });
    }
    
    public String getServerName(int serverId) throws SQLException {
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getServerNameString);
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
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(getOtherBungeeServerNamesString);
            smt.setInt(1, CubeQuest.getInstance().getServerId());
            ResultSet rs = smt.executeQuery();
            ArrayList<String> result = new ArrayList<String>();
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
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(getServersToGenerateDailyQuestsOn);
            ResultSet rs = smt.executeQuery();
            Map<String, Integer> result = new HashMap<String, Integer>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            rs.close();
            return result;
        });
    }
    
    public void setLegalQuestSpecificationCount(int count) throws SQLException {
        connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(setLegalQuestSpecificationCountString);
            smt.setInt(1, count);
            smt.setInt(2, CubeQuest.getInstance().getServerId());
            smt.executeUpdate();
            return null;
        });
    }
    
    public int reserveNewQuest() throws SQLException {
        return questDB.reserveNewQuest();
    }
    
    public void deleteQuest(int id) throws SQLException {
        questDB.deleteQuest(id);
    }
    
    public String getSerializedQuest(int id) throws SQLException {
        return questDB.getSerializedQuest(id);
    }
    
    public Map<Integer, String> getSerializedQuests() throws SQLException {
        return questDB.getSerializedQuests();
    }
    
    public void updateQuest(int id, String serialized) throws SQLException {
        questDB.updateQuest(id, serialized);
    }
    
    public Pair<Integer, Integer> getPlayerData(UUID id) throws SQLException {
        return playerDB.getPlayerData(id);
    }
    
    public void setPlayerData(UUID id, int questPoints, int xp) throws SQLException {
        playerDB.setPlayerData(id, questPoints, xp);
    }
    
    public int countPlayersGivenTo(int questId) throws SQLException {
        return playerDB.countPlayersGivenTo(questId);
    }
    
    public Map<Integer, QuestState> getQuestStates(UUID playerId) throws SQLException {
        return playerDB.getQuestStates(playerId);
    }
    
    public QuestState getPlayerState(int questId, UUID playerId) throws SQLException {
        return playerDB.getPlayerState(questId, playerId);
    }
    
    public void setPlayerState(int questId, UUID playerId, QuestState state) throws SQLException {
        playerDB.setPlayerState(questId, playerId, state);
    }
    
    public List<String> getSerializedRewardsToDeliver(UUID playerId) throws SQLException {
        return playerDB.getSerializedRewardsToDeliver(playerId);
    }
    
    public List<Reward> getAndDeleteRewardsToDeliver(UUID playerId)
            throws SQLException, InvalidConfigurationException {
        return playerDB.getAndDeleteRewardsToDeliver(playerId);
    }
    
    public void addRewardToDeliver(Reward reward, UUID playerId) throws SQLException {
        playerDB.addRewardToDeliver(reward, playerId);
    }
    
    protected QuestDatabase getQuestDB() {
        return questDB;
    }
    
    protected PlayerDatabase getPlayerDatabase() {
        return playerDB;
    }
    
}
