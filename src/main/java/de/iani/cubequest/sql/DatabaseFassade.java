package de.iani.cubequest.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.sql.util.MySQLConnection;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.sql.util.SQLConnection;

public class DatabaseFassade {

    private CubeQuest plugin;

    private QuestDatabase questDB;
    private PlayerDatabase playerDB;

    private SQLConnection connection;

    private String tablePrefix;

    private final String addServerIdString;

    public DatabaseFassade(CubeQuest plugin) {
        addServerIdString = "INSERT INTO " + tablePrefix + "_servers () VALUES ()";

        questDB = new QuestDatabase(connection, tablePrefix);
        playerDB = new PlayerDatabase(connection, tablePrefix);
    }

    public boolean reconnect() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        try {
            SQLConfig sqlconf = plugin.getSQLConfigData();
            connection = new MySQLConnection(sqlconf.getHost(), sqlconf.getDatabase(), sqlconf.getUser(), sqlconf.getPassword());
            tablePrefix = sqlconf.getTablePrefix();

            createTables();
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
                        + "`id` INT NOT NULL AUTO_INCREMENT,"
                        + "PRIMARY KEY ( `id` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }

    public int addServerId() throws SQLException {
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(addServerIdString, Statement.RETURN_GENERATED_KEYS);
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

    public String getSerializedQuest(int id) throws SQLException {
        return questDB.getSerializedQuest(id);
    }

    public Map<Integer, String> getSerializedQuests() throws SQLException {
        return questDB.getSerializedQuests();
    }

    public void updateQuest(int id, String serialized) throws SQLException {
        questDB.updateQuest(id, serialized);
    }

    public Map<Integer, Status> getQuestStates(UUID playerId) throws SQLException {
        return playerDB.getQuestStates(playerId);
    }

    /*public Map<UUID, Status> getPlayerStates(int questId) throws SQLException {
        return playerDB.getPlayerStates(questId);
    }*/

    /*public Status getPlayerStatus(int questId, UUID playerId) throws SQLException {
        return playerDB.getPlayerStatus(questId, playerId);
    }

    public void setPlayerStatus(int questId, UUID playerId, Status status) throws SQLException {
        playerDB.setPlayerStatus(questId, playerId, status);
    }*/

    public QuestState getPlayerState(int questId, UUID playerId) throws SQLException {
        return playerDB.getPlayerState(questId, playerId);
    }

    public void setPlayerState(int questId, UUID playerId, QuestState state) throws SQLException {
        playerDB.setPlayerState(questId, playerId, state);
    }

    //TODO: erweiterte QuestStates (davon nur die laden, die gebraucht werden, andere nachladen).
    //TODO: Implementationen von getSerializedQuest und updateQuest

}
