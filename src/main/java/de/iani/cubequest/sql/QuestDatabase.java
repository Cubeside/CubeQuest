package de.iani.cubequest.sql;

import de.iani.cubequest.sql.util.SQLConnection;

public class QuestDatabase {

    private SQLConnection connection;
    private String tableName;

    private final String getNameString;
    private final String getGiveMessageString;
    private final String getSuccessMessageString;
    private final String getFailMessageString;
    private final String getSuccessRewardString;
    private final String getFailRewardString;


    protected QuestDatabase(SQLConnection connection, String tablePrefix) {
        this.connection = connection;
        this.tableName = tablePrefix + "_questData";

        this.getNameString = "SELECT name FROM '" + tableName + "' WHERE id = ?";
    }

    public int reserveNewQuest() {

    }

    /*public String getName(int id) throws SQLException {

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(getNameString);
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

    }*/

}
