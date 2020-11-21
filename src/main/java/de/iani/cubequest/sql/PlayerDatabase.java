package de.iani.cubequest.sql;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.sql.util.SQLConnection;
import de.iani.cubequest.util.Pair;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerDatabase {
    
    private SQLConnection connection;
    private String playersTableName;
    private String questStatesTableName;
    private String rewardsToDeliverTableName;
    
    private final String getPlayerDataString;
    private final String updatePlayerDataString;
    private final String changeXpString;
    private final String setXpString;
    private final String getXpString;
    private final String changeQuestPointsString;
    private final String setQuestPointsString;
    private final String getQuestPointsString;
    private final String countPlayersGivenToString;
    private final String getQuestStatesString;
    private final String getPlayerStateString;
    private final String updatePlayerStateString;
    private final String deletePlayerStateString;
    private final String getRewardsToDeliverString;
    private final String addRewardsToDeliverString;
    private final String deleteRewardsToDeliverString;
    
    protected PlayerDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.playersTableName = tablePrefix + "_players";
        this.questStatesTableName = tablePrefix + "_playerStates";
        this.rewardsToDeliverTableName = tablePrefix + "_rewardsToDeliver";
        
        this.getPlayerDataString = "SELECT questPoints, xp FROM `" + this.playersTableName + "` WHERE id = ?";
        this.updatePlayerDataString = "INSERT INTO `" + this.playersTableName
                + "` (id, questPoints, xp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE questPoints = ?, xp = ?";
        this.changeXpString = "INSERT INTO `" + this.playersTableName
                + "` (id, xp) VALUES (?, ?) ON DUPLICATE KEY UPDATE xp = xp + ?";
        this.setXpString =
                "INSERT INTO `" + this.playersTableName + "` (id, xp) VALUES (?, ?) ON DUPLICATE KEY UPDATE xp = ?";
        this.getXpString = "SELECT xp FROM `" + this.playersTableName + "` WHERE id = ?";
        this.changeQuestPointsString = "INSERT INTO `" + this.playersTableName
                + "` (id, questPoints) VALUES (?, ?) ON DUPLICATE KEY UPDATE questPoints = questPoints + ?";
        this.setQuestPointsString = "INSERT INTO `" + this.playersTableName
                + "` (id, questPoints) VALUES (?, ?) ON DUPLICATE KEY UPDATE questPoints = ?";
        this.getQuestPointsString = "SELECT questPoints FROM `" + this.playersTableName + "` WHERE id = ?";
        this.countPlayersGivenToString =
                "SELECT COUNT(player) FROM `" + this.questStatesTableName + "` WHERE status=1 AND quest=?"; // 1 ist
                                                                                                            // GIVENTO
        this.getQuestStatesString =
                "SELECT quest, status, data FROM `" + this.questStatesTableName + "` WHERE status=1 AND player=?"; // 1
                                                                                                                   // ist
                                                                                                                   // GIVENTO
        this.deletePlayerStateString = "DELETE FROM `" + this.questStatesTableName + "` WHERE quest=? AND player=?";
        this.getPlayerStateString =
                "SELECT status, data  FROM `" + this.questStatesTableName + "` WHERE quest=? AND player=?";
        this.updatePlayerStateString = "INSERT INTO `" + this.questStatesTableName
                + "` (quest, player, status, data) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = ?, data = ?";
        this.getRewardsToDeliverString = "SELECT reward FROM `" + this.rewardsToDeliverTableName + "` WHERE player=?";
        this.addRewardsToDeliverString =
                "INSERT INTO `" + this.rewardsToDeliverTableName + "` (player, reward) VALUES (?, ?)";
        this.deleteRewardsToDeliverString = "DELETE FROM `" + this.rewardsToDeliverTableName + "` WHERE player=?";
    }
    
    protected void createTables() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(this.playersTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.playersTableName + "` (" + "`id` CHAR(36), "
                        + "`questPoints` INT NOT NULL DEFAULT 0, " + "`xp` INT NOT NULL DEFAULT 0, "
                        + "PRIMARY KEY (`id`)) " + "ENGINE = innodb");
                smt.close();
            }
            if (!sqlConnection.hasTable(this.questStatesTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.questStatesTableName + "` (" + "`quest` INT, "
                        + "`player` CHAR(36), " + "`status` INT NOT NULL, " + "`data` MEDIUMTEXT, "
                        + "PRIMARY KEY (`quest`, `player`), " + "FOREIGN KEY (`quest`) REFERENCES `"
                        + CubeQuest.getInstance().getDatabaseFassade().getQuestDB().getTableName()
                        + "` (`id`) ON UPDATE CASCADE ON DELETE CASCADE ," + "INDEX (`player`)" + ") ENGINE = innodb");
                smt.close();
            }
            if (!sqlConnection.hasTable(this.rewardsToDeliverTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + this.rewardsToDeliverTableName + "` ("
                        + "`id` INT AUTO_INCREMENT, " + "`player` CHAR(36), " + "`reward` MEDIUMTEXT, "
                        + "PRIMARY KEY (`id`) ) " + "INDEX (`player`)" + "ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }
    
    protected Pair<Integer, Integer> getPlayerData(UUID id) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getPlayerDataString);
            smt.setString(1, id.toString());
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            rs.first();
            Pair<Integer, Integer> result = new Pair<>(rs.getInt(1), rs.getInt(2));
            rs.close();
            return result;
        });
    }
    
    protected void setPlayerData(UUID id, int questPoints, int xp) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.updatePlayerDataString);
            smt.setString(1, id.toString());
            smt.setInt(2, questPoints);
            smt.setInt(3, xp);
            smt.setInt(4, questPoints);
            smt.setInt(5, xp);
            smt.executeUpdate();
            return null;
        });
    }
    
    protected int changeXp(UUID id, boolean set, int value) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(set ? this.setXpString : this.changeXpString);
            smt.setString(1, id.toString());
            smt.setInt(2, value);
            smt.setInt(3, value);
            smt.executeUpdate();
            
            smt = sqlConnection.getOrCreateStatement(this.getXpString);
            smt.setString(1, id.toString());
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("No result from database query!");
            }
            return rs.getInt(1);
        });
    }
    
    protected int changeQuestPoints(UUID id, boolean set, int value) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt =
                    sqlConnection.getOrCreateStatement(set ? this.setQuestPointsString : this.changeQuestPointsString);
            smt.setString(1, id.toString());
            smt.setInt(2, value);
            smt.setInt(3, value);
            smt.executeUpdate();
            
            smt = sqlConnection.getOrCreateStatement(this.getQuestPointsString);
            smt.setString(1, id.toString());
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("No result from database query!");
            }
            return rs.getInt(1);
        });
    }
    
    protected int countPlayersGivenTo(int questId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.countPlayersGivenToString);
            smt.setInt(1, questId);
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return 0;
            }
            rs.first();
            int result = rs.getInt(1);
            rs.close();
            return result;
        });
    }
    
    protected Map<Integer, QuestState> getQuestStates(UUID playerId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getQuestStatesString);
            smt.setString(1, playerId.toString());
            ResultSet rs = smt.executeQuery();
            HashMap<Integer, QuestState> result = new HashMap<>();
            while (rs.next()) {
                Status status = Status.values()[rs.getInt(2)];
                String serialized = rs.getString(3);
                result.put(rs.getInt(1), CubeQuest.getInstance().getQuestStateCreator().create(playerId, rs.getInt(1),
                        status, serialized));
            }
            rs.close();
            return result;
        });
        
    }
    
    /*
     * protected Map<UUID, String> getSerializedPlayerStates(int questId) throws SQLException {
     * 
     * return this.connection.runCommands((connection, sqlConnection) -> { PreparedStatement smt =
     * sqlConnection.getOrCreateStatement(getPlayerStatesString); smt.setInt(1, questId); ResultSet rs =
     * smt.executeQuery(); HashMap<UUID, QuestState> result = new HashMap<UUID, QuestState>(); while
     * (rs.next()) { //result.put(UUID.fromString(rs.getString(1)), Status.values()[rs.getInt(2)]);
     * result.put(UUID.fromString(rs.getString(1)), rs.getString(2)) } rs.close(); return result; });
     * 
     * }
     */
    
    /*
     * protected Status getPlayerStatus(int questId, UUID playerId) throws SQLException {
     * 
     * return this.connection.runCommands((connection, sqlConnection) -> { PreparedStatement smt =
     * sqlConnection.getOrCreateStatement(getPlayerStatusString); smt.setInt(1, questId);
     * smt.setString(2, playerId.toString()); ResultSet rs = smt.executeQuery(); if (!rs.next()) {
     * rs.close(); return null; } Status result = Status.values()[rs.getInt(1)]; rs.close(); return
     * result; });
     * 
     * }
     */
    
    protected QuestState getPlayerState(int questId, UUID playerId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getPlayerStateString);
            smt.setInt(1, questId);
            smt.setString(2, playerId.toString());
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            Status status = Status.values()[rs.getInt(1)];
            String serialized = rs.getString(2);
            rs.close();
            return CubeQuest.getInstance().getQuestStateCreator().create(playerId, questId, status, serialized);
        });
    }
    
    protected void setPlayerState(int questId, UUID playerId, QuestState state) throws SQLException {
        
        if (state == null) {
            this.connection.runCommands((connection, sqlConnection) -> {
                PreparedStatement smt = sqlConnection.getOrCreateStatement(this.deletePlayerStateString);
                smt.setInt(1, questId);
                smt.setString(2, playerId.toString());
                smt.executeUpdate();
                return true;
            });
        } else {
            this.connection.runCommands((connection, sqlConnection) -> {
                PreparedStatement smt = sqlConnection.getOrCreateStatement(this.updatePlayerStateString);
                String stateString = state.serialize();
                smt.setInt(1, questId);
                smt.setString(2, playerId.toString());
                smt.setInt(3, state.getStatus().ordinal());
                smt.setString(4, stateString);
                smt.setInt(5, state.getStatus().ordinal());
                smt.setString(6, stateString);
                smt.executeUpdate();
                return true;
            });
        }
        
    }
    
    protected List<String> getSerializedRewardsToDeliver(UUID playerId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.getRewardsToDeliverString);
            smt.setString(1, playerId.toString());
            ResultSet rs = smt.executeQuery();
            LinkedList<String> result = new LinkedList<>();
            while (rs.next()) {
                String serialized = rs.getString(1);
                result.add(serialized);
            }
            rs.close();
            return result;
        });
    }
    
    protected List<Reward> getAndDeleteRewardsToDeliver(UUID playerId)
            throws SQLException, InvalidConfigurationException {
        LinkedList<Reward> result = new LinkedList<>();
        List<String> serializedList = getSerializedRewardsToDeliver(playerId);
        YamlConfiguration yc = new YamlConfiguration();
        for (String s : serializedList) {
            yc.loadFromString(s);
            result.add((Reward) yc.get("reward"));
        }
        
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.deleteRewardsToDeliverString);
            smt.setString(1, playerId.toString());
            smt.executeUpdate();
            return null;
        });
        
        return result;
    }
    
    protected void addRewardToDeliver(Reward reward, UUID playerId) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.addRewardsToDeliverString);
            YamlConfiguration yc = new YamlConfiguration();
            yc.set("reward", reward);
            smt.setString(1, playerId.toString());
            smt.setString(2, yc.saveToString());
            smt.executeUpdate();
            return null;
        });
    }
    
}
