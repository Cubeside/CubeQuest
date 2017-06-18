package de.iani.cubequest.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.sql.util.SQLConnection;

public class PlayerDatabase {

    private SQLConnection connection;
    private String questStatesTableName;

    private final String countPlayersGivenToString;
    private final String getQuestStatesString;
    private final String getPlayerStatusString;
    private final String getPlayerStatesString;
    private final String getPlayerStateString;
    private final String updatePlayerStateString;
    private final String deletePlayerStateString;

    protected PlayerDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.questStatesTableName = tablePrefix + "_playerStates";

        this.countPlayersGivenToString = "SELECT COUNT player FRPM '" + questStatesTableName + "' WHERE status=1 AND quest=?";  // 1 ist GIVENTO
        this.getQuestStatesString = "SELECT quest, status, data FROM '" + questStatesTableName + "' WHERE status=1 AND player=?";   // 1 ist GIVENTO
        this.getPlayerStatusString = "SELECT status FROM '" + questStatesTableName + "' WHERE quest=? AND player=?";
        this.getPlayerStatesString = "SELECT player, status FROM '" + questStatesTableName + "' WHERE quest=?";
        this.deletePlayerStateString = "DELETE FROM '" + questStatesTableName + "' WHERE quest=? AND player=?";
        this.getPlayerStateString = "SELECT status, data  FROM '" + questStatesTableName + "' WHERE quest=? AND player=?";
        this.updatePlayerStateString = "INSERT INTO '" + questStatesTableName + "' (quest, player, status, state) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE status = ?, state = ?";
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
               smt.executeQuery();
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
               smt.executeQuery();
               return true;
           });
       }

   }

}
