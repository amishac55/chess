package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.idao.AuthDAO;
import model.AuthData;

import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLAuthDAO extends SQLBaseClass implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        try {
            String[] createStatements = {
                    """
            CREATE TABLE IF NOT EXISTS authTable (
              `authToken` varchar(36) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
            };
            configureDatabase(createStatements);
        } catch (Exception e) {
            throw new DataAccessException(400, e.getMessage());
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO authTable (authToken, username) VALUES (?, ?)";
        try {
            executeUpdate(statement, authToken, username);
            return new AuthData(authToken, username);
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to create data: %s", e.getMessage()));
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authTable WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void clearAuthData() throws DataAccessException {
        var statement = "TRUNCATE authTable";
        try {
            executeUpdate(statement);
        } catch (Exception e) {
            throw new DataAccessException(400, "Unable to clear data: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            var statement = "DELETE FROM authTable WHERE authToken=?";
            executeUpdate(statement, authToken);
        } catch (Exception e) {
            throw new DataAccessException(500, "Invalid Authentication token");
        }

    }

    @Override
    public boolean verifyAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT COUNT(*) FROM authTable WHERE authToken = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to verify auth token: %s", e.getMessage()));
        }
        return false;
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(400, String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

}
