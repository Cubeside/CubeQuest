package de.iani.cubequest.sql;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.sql.util.SQLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerDatabase {
    
    private SQLConnection connection;
    private String serversTableName;
    
    private String addServerIdString;
    private String setServerNameString;
    private String getServerNameString;
    private String getServerIdString;
    private String getOtherBungeeServerNamesString;
    private String getServersToGenerateDailyQuestsOn;
    private String setLegalQuestSpecificationCountString;
    
    protected ServerDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.serversTableName = tablePrefix + "_servers";
        
        this.addServerIdString = "INSERT INTO `" + this.serversTableName + "` () VALUES ()";
        this.setServerNameString =
                "UPDATE `" + this.serversTableName + "` SET `name`=? WHERE `id`=?";
        this.getServerNameString =
                "SELECT `name` FROM `" + this.serversTableName + "` WHERE `id`=?";
        this.getServerIdString = "SELECT `id` FROM `" + this.serversTableName + "`WHERE `name`=?";
        this.getOtherBungeeServerNamesString =
                "SELECT `name` FROM `" + this.serversTableName + "` WHERE NOT `id`=?";
        this.getServersToGenerateDailyQuestsOn = "SELECT `name`, `legalQuestSpecifications` FROM `"
                + this.serversTableName + "` WHERE `legalQuestSpecifications`>0";
        this.setLegalQuestSpecificationCountString = "UPDATE `" + tablePrefix
                + "_servers` SET `legalQuestSpecifications`=? WHERE `id`=?";
    }
    
    public void createTables() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.serversTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.serversTableName + "` ("
                        + "`id` INT NOT NULL AUTO_INCREMENT," + "`name` VARCHAR(255),"
                        + "`legalQuestSpecifications` INT NOT NULL DEFAULT 0,"
                        + "PRIMARY KEY ( `id` ), UNIQUE( `name` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }
    
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
    
    public int getServerId(String serverName) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getServerIdString);
            smt.setString(1, serverName);
            ResultSet rs = smt.executeQuery();
            int result = -1;
            if (rs.next()) {
                result = rs.getInt(1);
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
    
    public String getTableName() {
        return this.serversTableName;
    }
    
}
