package de.iani.cubequest.sql;

import de.iani.cubequest.sql.util.SQLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class QuestDatabase {
    
    private SQLConnection connection;
    private String tableName;
    
    private final String addNewQuestIdString;
    private final String deleteQuestString;
    private final String getSerializedQuestString;
    private final String getSerializedQuestsString;
    private final String updateSeriazlizedQuestString;
    
    
    protected QuestDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.tableName = tablePrefix + "_questData";
        
        this.addNewQuestIdString = "INSERT INTO `" + tableName + "` () VALUES ()";
        this.deleteQuestString = "DELETE FROM `" + tableName + "` WHERE `id`=?";
        this.getSerializedQuestString = "SELECT `serialized` FROM " + tableName + " WHERE `id`=?";
        this.getSerializedQuestsString = "SELECT `id`, `serialized` FROM " + tableName;
        this.updateSeriazlizedQuestString = "INSERT INTO `" + tableName
                + "` (`id`,`serialized`) VALUES (?,?) ON DUPLICATE KEY UPDATE `serialized`=?";
    }
    
    protected void createTable() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(tableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + tableName + "` (" + "`id` INT AUTO_INCREMENT,"
                        + "`serialized` MEDIUMTEXT," + "PRIMARY KEY ( `id` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }
    
    public void deleteQuest(int id) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(deleteQuestString);
            smt.setInt(1, id);
            smt.executeUpdate();
            return null;
        });
    }
    
    public int reserveNewQuest() throws SQLException {
        return connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(addNewQuestIdString,
                    Statement.RETURN_GENERATED_KEYS);
            smt.executeUpdate();
            ResultSet rs = smt.getGeneratedKeys();
            rs.first();
            int rv = rs.getInt(1);
            rs.close();
            return rv;
        });
    }
    
    public String getSerializedQuest(int id) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getSerializedQuestString);
            smt.setInt(1, id);
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            String result = rs.getString(1);
            rs.close();
            return result;
        });
    }
    
    public Map<Integer, String> getSerializedQuests() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            Statement smt = connection.createStatement();
            ResultSet rs = smt.executeQuery(getSerializedQuestsString);
            HashMap<Integer, String> result = new HashMap<Integer, String>();
            while (rs.next()) {
                result.put(rs.getInt(1), rs.getString(2));
            }
            rs.close();
            return result;
        });
    }
    
    public void updateQuest(int id, String serialized) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(updateSeriazlizedQuestString);
            smt.setInt(1, id);
            smt.setString(2, serialized);
            smt.setString(3, serialized);
            smt.executeUpdate();
            return null;
        });
    }
    
    public String getTableName() {
        return tableName;
    }
    
}
