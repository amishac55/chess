package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.SQLException;

public abstract class SQLBaseClass {

    protected void configureDatabase(String[] createStatements) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Unable to configure database: %s", e.getMessage()));
        }
    }
}
