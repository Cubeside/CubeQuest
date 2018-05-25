package de.iani.cubequest.sql;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.DailyQuestData;
import de.iani.cubequest.generation.DelegatedGenerationData;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.sql.util.SQLConnection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DailyQuestDatabase {
    
    private SQLConnection connection;
    private String dailyQuestDataTableName;
    private String dailyQuestsTableName;
    private String delegatedGenerationTableName;
    
    private String addNewDataIdString;
    private String deleteDataString;
    private String updateDataString;
    private String updateQuestsString;
    
    private String getDataString;
    private String getQuestsString;
    
    private String addDelegatedGenerationString;
    private String getDelegatedGenerationsString;
    private String deleteDelegatedGenerationString;
    
    protected DailyQuestDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.dailyQuestDataTableName = tablePrefix + "_daily_quest_data";
        this.dailyQuestsTableName = tablePrefix + "_daily_quests";
        this.delegatedGenerationTableName = tablePrefix + "_delegated_generation_data";
        
        this.addNewDataIdString = "INSERT INTO `" + this.dailyQuestDataTableName + "` () VALUES ()";
        this.deleteDataString = "DELETE FROM `" + this.dailyQuestDataTableName + "` WHERE `id`=?";
        this.updateDataString = "UPDATE `" + this.dailyQuestDataTableName
                + "` SET `date_string`=?, `next_day_date`=?, `num_of_quests`=? WHERE `id`=?";
        this.updateQuestsString =
                "INSERT INTO `" + this.dailyQuestsTableName + "` (`data_id`, `ordinal`, `quest_id`)"
                        + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `quest_id`=?";
        
        this.getDataString = "SELECT `id`, `date_string`, `next_day_date`, `num_of_quests` FROM `"
                + this.dailyQuestDataTableName + "` ORDER BY date_string ASC, id ASC";
        this.getQuestsString = "SELECT `ordinal`, `quest_id` FROM `" + this.dailyQuestsTableName
                + "` WHERE `data_id`=?";
        
        this.addDelegatedGenerationString = "INSERT INTO `" + this.delegatedGenerationTableName
                + "` (`server`, `date_string`, `ordinal`, `difficulty`, `ran_seed`) VALUES (?, ?, ?, ?, ?)";
        this.getDelegatedGenerationsString =
                "SELECT `date_string`, `ordinal`, `difficulty`, `ran_seed` FROM `"
                        + this.delegatedGenerationTableName + "` WHERE `server`=?";
        this.deleteDelegatedGenerationString = "DELETE FROM `" + this.delegatedGenerationTableName
                + "` WHERE `server`=? AND `date_string`=? AND `ordinal`=?";
    }
    
    public void createTables() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.dailyQuestDataTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.dailyQuestDataTableName + "` ("
                        + " `id` INT NOT NULL AUTO_INCREMENT," + " `date_string` TINYTEXT,"
                        + " `next_day_date` DATE," + " `num_of_quests` INT,"
                        + " PRIMARY KEY ( `id` ) ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
        
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.dailyQuestsTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.dailyQuestsTableName + "` ("
                        + " `data_id` INT NOT NULL," + " `ordinal` INT NOT NULL,"
                        + " `quest_id` INT," + " PRIMARY KEY ( `data_id`, `ordinal` ),"
                        + " FOREIGN KEY ( `data_id` ) REFERENCES `" + this.dailyQuestDataTableName
                        + "` ( `id` ) ON UPDATE CASCADE ON DELETE CASCADE,"
                        + " FOREIGN KEY ( `quest_id` ) REFERENCES `"
                        + CubeQuest.getInstance().getDatabaseFassade().getQuestDB().getTableName()
                        + "` ( `id` ) ON UPDATE CASCADE ON DELETE RESTRICT ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
        
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.delegatedGenerationTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.delegatedGenerationTableName + "` ("
                        + " `server` VARCHAR(255) NOT NULL,"
                        + " `date_string` VARCHAR(255) NOT NULL," + " `ordinal` INT NOT NULL,"
                        + " `difficulty` DOUBLE NOT NULL," + " `ran_seed` BIGINT NOT NULL,"
                        + " PRIMARY KEY ( `server`, `date_string`, `ordinal` ),"
                        + " FOREIGN KEY ( `server` ) REFERENCES `"
                        + CubeQuest.getInstance().getDatabaseFassade().getServerDB().getTableName()
                        + "` ( `name` )" + " ) ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }
    
    public int reserveNewDailyQuestData() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.addNewDataIdString,
                    Statement.RETURN_GENERATED_KEYS);
            smt.executeUpdate();
            ResultSet rs = smt.getGeneratedKeys();
            rs.first();
            int rv = rs.getInt(1);
            rs.close();
            return rv;
        });
    }
    
    public void deleteDailyQuestData(DailyQuestData data) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.deleteDataString);
            smt.setInt(1, data.getId());
            smt.executeUpdate();
            return null;
        });
    }
    
    public void updateDailyQuestData(DailyQuestData data) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.updateDataString);
            smt.setString(1, data.getDateString());
            smt.setDate(2, new Date(data.getNextDayDate().getTime()));
            smt.setInt(3, data.getQuests().size());
            smt.setInt(4, data.getId());
            smt.executeUpdate();
            return null;
        });
        
        updateQuests(data);
    }
    
    private void updateQuests(DailyQuestData data) throws SQLException {
        int i = 0;
        for (Quest quest: data.getQuests()) {
            final int ordinal = i;
            this.connection.runCommands((connection, sqlConnection) -> {
                PreparedStatement smt = sqlConnection.getOrCreateStatement(this.updateQuestsString);
                Integer questId = quest == null ? null : quest.getId();
                smt.setInt(1, data.getId());
                smt.setInt(2, ordinal);
                smt.setObject(3, questId);
                smt.setObject(4, questId);
                smt.executeUpdate();
                return null;
            });
            
            i++;
        }
    }
    
    public List<DailyQuestData> getDailyQuestData() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            Statement smt = connection.createStatement();
            ResultSet rs = smt.executeQuery(this.getDataString);
            List<DailyQuestData> result = new ArrayList<>();
            while (rs.next()) { // TODO: entschachteln
                int id = rs.getInt(1);
                String dateString = rs.getString(2);
                Date nextDayDate = rs.getDate(3);
                int numOfQuests = rs.getInt(4);
                Integer[] quests = getQuests(id, numOfQuests);
                result.add(new DailyQuestData(id, Arrays.asList(quests), dateString, nextDayDate));
            }
            rs.close();
            return result;
        });
    }
    
    private Integer[] getQuests(int dataId, int numOfQuests) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getQuestsString);
            smt.setInt(1, dataId);
            ResultSet rs = smt.executeQuery();
            Integer[] result = new Integer[numOfQuests];
            while (rs.next()) {
                result[rs.getInt(1)] = rs.getInt(2);
            }
            rs.close();
            return result;
        });
    }
    
    public void addDelegatedQuestGeneration(String server, DelegatedGenerationData data)
            throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.addDelegatedGenerationString);
            smt.setString(1, server);
            smt.setString(2, data.dateString);
            smt.setInt(3, data.questOrdinal);
            smt.setDouble(4, data.difficulty);
            smt.setLong(5, data.ranSeed);
            smt.executeUpdate();
            return null;
        });
    }
    
    public List<DelegatedGenerationData> popDelegatedQuestGenerations() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.getDelegatedGenerationsString);
            smt.setString(1, CubeQuest.getInstance().getBungeeServerName());
            ResultSet rs = smt.executeQuery();
            List<DelegatedGenerationData> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new DelegatedGenerationData(rs.getString(1), rs.getInt(2),
                        rs.getDouble(3), rs.getLong(4)));
            }
            
            for (DelegatedGenerationData data: result) {
                deleteDelegatedGenerationData(data);
            }
            return result;
        });
    }
    
    private void deleteDelegatedGenerationData(DelegatedGenerationData data) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(this.deleteDelegatedGenerationString);
            smt.setString(1, CubeQuest.getInstance().getBungeeServerName());
            smt.setString(2, data.dateString);
            smt.setInt(3, data.questOrdinal);
            smt.executeUpdate();
            return null;
        });
    }
}
