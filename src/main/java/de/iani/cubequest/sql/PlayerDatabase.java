package de.iani.cubequest.sql;

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

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.sql.util.SQLConnection;

public class PlayerDatabase {

    private SQLConnection connection;
    private String questStatesTableName;
    private String rewardsToDeliverTableName;

    private final String countPlayersGivenToString;
    private final String getQuestStatesString;
    private final String getPlayerStatusString;
    private final String getPlayerStatesString;
    private final String getPlayerStateString;
    private final String updatePlayerStateString;
    private final String deletePlayerStateString;
    private final String getRewardsToDeliverString;
    private final String addRewardsToDeliverString;
    private final String deleteRewardsToDeliverString;

    protected PlayerDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.questStatesTableName = tablePrefix + "_playerStates";
        this.rewardsToDeliverTableName = tablePrefix + "_rewardsToDeliver";

        this.countPlayersGivenToString = "SELECT COUNT player FROM `" + questStatesTableName + "` WHERE status=1 AND quest=?";  // 1 ist GIVENTO
        this.getQuestStatesString = "SELECT quest, status, data FROM `" + questStatesTableName + "` WHERE status=1 AND player=?";   // 1 ist GIVENTO
        this.getPlayerStatusString = "SELECT status FROM `" + questStatesTableName + "` WHERE quest=? AND player=?";
        this.getPlayerStatesString = "SELECT player, status FROM `" + questStatesTableName + "` WHERE quest=?";
        this.deletePlayerStateString = "DELETE FROM `" + questStatesTableName + "` WHERE quest=? AND player=?";
        this.getPlayerStateString = "SELECT status, data  FROM `" + questStatesTableName + "` WHERE quest=? AND player=?";
        this.updatePlayerStateString = "INSERT INTO `" + questStatesTableName + "` (quest, player, status, state) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE status = ?, state = ?";
        this.getRewardsToDeliverString = "SELECT reward FROM `" + rewardsToDeliverTableName + "` WHERE player=?";
        this.addRewardsToDeliverString = "INSERT INTO `" + rewardsToDeliverTableName + "` (player, reward) VALUES (?, ?)";
        this.deleteRewardsToDeliverString = "DELETE FROM `" + rewardsToDeliverTableName + "` WHERE player=?";
    }

    protected void createTables() throws SQLException {
    	this.connection.runCommands((connection, sqlConnection) -> {
            if (!sqlConnection.hasTable(questStatesTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + questStatesTableName + "` ("
                        + "`quest` INT, "
                		+ "`player` CHAR(36), "
                		+ "`status` INT NOT NULL, "
                        + "`data` MEDIUMTEXT, "
                        + "PRIMARY KEY (`quest`, `player`), "
                        + "FOREIGN KEY (`quest`) REFERENCES `" + CubeQuest.getInstance().getDatabaseFassade().getQuestDB().getTableName() + "` (`id`)) "
                        + "ENGINE = innodb");
                smt.close();
            }
            if (!sqlConnection.hasTable(rewardsToDeliverTableName)) {
                Statement smt = connection.createStatement();
                smt.executeUpdate("CREATE TABLE `" + rewardsToDeliverTableName + "` ("
                		+ "`id` INT AUTO_INCREMENT, "
                		+ "`player` CHAR(36), "
                        + "`reward` MEDIUMTEXT, "
                        + "PRIMARY KEY (`id`) ) "
                        + "ENGINE = innodb");
                smt.close();
            }
            return null;
        });
    }

    protected int countPlayersGivenTo(int questId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(countPlayersGivenToString);
            smt.setInt(1, questId);
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return 0;
            }
            int result = rs.getInt(1);
            rs.close();
            return result;
        });
    }

    protected Map<Integer, QuestState> getQuestStates(UUID playerId) throws SQLException {

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getQuestStatesString);
            smt.setString(1,  playerId.toString());
            ResultSet rs = smt.executeQuery();
            HashMap<Integer, QuestState> result = new HashMap<Integer, QuestState>();
            while (rs.next()) {
                Status status = Status.values()[rs.getInt(2)];
                String serialized = rs.getString(3);
                result.put(rs.getInt(1), CubeQuest.getInstance().getQuestStateCreator().create(playerId, rs.getInt(1), status, serialized));
            }
            rs.close();
            return result;
        });

    }

/*    protected Map<UUID, String> getSerializedPlayerStates(int questId) throws SQLException {

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getPlayerStatesString);
            smt.setInt(1, questId);
            ResultSet rs = smt.executeQuery();
            HashMap<UUID, QuestState> result = new HashMap<UUID, QuestState>();
            while (rs.next()) {
                //result.put(UUID.fromString(rs.getString(1)), Status.values()[rs.getInt(2)]);
                result.put(UUID.fromString(rs.getString(1)), rs.getString(2))
            }
            rs.close();
            return result;
        });

    }*/

/*    protected Status getPlayerStatus(int questId, UUID playerId) throws SQLException {

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getPlayerStatusString);
            smt.setInt(1, questId);
            smt.setString(2,  playerId.toString());
            ResultSet rs = smt.executeQuery();
            if (!rs.next()) {
                rs.close();
                return null;
            }
            Status result = Status.values()[rs.getInt(1)];
            rs.close();
            return result;
        });

    }*/

    protected QuestState getPlayerState(int questId, UUID playerId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getPlayerStateString);
            smt.setInt(1, questId);
            smt.setString(2,  playerId.toString());
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
               PreparedStatement smt = sqlConnection.getOrCreateStatement(deletePlayerStateString);
               smt.setInt(1, questId);
               smt.setString(2,  playerId.toString());
               smt.executeUpdate();
               return true;
           });
       } else {
           this.connection.runCommands((connection, sqlConnection) -> {
               PreparedStatement smt = sqlConnection.getOrCreateStatement(updatePlayerStateString);
               String stateString = state.serialize();
               smt.setInt(1, questId);
               smt.setString(2,  playerId.toString());
               smt.setInt(3, state.getStatus().ordinal());
               smt.setString(4,  stateString);
               smt.setInt(5, state.getStatus().ordinal());
               smt.setString(6, stateString);
               smt.executeUpdate();
               return true;
           });
       }

   }

	protected List<String> getSerializedRewardsToDeliver(UUID playerId) throws SQLException {
		return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getRewardsToDeliverString);
            smt.setString(1,  playerId.toString());
            ResultSet rs = smt.executeQuery();
            LinkedList<String> result = new LinkedList<String>();
            while (rs.next()) {
                String serialized = rs.getString(1);
                result.add(serialized);
            }
            rs.close();
            return result;
        });
	}

	protected List<Reward> getAndDeleteRewardsToDeliver(UUID playerId) throws SQLException, InvalidConfigurationException {
	    LinkedList<Reward> result = new LinkedList<Reward>();
	    List<String> serializedList = getSerializedRewardsToDeliver(playerId);
	    for (String s: serializedList) {
	        result.add(new Reward(s));
	    }

        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(deleteRewardsToDeliverString);
            smt.setString(1,  playerId.toString());
            smt.executeUpdate();
            return null;
        });

	    return result;
	}

    public void addRewardToDeliver(Reward reward, UUID playerId) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(addRewardsToDeliverString);
            YamlConfiguration yc = new YamlConfiguration();
            yc.getDefaultSection().set("reward", reward.serialize());
            smt.setString(1,  playerId.toString());
            smt.setString(2, yc.toString());
            smt.executeUpdate();
            return null;
        });
    }

}
