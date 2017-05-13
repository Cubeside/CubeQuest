package de.iani.cubequest.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.sql.util.MySQLConnection;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.sql.util.SQLConnection;

public class DatabaseFassade {

    private CubeQuest plugin;

    private SQLConnection connection;

    private String tablePrefix;

    private final String addServerIdString;

    public DatabaseFassade(CubeQuest plugin) {
        addServerIdString = "INSERT INTO " + tablePrefix + "_servers () VALUES ()";
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

}
